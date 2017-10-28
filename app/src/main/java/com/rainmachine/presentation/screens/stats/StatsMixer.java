package com.rainmachine.presentation.screens.stats;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.model.DayStatsDetails;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.Mixer;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.WateringLogDetailsDay;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.MetricCalculator;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class StatsMixer {

    private static final int NUM_PAST_DAYS_YEAR = 365;
    private static final int NUM_PAST_DAYS_MONTH = 30;
    private static final int NUM_PAST_DAYS_WEEK = 7;
    private static final int NUM_FUTURE_DAYS_WEEK = 6; // includes today

    static final int DATA_TYPE_WEEK = 0;
    static final int DATA_TYPE_MONTH_YEAR = 1;

    private Context context;
    private Features features;
    private DatabaseRepositoryImpl databaseRepository;
    private Device device;
    private SprinklerRepositoryImpl sprinklerRepository;
    private CalendarFormatter formatter;

    private LocalDate todayOfYearStream;
    private Single<Map<LocalDate, WateringLogDetailsDay>> yearStream;
    private StatsViewModel latestStatsViewModel;

    public StatsMixer(Context context, Device device, Features features, DatabaseRepositoryImpl
            databaseRepository, SprinklerRepositoryImpl sprinklerRepository, CalendarFormatter
                              formatter) {
        this.context = context;
        this.features = features;
        this.databaseRepository = databaseRepository;
        this.device = device;
        this.sprinklerRepository = sprinklerRepository;
        this.formatter = formatter;
    }

    public Observable<StatsViewModel> refresh(int dataType, boolean force) {
        Observable<StatsViewModel> observable;
        if (features.useNewApi()) {
            int numPastDays;
            int numFutureDays;
            if (dataType == DATA_TYPE_WEEK) {
                numPastDays = NUM_PAST_DAYS_WEEK;
                numFutureDays = NUM_FUTURE_DAYS_WEEK;
            } else {
                numPastDays = NUM_PAST_DAYS_YEAR;
                numFutureDays = 0;
            }
            observable = Observable.combineLatest(
                    sprinklerRepository.dashboardGraphs().toObservable(),
                    stats(numPastDays, numFutureDays, dataType, force),
                    (dashboardGraphs, statsViewModel) -> {
                        for (Program program : statsViewModel.programs) {
                            boolean isEntryInDb = false;
                            for (DashboardGraphs.DashboardGraph graph : dashboardGraphs
                                    .graphs) {
                                if (graph.programId == program.id) {
                                    graph.programName = program.name;
                                    isEntryInDb = true;
                                    break;
                                }
                            }
                            if (!isEntryInDb) {
                                dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                                        (DashboardGraphs.GraphType.PROGRAM, true, program.id,
                                                program.name));
                            }
                        }
                        statsViewModel.dashboardGraphs = dashboardGraphs;
                        return statsViewModel;
                    })
                    .doOnNext(statsViewModel -> latestStatsViewModel = statsViewModel);
        } else {
            observable = sprinklerRepository.weatherData3().toObservable()
                    .map(statsDays -> buildViewModel3(statsDays));
        }
        return observable
                .doOnSubscribe(disposable -> startMillis = System.currentTimeMillis())
                .doAfterTerminate(() -> {
                    long endMillis = System.currentTimeMillis();
                    Timber.d("Total duration refresh stats %d ms", endMillis - startMillis);
                });
    }

    private Observable<StatsViewModel> stats(final int numPastDays, final int numFutureDays,
                                             final int dataType, final boolean force) {
        Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogDetailsStream =
                (dataType == DATA_TYPE_WEEK || !device.isCloud())
                        ? sprinklerRepository.wateringLogDetails(numPastDays, false)
                        : yearlyWateringLogDetails(numPastDays);
        Single<List<Program>> programsStream = Single.zip(
                sprinklerRepository.programs(),
                sprinklerRepository.zonesProperties(),
                (programs, zoneProperties) -> {
                    Iterator<Program> itPrograms = programs.iterator();
                    while (itPrograms.hasNext()) {
                        Program program = itPrograms.next();
                        if (!program.enabled) {
                            itPrograms.remove();
                        }
                    }
                    for (Program program : programs) {
                        boolean isMasterValve;
                        boolean isEnabled;
                        Iterator<ProgramWateringTimes> it = program.wateringTimes.iterator();
                        while (it.hasNext()) {
                            ProgramWateringTimes wt = it.next();
                            isMasterValve = false;
                            isEnabled = false;
                            for (ZoneProperties zone : zoneProperties) {
                                if (wt.id == zone.id) {
                                    wt.referenceTime = zone.referenceTime;
                                    wt.hasDefaultAdvancedSettings = zone
                                            .hasDefaultAdvancedSettings();
                                    isMasterValve = zone.masterValve;
                                    isEnabled = zone.enabled;
                                    break;
                                }
                            }
                            if (isMasterValve || !isEnabled) {
                                it.remove();
                            }
                        }
                    }
                    return programs;
                });

        if (features.showVitalsChart()) {
            List<Single<?>> streams = new ArrayList<>();
            streams.add(wateringLogDetailsStream);
            streams.add(sprinklerRepository.statsDetails());
            streams.add(sprinklerRepository.mixers(numPastDays, numFutureDays));
            streams.add(programsStream);
            streams.add(sprinklerRepository.parsers());
            streams.add(sprinklerRepository.timeDate());
            streams.add(sprinklerRepository.devicePreferences());
            streams.add(sprinklerRepository.zonesProperties());
            streams.add(sprinklerRepository.globalRestrictions());

            return Single
                    .zip(streams, args -> {
                        int pos = 0;
                        Map<LocalDate, WateringLogDetailsDay> wateringLogDetailsDays =
                                (Map<LocalDate, WateringLogDetailsDay>) args[pos++];
                        Map<LocalDate, DayStatsDetails> statsDetailsDays = (Map<LocalDate,
                                DayStatsDetails>) args[pos++];
                        Map<LocalDate, Mixer> mixerDays = (Map<LocalDate, Mixer>) args[pos++];
                        List<Program> programs = (List<Program>) args[pos++];
                        List<Parser> parsers = (List<Parser>) args[pos++];
                        LocalDateTime sprinklerLocalDateTime = (LocalDateTime) args[pos++];
                        DevicePreferences devicePreferences = (DevicePreferences) args[pos++];
                        List<ZoneProperties> zoneProperties = (List<ZoneProperties>) args[pos++];
                        GlobalRestrictions globalRestrictions = (GlobalRestrictions) args[pos];
                        return buildViewModel(statsDetailsDays, programs,
                                wateringLogDetailsDays, null, mixerDays, parsers,
                                sprinklerLocalDateTime,
                                devicePreferences, zoneProperties, globalRestrictions, numPastDays,
                                numFutureDays, dataType, force);
                    })
                    .toObservable();
        } else {
            return Single.zip(
                    wateringLogDetailsStream,
                    sprinklerRepository.wateringLogSimulatedDetails(numPastDays),
                    sprinklerRepository.statsDetails(),
                    sprinklerRepository.mixers(numPastDays, numFutureDays),
                    programsStream,
                    sprinklerRepository.devicePreferences(),
                    (wateringLogDetailsDays, wateringLogSimulatedDetailsDays, statsDetailsDays,
                     mixerDays, programs, devicePreferences) -> buildViewModel
                            (statsDetailsDays, programs,
                                    wateringLogDetailsDays, wateringLogSimulatedDetailsDays,
                                    mixerDays, null, null, devicePreferences, null, null,
                                    numPastDays, numFutureDays, dataType, force))
                    .toObservable();
        }
    }

    private long startMillis;

    private Single<Map<LocalDate, WateringLogDetailsDay>> yearlyWateringLogDetails(int numPastDays) {
        if (yearStream != null && todayOfYearStream.equals(new LocalDate())) {
            return yearStream;
        }
        final int NUM_DAYS_PER_BATCH = 15;
        LocalDate startDate = new LocalDate().minusDays(numPastDays);
        yearStream = sprinklerRepository
                .parallelWateringLogDetails(startDate, numPastDays, NUM_DAYS_PER_BATCH)
                .cache()
                .doOnError(throwable -> {
                    /* We need to nullify, otherwise it will return error to all
                    subscribers in the future */
                    yearStream = null;
                });
        todayOfYearStream = new LocalDate();
        return yearStream;
    }

    private StatsViewModel buildViewModel3(List<DayStats> stats) {
        StatsViewModel viewModel = new StatsViewModel();
        for (DayStats day : stats) {
            day.weatherImageId = WeatherIconMapper.get(day.iconName);
        }
        viewModel.stats = stats;
        return viewModel;
    }

    private StatsViewModel buildViewModel(Map<LocalDate, DayStatsDetails> statsDetailsDays,
                                          List<Program> programs,
                                          Map<LocalDate, WateringLogDetailsDay>
                                                  wateringLogDetailsDays,
                                          Map<LocalDate, WateringLogDetailsDay>
                                                  wateringLogSimulatedDetailsDays,
                                          Map<LocalDate, Mixer> mixerDays,
                                          List<Parser> parsers,
                                          LocalDateTime sprinklerLocalDateTime,
                                          DevicePreferences devicePreferences,
                                          List<ZoneProperties> zonesProperties,
                                          GlobalRestrictions globalRestrictions, int numPastDays,
                                          int numFutureDays, int dataType, boolean force) {
        StatsViewModel viewModel = getLatestData();
        if (viewModel == null || force) {
            viewModel = new StatsViewModel();
        }
        StatsDataCategory category;
        if (dataType == DATA_TYPE_WEEK) {
            viewModel.weekCategory = new StatsDataCategory();
            viewModel.weekCategory.days = new HashMap<>();
            viewModel.weekCategory.startDate = new LocalDate().minusDays(numPastDays);
            viewModel.weekCategory.numDays = numPastDays + numFutureDays;
            category = viewModel.weekCategory;
        } else {
            viewModel.monthCategory = new StatsDataCategory();
            viewModel.monthCategory.days = new HashMap<>();
            viewModel.monthCategory.startDate = new LocalDate().minusDays(numPastDays);
            viewModel.monthCategory.numDays = numPastDays + numFutureDays;
            viewModel.yearCategory = new StatsDataCategory();
            viewModel.yearCategory.days = new HashMap<>();
            viewModel.yearCategory.startDate = new LocalDate().minusDays(numPastDays);
            viewModel.yearCategory.numDays = numPastDays + numFutureDays;
            category = viewModel.yearCategory;
        }
        viewModel.programs = programs;

        final float MAX_PERCENTAGE_ALLOWED = 2.0f; // 200%
        LocalDate today = new LocalDate();
        for (int i = 0; i < category.numDays; i++) {
            LocalDate date = category.startDate.plusDays(i);
            StatsDayViewModel dayViewModel = new StatsDayViewModel();
            dayViewModel.date = date;

            addTemperatureRainAmountInfo(dayViewModel, date, mixerDays, devicePreferences,
                    globalRestrictions);

            addProgramDailyWaterNeed(dayViewModel, date, today, programs, wateringLogDetailsDays,
                    statsDetailsDays, MAX_PERCENTAGE_ALLOWED);

            if (features.hasDailyWaterNeedChart()) {
                addDailyWaterNeed(dayViewModel, date, today, wateringLogSimulatedDetailsDays,
                        statsDetailsDays, MAX_PERCENTAGE_ALLOWED);
            }

            category.days.put(date, dayViewModel);
        }

        viewModel.simulationExpiredProgramIds = new ArrayList<>(programs.size());
        if (features.hasSimulationFunctionality()) {
            for (Program program : programs) {
                if (program.simulationExpired) {
                    viewModel.simulationExpiredProgramIds.add(program.id);
                }
            }
        }

        // This particular data is the same for month and year. We filled data for year and now
        // we also fill for month
        if (dataType != DATA_TYPE_WEEK) {
            viewModel.monthCategory.days.putAll(category.days);
        }

        viewModel.isUnitsMetric = devicePreferences.isUnitsMetric;
        viewModel.use24HourFormat = devicePreferences.use24HourFormat;
        viewModel.sprinklerLocalDateTime = sprinklerLocalDateTime;

        if (features.showVitalsChart()) {
            addVitalsInfo(viewModel, parsers, zonesProperties, programs, wateringLogDetailsDays,
                    devicePreferences, sprinklerLocalDateTime, dataType);
        }
        computeMinMax(viewModel, programs, dataType);
        viewModel.showDailyWaterNeedChart = features.hasDailyWaterNeedChart();
        return viewModel;
    }

    private void addTemperatureRainAmountInfo(StatsDayViewModel dayViewModel, LocalDate date,
                                              Map<LocalDate, Mixer> mixerDays,
                                              DevicePreferences devicePreferences,
                                              GlobalRestrictions globalRestrictions) {
        final int MAX_TEMPERATURE_ALLOWED = devicePreferences.isUnitsMetric ? 50 : 122;
        final int MIN_TEMPERATURE_ALLOWED = devicePreferences.isUnitsMetric ? -50 : -58;
        final int MAX_RAIN_AMOUNT_ALLOWED = devicePreferences.isUnitsMetric ? 51 : 2;
        Mixer mixer = mixerDays.get(date);
        if (mixer != null) {
            if (devicePreferences.isUnitsMetric) {
                dayViewModel.maxTemperature = Math.round(mixer.maxTemperature);
                dayViewModel.minTemperature = Math.round(mixer.minTemperature);
            } else {
                dayViewModel.maxTemperature = Math.round(MetricCalculator.celsiusToFahrenheit
                        (mixer.maxTemperature));
                dayViewModel.minTemperature = Math.round(MetricCalculator.celsiusToFahrenheit
                        (mixer.minTemperature));
            }
            if (globalRestrictions != null && globalRestrictions.freezeProtectEnabled) {
                // Both are in Celsius
                dayViewModel.lessOrEqualToFreezeProtect = mixer.minTemperature <=
                        globalRestrictions.freezeProtectTemperature(true);
            }

            int iconId;
            if (mixer.condition != null) {
                iconId = WeatherIconMapper.get(mixer.condition);
                if (iconId == 0) {
                    iconId = WeatherIconMapper.getDefaultWeatherIcon();
                }
            } else {
                iconId = WeatherIconMapper.getDefaultWeatherIcon();
            }
            dayViewModel.iconId = iconId;

            if (devicePreferences.isUnitsMetric) {
                dayViewModel.rainAmount = mixer.qpf;
            } else {
                dayViewModel.rainAmount = MetricCalculator.mmToInch(mixer.qpf);
            }
        } else {
            dayViewModel.maxTemperature = Integer.MIN_VALUE;
            dayViewModel.minTemperature = Integer.MIN_VALUE;
            dayViewModel.iconId = View.NO_ID;
            dayViewModel.rainAmount = 0.0f;
        }
        if (dayViewModel.maxTemperature < MIN_TEMPERATURE_ALLOWED
                || dayViewModel.maxTemperature > MAX_TEMPERATURE_ALLOWED) {
            dayViewModel.maxTemperature = Integer.MIN_VALUE;
        }
        if (dayViewModel.minTemperature < MIN_TEMPERATURE_ALLOWED
                || dayViewModel.minTemperature > MAX_TEMPERATURE_ALLOWED) {
            dayViewModel.minTemperature = Integer.MIN_VALUE;
        }
        if (dayViewModel.rainAmount < 0 || dayViewModel.rainAmount > MAX_RAIN_AMOUNT_ALLOWED) {
            dayViewModel.rainAmount = 0.0f;
        }
    }

    private void addProgramDailyWaterNeed(StatsDayViewModel dayViewModel, LocalDate date,
                                          LocalDate today, List<Program> programs,
                                          Map<LocalDate, WateringLogDetailsDay>
                                                  wateringLogDetailsDays,
                                          Map<LocalDate, DayStatsDetails> statsDetailsDays,
                                          final float MAX_PERCENTAGE_ALLOWED) {
        dayViewModel.programDailyWaterNeed = new SparseArray<>(programs.size());
        for (Program program : programs) {
            dayViewModel.programDailyWaterNeed.put((int) program.id, 0.0f);
        }
        if (date.isBefore(today)) {
            WateringLogDetailsDay dayLog = wateringLogDetailsDays.get(date);
            if (dayLog != null && dayLog.programDailyWaterNeed != null) {
                for (Program program : programs) {
                    Float waterNeed = dayLog.programDailyWaterNeed.get((int) program.id);
                    if (waterNeed != null) {
                        dayViewModel.programDailyWaterNeed.put((int) program.id,
                                waterNeed);
                    }
                }
            }
        } else {
            // For SPK1 3.64+ we leave all program chart data points to 0
            if (!features.hideFutureDaysForProgramCharts()) {
                DayStatsDetails dayStatsDetails = statsDetailsDays.get(date);
                if (dayStatsDetails != null) {
                    for (Program program : programs) {
                        Float waterConsumption = dayStatsDetails.programDailyWaterNeed.get(
                                (int) program.id);
                        if (waterConsumption != null) {
                            dayViewModel.programDailyWaterNeed.put((int) program.id,
                                    waterConsumption);
                        }
                    }
                }
            }
        }
        for (Program program : programs) {
            float waterConsumption = dayViewModel.programDailyWaterNeed.get((int) program.id);
            if (waterConsumption < 0 || waterConsumption > MAX_PERCENTAGE_ALLOWED) {
                dayViewModel.programDailyWaterNeed.put((int) program.id, 0.0f);
            }
        }
    }

    private void addDailyWaterNeed(StatsDayViewModel dayViewModel, LocalDate date, LocalDate today,
                                   Map<LocalDate, WateringLogDetailsDay>
                                           wateringLogSimulatedDetailsDays,
                                   Map<LocalDate, DayStatsDetails> statsDetailsDays,
                                   final float MAX_PERCENTAGE_ALLOWED) {
        if (date.isBefore(today)) {
            WateringLogDetailsDay dayLog = wateringLogSimulatedDetailsDays.get(date);
            if (dayLog != null) {
                dayViewModel.dailyWaterNeed = dayLog.totalDailyWaterNeed;
            } else {
                dayViewModel.dailyWaterNeed = 0.0f;
            }
        } else {
            DayStatsDetails dayStatsDetails = statsDetailsDays.get(date);
            if (dayStatsDetails != null) {
                dayViewModel.dailyWaterNeed = dayStatsDetails.dailyWaterNeed;
            } else {
                dayViewModel.dailyWaterNeed = 0.0f;
            }
        }
        if (dayViewModel.dailyWaterNeed < 0 || dayViewModel.dailyWaterNeed >
                MAX_PERCENTAGE_ALLOWED) {
            dayViewModel.dailyWaterNeed = 0.0f;
        }
    }

    private void addVitalsInfo(StatsViewModel viewModel, List<Parser> parsers,
                               List<ZoneProperties> zonesProperties, List<Program> programs,
                               Map<LocalDate, WateringLogDetailsDay> wateringLogDetailsDays,
                               DevicePreferences devicePreferences,
                               LocalDateTime sprinklerLocalDateTime, int dataType) {
        viewModel.showVitals = true;
        // Compute water saved
        if (dataType == DATA_TYPE_WEEK) {
            viewModel.weekCategory.waterSavedPercentage = computeWaterSavedPercentage
                    (NUM_PAST_DAYS_WEEK, wateringLogDetailsDays);
            viewModel.weekCategory.waterSavedAmount = computeWaterSavedAmount
                    (NUM_PAST_DAYS_WEEK,
                            zonesProperties, wateringLogDetailsDays, devicePreferences);
        } else {
            viewModel.monthCategory.waterSavedPercentage = computeWaterSavedPercentage
                    (NUM_PAST_DAYS_MONTH, wateringLogDetailsDays);
            viewModel.monthCategory.waterSavedAmount = computeWaterSavedAmount
                    (NUM_PAST_DAYS_MONTH, zonesProperties, wateringLogDetailsDays,
                            devicePreferences);
            viewModel.yearCategory.waterSavedPercentage = computeWaterSavedPercentage
                    (NUM_PAST_DAYS_YEAR, wateringLogDetailsDays);
            viewModel.yearCategory.waterSavedAmount = computeWaterSavedAmount
                    (NUM_PAST_DAYS_YEAR,
                            zonesProperties, wateringLogDetailsDays, devicePreferences);
        }

        LocalDateTime nextWatering = null;
        LocalDateTime dateTime;
        for (Program program : programs) {
            if (program.nextRunSprinklerLocalDate != null) {
                dateTime = program.nextRunSprinklerLocalDate.toLocalDateTime(program
                        .startTime.localDateTime.toLocalTime());
            } else {
                dateTime = null;
            }
            if (nextWatering == null || (dateTime != null && nextWatering.isAfter(dateTime))) {
                nextWatering = dateTime;
            }
        }
        if (nextWatering == null) {
            viewModel.nextWateringCycle = context.getString(R.string
                    .stats_no_next_watering_cycle);
        } else {
            String dayOfWeek = nextWatering.dayOfWeek().getAsShortText(Locale.ENGLISH);
            String s = dayOfWeek + " " +
                    nextWatering.toString("MMM dd, yyyy", Locale.ENGLISH) + " at " +
                    CalendarFormatter.hourMinColon(nextWatering.toLocalTime(),
                            devicePreferences.use24HourFormat);
            viewModel.nextWateringCycle = context.getString(R.string
                    .stats_next_watering_cycle, s);
        }

        LocalDateTime lastRun = null;
        for (Parser parser : parsers) {
            if (parser.lastRun != null && (lastRun == null || parser.lastRun.isAfter(lastRun))) {
                lastRun = parser.lastRun;
            }
        }
        if (lastRun == null) {
            viewModel.lastWeatherUpdate = context.getString(R.string.stats_last_weather_update,
                    "never");
        } else {
            String elapsed = formatter.ago(lastRun, sprinklerLocalDateTime);
            viewModel.lastWeatherUpdate = context.getString(R.string.stats_last_weather_update,
                    elapsed);
        }
    }

    private float computeWaterSavedPercentage(int numDays, Map<LocalDate, WateringLogDetailsDay>
            wateringLogDetailsDays) {
        LocalDate startDate = new LocalDate().minusDays(numDays);
        LocalDate date;
        int totalRealDuration = 0;
        int totalUserDuration = 0;
        for (int i = 0; i < numDays; i++) {
            date = startDate.plusDays(i);
            WateringLogDetailsDay wateringLogDetails = wateringLogDetailsDays.get(date);
            if (wateringLogDetails != null) {
                totalRealDuration += wateringLogDetails.totalRealDuration;
                totalUserDuration += wateringLogDetails.totalUserDuration;
            }
        }

        float waterSaved;
        if (totalUserDuration == 0) {
            waterSaved = 0.0f;
        } else {
            waterSaved = 1 - (totalRealDuration * 1.0f) / totalUserDuration;
        }
        // We should not have negative percent. It does not look right to the user :)
        if (waterSaved < 0) {
            waterSaved = 0;
        }
        return waterSaved;
    }

    private float computeWaterSavedAmount(int numDays, List<ZoneProperties> zonesProperties,
                                          Map<LocalDate, WateringLogDetailsDay>
                                                  wateringLogDetailsDays,
                                          DevicePreferences devicePreferences) {
        LocalDate startDate = new LocalDate().minusDays(numDays);
        LocalDate date;
        float waterSavedAmount = 0f;
        for (int i = 0; i < numDays; i++) {
            date = startDate.plusDays(i);
            WateringLogDetailsDay wateringLogDetails = wateringLogDetailsDays.get(date);
            if (wateringLogDetails != null) {
                for (ZoneProperties zoneProperties : zonesProperties) {
                    waterSavedAmount += computeWaterSavedAmount(zoneProperties,
                            wateringLogDetails, devicePreferences);
                }
            }
        }

        // We should not have negative value. It does not look right to the user :)
        if (waterSavedAmount < 0) {
            waterSavedAmount = 0;
        }
        return waterSavedAmount;
    }

    private float computeWaterSavedAmount(ZoneProperties zoneProperties, WateringLogDetailsDay
            wateringLogDetails, DevicePreferences devicePreferences) {
        int totalSavedDuration = 0; // how many seconds saved during a day
        for (WateringLogDetailsDay.Program program : wateringLogDetails.programs) {
            for (WateringLogDetailsDay.Program.Zone zone : program.zones) {
                if (zone.id == zoneProperties.id) {
                    totalSavedDuration += (zone.totalUserDuration - zone.totalRealDuration);
                    break;
                }
            }
        }
        float waterSavedAmount = DomainUtils.computeWaterVolume(zoneProperties,
                totalSavedDuration);
        if (!devicePreferences.isUnitsMetric) {
            waterSavedAmount = MetricCalculator.volumeMetersToGal(waterSavedAmount);
        }
        return waterSavedAmount;
    }

    private void computeMinMax(StatsViewModel viewModel, List<Program> programs, int dataType) {
        if (dataType == DATA_TYPE_WEEK) {
            computeMinMaxWeek(viewModel, viewModel.weekCategory, programs);
        } else if (dataType == DATA_TYPE_MONTH_YEAR) {
            computeMinMaxMonthYear(viewModel, viewModel.monthCategory, programs);
            computeMinMaxMonthYear(viewModel, viewModel.yearCategory, programs);
        }
    }

    private void computeMinMaxWeek(StatsViewModel viewModel, StatsDataCategory category,
                                   List<Program> programs) {
        category.scaleViewModel = new ScaleViewModel();
        float maxPercentDailyWaterNeed = 100f;
        int maxValueTemperature = Integer.MIN_VALUE;
        int minValueTemperature = Integer.MAX_VALUE;
        float maxRainAmount = viewModel.isUnitsMetric ? 5f : 0.2f;
        category.scaleViewModel.maxPercentPrograms = new SparseArray<>();
        for (Program program : programs) {
            category.scaleViewModel.maxPercentPrograms.put((int) program.id, 100);
        }
        for (int i = 0; i < category.numDays; i++) {
            LocalDate date = category.startDate.plusDays(i);
            StatsDayViewModel dayViewModel = category.days.get(date);
            if (dayViewModel.dailyWaterNeed * 100 > maxPercentDailyWaterNeed) {
                maxPercentDailyWaterNeed = dayViewModel.dailyWaterNeed * 100;
            }
            if (dayViewModel.maxTemperature != Integer.MIN_VALUE) {
                if (dayViewModel.maxTemperature > maxValueTemperature) {
                    maxValueTemperature = dayViewModel.maxTemperature;
                }
            }
            if (dayViewModel.minTemperature != Integer.MIN_VALUE) {
                if (dayViewModel.minTemperature < minValueTemperature) {
                    minValueTemperature = dayViewModel.minTemperature;
                }
            }
            for (Program program : viewModel.programs) {
                float percent = dayViewModel.programDailyWaterNeed.get((int) program.id);
                float maxPercent = category.scaleViewModel.maxPercentPrograms.get((int)
                        program.id);
                if (percent * 100 > maxPercent) {
                    category.scaleViewModel.maxPercentPrograms.put((int) program.id, roundUp(
                            (int)
                                    (percent * 100), 10));
                }
            }
            if (dayViewModel.rainAmount > maxRainAmount) {
                maxRainAmount = dayViewModel.rainAmount;
            }
        }
        category.scaleViewModel.maxDailyWaterNeedPercent = roundUp((int)
                maxPercentDailyWaterNeed, 10);
        // Set dome default limits if no data available
        if (maxValueTemperature == Integer.MIN_VALUE) {
            category.scaleViewModel.maxTemperature = viewModel.isUnitsMetric ? 50 : 100;
        } else {
            category.scaleViewModel.maxTemperature = roundUp(maxValueTemperature, 10);
        }
        if (minValueTemperature == Integer.MAX_VALUE) {
            category.scaleViewModel.minTemperature = 0;
        } else {
            category.scaleViewModel.minTemperature = roundDown(minValueTemperature, 10);
        }
        if (category.scaleViewModel.maxTemperature == category.scaleViewModel
                .minTemperature) {
            category.scaleViewModel.maxTemperature += 10;
            category.scaleViewModel.minTemperature -= 10;
        }
        category.scaleViewModel.maxRainAmount = roundUpMaxRainAmount(maxRainAmount, viewModel
                .isUnitsMetric);
    }

    private void computeMinMaxMonthYear(StatsViewModel viewModel, StatsDataCategory category,
                                        List<Program> programs) {
        category.scaleViewModel = new ScaleViewModel();
        float maxPercentDailyWaterNeed = 100f;
        int maxValueTemperature = Integer.MIN_VALUE;
        int minValueTemperature = Integer.MAX_VALUE;
        float maxRainAmount = viewModel.isUnitsMetric ? 5f : 0.2f;
        category.scaleViewModel.maxPercentPrograms = new SparseArray<>();
        for (Program program : programs) {
            category.scaleViewModel.maxPercentPrograms.put((int) program.id, 100);
        }
        for (int i = 0; i < category.numDays; i++) {
            LocalDate date = category.startDate.plusDays(i);
            StatsDayViewModel dayViewModel = category.days.get(date);
            if (dayViewModel.dailyWaterNeed * 100 > maxPercentDailyWaterNeed) {
                maxPercentDailyWaterNeed = dayViewModel.dailyWaterNeed * 100;
            }
            if (dayViewModel.maxTemperature != Integer.MIN_VALUE) {
                if (dayViewModel.maxTemperature > maxValueTemperature) {
                    maxValueTemperature = dayViewModel.maxTemperature;
                }
            }
            if (dayViewModel.minTemperature != Integer.MIN_VALUE) {
                if (dayViewModel.minTemperature < minValueTemperature) {
                    minValueTemperature = dayViewModel.minTemperature;
                }
            }
            for (Program program : viewModel.programs) {
                float percent = dayViewModel.programDailyWaterNeed.get((int) program.id);
                float maxPercent = category.scaleViewModel.maxPercentPrograms.get((int)
                        program.id);
                if (percent * 100 > maxPercent) {
                    category.scaleViewModel.maxPercentPrograms.put((int) program.id,
                            roundUp((int) (percent * 100), 10));
                }
            }
            if (dayViewModel.rainAmount > maxRainAmount) {
                maxRainAmount = dayViewModel.rainAmount;
            }
        }
        category.scaleViewModel.maxDailyWaterNeedPercent = roundUp((int)
                maxPercentDailyWaterNeed, 10);
        // Set dome default limits if no data available
        if (maxValueTemperature == Integer.MIN_VALUE) {
            category.scaleViewModel.maxTemperature = viewModel.isUnitsMetric ? 50 : 100;
        } else {
            category.scaleViewModel.maxTemperature = roundUp(maxValueTemperature, 10);
        }
        if (minValueTemperature == Integer.MAX_VALUE) {
            category.scaleViewModel.minTemperature = 0;
        } else {
            category.scaleViewModel.minTemperature = roundDown(minValueTemperature, 10);
        }
        if (category.scaleViewModel.maxTemperature == category.scaleViewModel
                .minTemperature) {
            category.scaleViewModel.maxTemperature += 10;
            category.scaleViewModel.minTemperature -= 10;
        }
        category.scaleViewModel.maxRainAmount = roundUpMaxRainAmount(maxRainAmount, viewModel
                .isUnitsMetric);
    }

    private int roundUp(int value, int multiple) {
        if (multiple == 0) {
            return value;
        }
        int remainder = Math.abs(value) % multiple;
        if (remainder == 0) {
            return value;
        }
        if (value < 0) {
            return -(Math.abs(value) - remainder);
        }
        return value + multiple - remainder;
    }

    private int roundDown(int value, int multiple) {
        if (multiple == 0) {
            return value;
        }
        int remainder = Math.abs(value) % multiple;
        if (remainder == 0) {
            return value;
        }
        if (value < 0) {
            return -(Math.abs(value) + multiple - remainder);
        }
        return value - remainder;
    }

    private float roundUpMaxRainAmount(float value, boolean isUnitsMetric) {
        if (isUnitsMetric) {
            int iValue = (int) value;
            return iValue + (value - iValue > 0 ? 1f : 0f);
        } else {
            int iValue = (int) (value * 10);
            return iValue / 10f + (value * 10 - iValue > 0 ? 0.1f : 0f);
        }
    }

    public StatsViewModel getLatestData() {
        return latestStatsViewModel;
    }

    Observable<DashboardGraphs> dashboardGraphsChanges() {
        return databaseRepository.dashboardGraphsByIdChanges();
    }

    void saveToDatabase(DashboardGraphs dashboardGraphs) {
        sprinklerRepository
                .saveDashboardGraphs(dashboardGraphs)
                .toObservable()
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
