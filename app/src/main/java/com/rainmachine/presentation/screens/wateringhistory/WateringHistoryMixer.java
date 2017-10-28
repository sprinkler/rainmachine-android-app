package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.WateringLogDetailsDay;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.MetricCalculator;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;

class WateringHistoryMixer {

    private static final String PATTERN_DATE_HUMAN = "EEE MMM dd";
    private static final String PATTERN_DATE_STANDARD = "yyyy-MM-dd";

    private Context context;
    private SprinklerRepositoryImpl sprinklerRepository;
    private Device device;
    private CalendarFormatter calendarFormatter;
    private DecimalFormatter decimalFormatter;
    private Features features;

    WateringHistoryMixer(Context context, SprinklerRepositoryImpl sprinklerRepository,
                         Device device, CalendarFormatter calendarFormatter,
                         DecimalFormatter decimalFormatter, Features features) {
        this.context = context;
        this.sprinklerRepository = sprinklerRepository;
        this.device = device;
        this.calendarFormatter = calendarFormatter;
        this.decimalFormatter = decimalFormatter;
        this.features = features;
    }

    public Observable<WateringHistoryViewModel> refresh() {
        return Single.zip(
                wateringLogDetails(),
                sprinklerRepository.programs(),
                sprinklerRepository.zonesProperties(),
                sprinklerRepository.devicePreferences(),
                (wateringLogDetailsDays, programs, zoneProperties, devicePreferences) ->
                        convertWateringHistoryData(wateringLogDetailsDays, programs,
                                zoneProperties, devicePreferences))
                .toObservable();
    }

    private Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogDetails() {
        final int NUM_DAYS_PER_BATCH = 15;
        LocalDate startDate = new LocalDate().minusDays(WateringHistoryConstants.NUM_PAST_DAYS -
                1); // we include today
        if (device.isCloud()) {
            return sprinklerRepository.parallelWateringLogDetails(startDate,
                    WateringHistoryConstants.NUM_PAST_DAYS, NUM_DAYS_PER_BATCH);
        } else {
            return sprinklerRepository.wateringLogDetails(WateringHistoryConstants
                    .NUM_PAST_DAYS, true);
        }
    }

    private WateringHistoryViewModel convertWateringHistoryData(
            Map<LocalDate, WateringLogDetailsDay> wateringLogDetailsDays,
            List<Program> programs, List<ZoneProperties> zonesProperties,
            DevicePreferences devicePreferences) {
        WateringHistoryViewModel viewModel = new WateringHistoryViewModel();
        viewModel.days = new ArrayList<>();
        LocalDate startDate = new LocalDate();
        for (int i = 0; i < WateringHistoryConstants.NUM_PAST_DAYS; i++) {
            LocalDate date = startDate.minusDays(i);
            WateringHistoryViewModel.Day dayData = new WateringHistoryViewModel.Day();
            dayData.date = date;
            dayData.programs = new ArrayList<>();

            WateringLogDetailsDay dayLog = wateringLogDetailsDays.get(date);
            if (dayLog != null) {
                for (WateringLogDetailsDay.Program program : dayLog.programs) {
                    WateringHistoryViewModel.Program programData = new
                            WateringHistoryViewModel.Program();
                    programData.id = program.id;
                    programData.zones = new ArrayList<>();
                    for (WateringLogDetailsDay.Program.Zone zone : program.zones) {
                        WateringHistoryViewModel.Zone zoneData = new
                                WateringHistoryViewModel.Zone();
                        zoneData.id = zone.id;
                        ZoneProperties zoneProperties = null;
                        for (ZoneProperties zoneProperties1 : zonesProperties) {
                            if (zoneProperties1.id == zone.id) {
                                zoneProperties = zoneProperties1;
                                zoneData.name = zoneProperties1.name;
                                break;
                            }
                        }
                        if (Strings.isBlank(zoneData.name)) {
                            zoneData.name = context.getString(R.string
                                    .watering_history_zone_unknown, zoneData.id);
                        }
                        zoneData.cycles = new ArrayList<>();
                        for (WateringLogDetailsDay.Program.Cycle cycle : zone.cycles) {
                            WateringHistoryViewModel.Cycle cycleData = new
                                    WateringHistoryViewModel.Cycle();
                            cycleData.scheduled = cycle.userDuration;
                            cycleData.watered = cycle.realDuration;
                            float waterUsedAmount = DomainUtils.computeWaterVolume
                                    (zoneProperties, cycleData.watered);
                            if (devicePreferences.isUnitsMetric) {
                                cycleData.waterUsedAmount = waterUsedAmount;
                            } else {
                                cycleData.waterUsedAmount = MetricCalculator.volumeMetersToGal
                                        (waterUsedAmount);
                            }

                            float waterScheduledAmount = DomainUtils.computeWaterVolume
                                    (zoneProperties, cycleData.scheduled);
                            if (devicePreferences.isUnitsMetric) {
                                cycleData.waterScheduledAmount = waterScheduledAmount;
                            } else {
                                cycleData.waterScheduledAmount = MetricCalculator.volumeMetersToGal
                                        (waterScheduledAmount);
                            }

                            cycleData.startTime = cycle.startTime;
                            zoneData.cycles.add(cycleData);
                        }
                        zoneData.totalScheduled = zone.totalUserDuration;
                        zoneData.totalWatered = zone.totalRealDuration;
                        if (features.hasSpecialCondition()) {
                            zoneData.specialCondition = mapSpecialCondition(zone.specialCondition);
                        } else {
                            zoneData.specialCondition = WateringHistoryViewModel.SpecialCondition
                                    .NONE;
                        }

                        float waterSavedAmount = DomainUtils.computeWaterVolume
                                (zoneProperties, zoneData.totalScheduled - zoneData.totalWatered);
                        if (devicePreferences.isUnitsMetric) {
                            zoneData.waterSavedAmount = waterSavedAmount;
                        } else {
                            zoneData.waterSavedAmount = MetricCalculator.volumeMetersToGal
                                    (waterSavedAmount);
                        }
                        programData.zones.add(zoneData);
                    }
                    for (Program localProgram : programs) {
                        if (localProgram.id == program.id) {
                            programData.name = localProgram.name;
                            break;
                        }
                    }
                    if (Strings.isBlank(programData.name)) {
                        if (programData.id == 0) {
                            programData.name = context.getString(R.string
                                    .watering_history_manual_watering);
                        } else {
                            programData.name = context.getString(R.string
                                    .watering_history_program_unknown, programData.id);
                        }
                    }
                    dayData.programs.add(programData);
                }
            }
            viewModel.days.add(dayData);
        }
        viewModel.use24HourFormat = devicePreferences.use24HourFormat;
        viewModel.isUnitsMetric = devicePreferences.isUnitsMetric;
        return viewModel;
    }

    Observable<Uri> createExportFile(final WateringHistoryViewModel viewModel, final
    WateringHistoryInterval wateringHistoryInterval) {
        return Observable
                .fromCallable(() -> {
                    DevicePreferences devicePreferences = sprinklerRepository
                            .devicePreferences().toObservable()
                            .blockingFirst();

                    List<WateringHistoryViewModel.Day> items;
                    String fileName;
                    if (wateringHistoryInterval == WateringHistoryInterval.WEEK) {
                        items = viewModel.days.subList(0, 7);
                        fileName = "watering_history_7_days.csv";
                    } else if (wateringHistoryInterval == WateringHistoryInterval.MONTH) {
                        items = viewModel.days.subList(0, 31);
                        fileName = "watering_history_31_days.csv";
                    } else {
                        items = viewModel.days.subList(0, WateringHistoryConstants.NUM_PAST_DAYS);
                        fileName = "watering_history_365_days.csv";
                    }
                    File dir = new File(context.getFilesDir(), "exports");
                    dir.mkdirs();
                    File file = new File(dir, fileName);
                    file.createNewFile();
                    generateCsvFile(file, items, devicePreferences);

                    return FileProvider.getUriForFile(context, InfrastructureUtils
                                    .FILE_PROVIDER_AUTHORITY,
                            file);
                });
    }

    private void generateCsvFile(File file, List<WateringHistoryViewModel.Day> days,
                                 DevicePreferences devicePreferences) throws IOException {
        FileWriter writer = new FileWriter(file);
        String units = context.getString(devicePreferences.isUnitsMetric ? R.string.all_m3 : R
                .string.all_gal);
        char separator = decimalFormatter.getDecimalSeparator() == ',' ? ';' : ',';
        writer.append(context.getString(R.string.watering_history_csv_date))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_date))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_program_name))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_zone_number))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_zone_name))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_cycle))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_time_scheduled))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_time_watered))
                .append(separator)
                .append(context.getString(R.string.watering_history_water_scheduled, units))
                .append(separator)
                .append(context.getString(R.string.watering_history_water_used, units))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_zone_start_time))
                .append(separator)
                .append(context.getString(R.string.watering_history_csv_zone_end_time))
                .append(separator);
        if (features.hasSpecialCondition()) {
            writer.append(context.getString(R.string.watering_history_csv_special_conditions))
                    .append(separator);
        }
        writer.append('\n');
        for (WateringHistoryViewModel.Day dayData : days) {
            if (dayData.programs.size() > 0) {
                for (WateringHistoryViewModel.Program programData : dayData.programs) {
                    for (WateringHistoryViewModel.Zone zoneData : programData.zones) {
                        int cycleNum = 1;
                        for (WateringHistoryViewModel.Cycle zoneCycle : zoneData.cycles) {
                            writer.append(dayData.date.toString(PATTERN_DATE_STANDARD, Locale
                                    .ENGLISH));
                            writer.append(separator);
                            writer.append(dayData.date.toString(PATTERN_DATE_HUMAN, Locale
                                    .ENGLISH));
                            writer.append(separator);
                            writer.append(programData.name);
                            writer.append(separator);
                            writer.append(Long.toString(zoneData.id));
                            writer.append(separator);
                            writer.append(zoneData.name);
                            writer.append(separator);
                            writer.append(Integer.toString(cycleNum));
                            writer.append(separator);
                            writer.append(calendarFormatter.hourMinSecColonFull(zoneCycle
                                    .scheduled));
                            writer.append(separator);
                            writer.append(calendarFormatter.hourMinSecColonFull(zoneCycle.watered));
                            writer.append(separator);
                            writer.append(decimalFormatter.limitedDecimals(zoneCycle
                                    .waterScheduledAmount, 2));
                            writer.append(separator);
                            writer.append(decimalFormatter.limitedDecimals(zoneCycle
                                    .waterUsedAmount, 2));
                            writer.append(separator);
                            writer.append(zoneCycle.startTime.toString(calendarFormatter
                                    .timeFormatWithSeconds(devicePreferences.isUnitsMetric)));
                            writer.append(separator);
                            writer.append(zoneCycle.getEndTime().toString(calendarFormatter
                                    .timeFormatWithSeconds(devicePreferences.isUnitsMetric)));
                            writer.append(separator);
                            if (features.hasSpecialCondition()) {
                                writer.append(specialConditionText(zoneData.specialCondition));
                                writer.append(separator);
                            }
                            writer.append('\n');
                            cycleNum++;
                        }
                    }
                }
                writer.append('\n');
            } else {
                writer.append(dayData.date.toString(PATTERN_DATE_STANDARD, Locale.ENGLISH));
                writer.append(separator);
                writer.append(dayData.date.toString(PATTERN_DATE_HUMAN, Locale.ENGLISH));
                writer.append(separator);
                writer.append(context.getString(R.string.watering_history_no_watering_data));
                writer.append(separator);
                writer.append(separator);
                writer.append(separator);
                writer.append(separator);
                writer.append(separator);
                writer.append(separator);
                writer.append(separator);
                if (features.hasSpecialCondition()) {
                    writer.append(separator);
                }
                writer.append(separator);
                writer.append('\n');
                writer.append('\n');
            }
        }

        writer.flush();
        writer.close();
    }

    private String specialConditionText(WateringHistoryViewModel.SpecialCondition condition) {
        switch (condition) {
            case NONE:
                return "";
            case STOPPED_BY_USER:
                return context.getString(R.string.watering_history_csv_condition_stopped_user);
            case MINIMUM_WATERING_TIME:
                return context.getString(R.string.watering_history_csv_condition_minimum_time);
            case FREEZE_PROTECT:
                return context.getString(R.string.watering_history_csv_condition_freeze_protect);
            case DAY_RESTRICTION:
                return context.getString(R.string.watering_history_csv_condition_day_restriction);
            case WATERING_REACHES_NEXT_DAY:
                return context.getString(R.string.watering_history_csv_condition_watering_next_day);
            case WATER_SURPLUS:
                return context.getString(R.string.watering_history_csv_condition_surplus);
            case RAIN_DETECTED:
                return context.getString(R.string.watering_history_csv_condition_rain_detected);
            case RAIN_SENSOR_RESTRICTION:
                return context.getString(R.string.watering_history_csv_condition_rain_sensor);
            case MONTH_RESTRICTION:
                return context.getString(R.string.watering_history_csv_condition_month_restriction);
            case RAIN_DELAY:
                return context.getString(R.string.watering_history_csv_condition_rain_delay);
            case PROGRAM_RAIN_RESTRICTION:
                return context.getString(R.string
                        .watering_history_csv_condition_program_rain_restriction);
        }
        return "";
    }

    private WateringHistoryViewModel.SpecialCondition mapSpecialCondition(WateringLogDetailsDay
                                                                                  .SpecialCondition condition) {
        if (condition == null) {
            return WateringHistoryViewModel.SpecialCondition.NONE;
        }
        switch (condition) {
            case NONE:
                return WateringHistoryViewModel.SpecialCondition.NONE;
            case STOPPED_BY_USER:
                return WateringHistoryViewModel.SpecialCondition.STOPPED_BY_USER;
            case MINIMUM_WATERING_TIME:
                return WateringHistoryViewModel.SpecialCondition.MINIMUM_WATERING_TIME;
            case FREEZE_PROTECT:
                return WateringHistoryViewModel.SpecialCondition.FREEZE_PROTECT;
            case DAY_RESTRICTION:
                return WateringHistoryViewModel.SpecialCondition.DAY_RESTRICTION;
            case WATERING_REACHES_NEXT_DAY:
                return WateringHistoryViewModel.SpecialCondition.WATERING_REACHES_NEXT_DAY;
            case WATER_SURPLUS:
                return WateringHistoryViewModel.SpecialCondition.WATER_SURPLUS;
            case RAIN_DETECTED:
                return WateringHistoryViewModel.SpecialCondition.RAIN_DETECTED;
            case RAIN_SENSOR_RESTRICTION:
                return WateringHistoryViewModel.SpecialCondition.RAIN_SENSOR_RESTRICTION;
            case MONTH_RESTRICTION:
                return WateringHistoryViewModel.SpecialCondition.MONTH_RESTRICTION;
            case RAIN_DELAY:
                return WateringHistoryViewModel.SpecialCondition.RAIN_DELAY;
            case PROGRAM_RAIN_RESTRICTION:
                return WateringHistoryViewModel.SpecialCondition.PROGRAM_RAIN_RESTRICTION;
        }
        return WateringHistoryViewModel.SpecialCondition.NONE;
    }
}
