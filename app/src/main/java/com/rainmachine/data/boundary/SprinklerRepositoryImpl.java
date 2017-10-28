package com.rainmachine.data.boundary;

import com.rainmachine.data.cache.CacheData;
import com.rainmachine.data.cache.CacheEntryKey;
import com.rainmachine.data.cache.CacheManager;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.mapper.SprinklerSettingsMapper;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.database.model.DeviceSettings;
import com.rainmachine.data.local.database.model.SprinklerSettings;
import com.rainmachine.data.local.database.model.WateringLogs;
import com.rainmachine.data.local.database.model.ZoneSettings;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApiDelegate3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ProgramsResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ZonesPropertiesResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v4.SprinklerApiDelegate;
import com.rainmachine.data.remote.sprinkler.v4.mapper.GlobalRestrictionsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.HourlyRestrictionsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ProgramsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZonesPropertiesResponseMapper;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer3;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.boundary.infrastructure.Analytics;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.model.CloudSettings;
import com.rainmachine.domain.model.CurrentActiveRestrictions;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.model.DayStatsDetails;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.Diagnostics;
import com.rainmachine.domain.model.DiagnosticsUploadStatus;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.model.Mixer;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.model.TimeDate3;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.Versions;
import com.rainmachine.domain.model.WateringLogDetailsDay;
import com.rainmachine.domain.model.WateringQueueItem;
import com.rainmachine.domain.model.WifiScan;
import com.rainmachine.domain.model.WifiSettings;
import com.rainmachine.domain.model.WifiSettingsSimple;
import com.rainmachine.domain.model.Zone;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.model.ZoneSimulation;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.SprinklerUtils;

import org.javatuples.Pair;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import timber.log.Timber;

public class SprinklerRepositoryImpl implements SprinklerRepository {

    private final SprinklerApiDelegate sprinklerApiDelegate;
    private final SprinklerApiDelegate3 sprinklerApiDelegate3;
    private final DatabaseRepositoryImpl databaseRepository;
    private final Device device;
    private final Features features;
    private final SprinklerUtils sprinklerUtils;
    private final CacheManager cacheManager;
    private final SprinklerPrefRepositoryImpl sprinklerPrefsRepository;
    private final CrashReporter crashReporter;
    private final Analytics analytics;
    private final BackupRepositoryImpl backupRepository;
    private final SprinklerRemoteErrorTransformer sprinklerRemoteErrorTransformer;
    private final SprinklerRemoteErrorTransformer3 sprinklerRemoteErrorTransformer3;

    public SprinklerRepositoryImpl(SprinklerApiDelegate sprinklerApiDelegate,
                                   SprinklerApiDelegate3 sprinklerApiDelegate3,
                                   DatabaseRepositoryImpl databaseRepository,
                                   Device device, Features features,
                                   SprinklerUtils sprinklerUtils,
                                   CacheManager cacheManager,
                                   SprinklerPrefRepositoryImpl sprinklerPrefsRepository,
                                   CrashReporter crashReporter, Analytics analytics,
                                   BackupRepositoryImpl backupRepository,
                                   SprinklerRemoteErrorTransformer
                                           sprinklerRemoteErrorTransformer,
                                   SprinklerRemoteErrorTransformer3
                                           sprinklerRemoteErrorTransformer3) {
        this.sprinklerApiDelegate = sprinklerApiDelegate;
        this.sprinklerApiDelegate3 = sprinklerApiDelegate3;
        this.databaseRepository = databaseRepository;
        this.device = device;
        this.features = features;
        this.sprinklerUtils = sprinklerUtils;
        this.cacheManager = cacheManager;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        this.crashReporter = crashReporter;
        this.analytics = analytics;
        this.backupRepository = backupRepository;
        this.sprinklerRemoteErrorTransformer = sprinklerRemoteErrorTransformer;
        this.sprinklerRemoteErrorTransformer3 = sprinklerRemoteErrorTransformer3;
    }

    private Single<SprinklerSettings> sprinklerSettings() {
        return Single.fromCallable(() -> databaseRepository.sprinklerSettings(device));
    }

    @Override
    public Single<DevicePreferences> devicePreferences() {
        return sprinklerSettings()
                .map(SprinklerSettingsMapper.instance());
    }

    private Single<Irrelevant> saveLocalUnits(boolean isUnitsMetric) {
        return Single.fromCallable(() -> {
            databaseRepository.updateSprinklerSettingsUnits(device, isUnitsMetric);
            return Irrelevant.INSTANCE;
        });
    }

    public Single<DeviceSettings> deviceSettings() {
        return databaseRepository.deviceSettings(device.deviceId);
    }

    public Observable<DeviceSettings> deviceSettingsLive() {
        return databaseRepository.deviceSettingsChanges(device.deviceId);
    }

    public Single<Irrelevant> saveZoneSettings(final ZoneSettings zoneSettings) {
        return deviceSettings()
                .flatMap(deviceSettings -> {
                    deviceSettings.zones.put(zoneSettings.zoneId, zoneSettings);
                    databaseRepository.saveDeviceSettings(deviceSettings);
                    return Single.just(Irrelevant.INSTANCE);
                });
    }

    public Single<DashboardGraphs> dashboardGraphs() {
        return Single.fromCallable(() -> {
            DashboardGraphs dashboardGraphs = databaseRepository.dashboardGraphsById
                    (device.deviceId);
            if (dashboardGraphs == null) {
                dashboardGraphs = new DashboardGraphs();
                dashboardGraphs.deviceId = device.deviceId;
                dashboardGraphs.graphs = new ArrayList<>();
                dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                        (DashboardGraphs.GraphType.WEATHER, true));
                dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                        (DashboardGraphs.GraphType.TEMPERATURE, true));
                dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                        (DashboardGraphs.GraphType.RAIN_AMOUNT, true));
                if (features.hasDailyWaterNeedChart()) {
                    dashboardGraphs.graphs.add(new DashboardGraphs.DashboardGraph
                            (DashboardGraphs.GraphType.DAILY_WATER_NEED, true));
                }
            }
            return dashboardGraphs;
        });
    }

    public Single<Irrelevant> saveDashboardGraphs(final DashboardGraphs dashboardGraphs) {
        return Single.fromCallable(() -> {
            databaseRepository.saveDashboardGraphs(dashboardGraphs);
            return Irrelevant.INSTANCE;
        });
    }

    /* ----- */
    @Override
    public Single<Boolean> testApiAuthenticated() {
        return sprinklerApiDelegate.testApiAuthenticated();
    }

    public Single<Boolean> testApiFunctional() {
        return sprinklerApiDelegate.testApiFunctional();
    }

    @Override
    public Single<CurrentActiveRestrictions> currentRestrictions() {
        return sprinklerApiDelegate.currentRestrictions();
    }

    @Override
    public Single<LocalDateTime> timeDate() {
        return sprinklerApiDelegate.timeDate();
    }

    @Override
    public Single<Long> rainDelay() {
        return sprinklerApiDelegate.rainDelay();
    }

    public Single<Irrelevant> saveRainDelay(final int rainDelayValue) {
        return sprinklerApiDelegate.saveRainDelay(rainDelayValue);
    }

    public Single<String> generateSupportPin() {
        return sprinklerApiDelegate.generateSupportPin();
    }

    public Single<Irrelevant> resetCloudCertificates() {
        return sprinklerApiDelegate.resetCloudCertificates();
    }

    public Single<Irrelevant> reboot() {
        return sprinklerApiDelegate.reboot();
    }

    public Single<Irrelevant> saveTimeDate(final LocalDate date, LocalTime time) {
        return sprinklerApiDelegate.saveTimeDate(date, time);
    }

    public Single<Boolean> betaUpdates() {
        Maybe<CacheData<Boolean>> memory = Single
                .fromCallable(() -> cacheManager.<Boolean>get(CacheEntryKey.BETA_UPDATES))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY BETA UPDATES"));
        Maybe<CacheData<Boolean>> network = betaUpdatesNetwork()
                .map(enabled -> cacheManager.saveBetaUpdatesEnabled(enabled))
                .toMaybe()
                .compose(logSource("NETWORK BETA UPDATES"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<Boolean> betaUpdatesNetwork() {
        return sprinklerApiDelegate.betaUpdates();
    }

    public Single<Irrelevant> saveBetaUpdates(boolean isEnabled) {
        return sprinklerApiDelegate.saveBetaUpdates(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.BETA_UPDATES));
    }

    public Single<Irrelevant> saveSshAccess(boolean isEnabled) {
        return sprinklerApiDelegate.saveSshAccess(isEnabled);
    }

    @Override
    public Single<CloudSettings> cloudSettings() {
        return sprinklerApiDelegate
                .cloudSettings()
                .doOnSuccess(cloudSettings -> {
                    String email = Strings.valueOrDefault(cloudSettings.email, cloudSettings
                            .pendingEmail);
                    crashReporter.logUserEmail(email);
                });
    }

    @Override
    public Completable enableCloud(boolean enable) {
        return sprinklerApiDelegate.enableCloud(enable).toCompletable();
    }

    @Override
    public Completable saveCloudEmail(String email) {
        return sprinklerApiDelegate.saveCloudEmail(email).toCompletable();
    }

    public Single<Versions> versions() {
        return sprinklerApiDelegate
                .versions()
                .doOnSuccess(versions -> {
                    sprinklerPrefsRepository.saveApiVersion(versions.apiVersion);
                    sprinklerPrefsRepository.saveSoftwareVersion(versions.softwareVersion);
                    sprinklerPrefsRepository.saveHardwareVersion(versions.hardwareVersion);
                    String s = "Api version: " + sprinklerPrefsRepository.apiVersion() + " " +
                            "Software version: " + sprinklerPrefsRepository.softwareVersion()
                            + " Hardware version: " + sprinklerPrefsRepository
                            .hardwareVersion();
                    Timber.d(s);
                    analytics.trackDevice(device.deviceId, versions.apiVersion, versions
                            .softwareVersion, versions.hardwareVersion);
                    crashReporter.logDeviceVersion(s);
                });
    }

    public Single<Boolean> checkDefaultPassword() {
        return sprinklerApiDelegate.checkDefaultPassword();
    }

    @Override
    public Single<LoginStatus> login(final String pass, final boolean isRemember) {
        return sprinklerApiDelegate
                .login(pass, isRemember)
                .doOnSuccess(loginResponse -> {
                    String accessToken = loginResponse.accessToken;
                    sprinklerPrefsRepository.saveSessionCookie(accessToken);
                })
                .map(login -> login.status);
    }

    public Single<Irrelevant> changePassword(final String oldPass, final String newPass) {
        return sprinklerApiDelegate
                .changePassword(oldPass, newPass)
                .doOnSuccess(accessToken -> sprinklerPrefsRepository.saveSessionCookie(accessToken))
                .map(accessToken -> Irrelevant.INSTANCE);
    }

    public Single<ZoneSimulation> simulateZone(final ZoneProperties zoneProperties) {
        return sprinklerApiDelegate.simulateZone(zoneProperties);
    }

    @Override
    public Single<Integer> numberOfZones() {
        if (features.useNewApi()) {
            return zonesProperties()
                    .map(zoneProperties -> zoneProperties.size());
        } else {
            return zonesProperties3()
                    .map(zoneProperties -> zoneProperties.size());
        }
    }

    @Override
    public Single<List<Zone>> zones() {
        return sprinklerApiDelegate.zones();
    }

    public Single<Zone> zone(long zoneId) {
        return sprinklerApiDelegate.zone(zoneId);
    }

    public Single<Irrelevant> startZone(final long zoneId, final int seconds) {
        return sprinklerApiDelegate.startZone(zoneId, seconds);
    }

    public Single<Irrelevant> stopZone(final long zoneId) {
        return sprinklerApiDelegate.stopZone(zoneId);
    }

    public Single<Irrelevant> stopWatering() {
        return sprinklerApiDelegate.stopWatering();
    }

    @Override
    public Single<List<WateringQueueItem>> wateringQueue() {
        return sprinklerApiDelegate.wateringQueue();
    }

    @Override
    public Single<WifiSettingsSimple> wifiSettings() {
        return sprinklerApiDelegate.wifiSettings();
    }

    @Override
    public Single<Update> update(boolean dealWithError) {
        return sprinklerApiDelegate.update(dealWithError);
    }

    @Override
    public Completable triggerUpdateCheck() {
        return sprinklerApiDelegate.triggerUpdateCheck().toCompletable();
    }

    @Override
    public Single<Irrelevant> triggerUpdate() {
        return sprinklerApiDelegate.triggerUpdate();
    }

    public Single<List<WifiScan>> wifiScan() {
        return sprinklerApiDelegate.wifiScan();
    }

    public Single<Diagnostics> diagnostics() {
        return sprinklerApiDelegate.diagnostics();
    }

    public Single<Irrelevant> saveLogLevel(int level) {
        return sprinklerApiDelegate.saveLogLevel(level);
    }

    @Override
    public Single<List<Parser>> parsers() {
        return sprinklerApiDelegate
                .parsers()
                .flatMapObservable(parsers -> Observable.fromIterable(parsers))
                .filter(parser -> features.hasParserFullFunctionality() || (parser.isNOAA() ||
                        parser.isMETNO()))
                .filter(parser -> !parser.shouldBeHidden())
                .toList();
    }

    public Single<Parser> parser(long uid) {
        return sprinklerApiDelegate.parser(uid);
    }

    public Single<Irrelevant> saveParserParams(final Parser parser) {
        return sprinklerApiDelegate.saveParserParams(parser);
    }

    public Single<Irrelevant> saveParserEnabled(long parserId, boolean isEnabled) {
        return sprinklerApiDelegate.saveParserEnabled(parserId, isEnabled);
    }

    public Single<Irrelevant> addParser(String filename, File file) {
        return sprinklerApiDelegate.addParser(filename, file);
    }

    public Single<Irrelevant> runParser(long id) {
        return sprinklerApiDelegate.runParser(id);
    }

    @Override
    public Completable runAllParsers() {
        return sprinklerApiDelegate.runAllParsers().toCompletable();
    }

    public Single<Irrelevant> setParserDefaults(long id) {
        return sprinklerApiDelegate.setParserDefaults(id);
    }

    public Single<Irrelevant> enableLightLEDs(boolean enable) {
        return sprinklerApiDelegate.enableLightLEDs(enable);
    }

    public Single<Irrelevant> saveWifiSettings(final WifiSettings wifiSettings) {
        return sprinklerApiDelegate.saveWifiSettings(wifiSettings);
    }

    public Single<Map<LocalDate, Mixer>> mixers(int numPastDays, int numFutureDays) {
        return sprinklerApiDelegate.mixers(numPastDays, numFutureDays);
    }

    public Single<Map<LocalDate, DayStatsDetails>> statsDetails() {
        return sprinklerApiDelegate.statsDetails();
    }

    public Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogDetails(int numPastDays,
                                                                            boolean includeToday) {
        LocalDate startDate = new LocalDate().minusDays(includeToday ? numPastDays - 1 :
                numPastDays);
        return wateringLogDetails(startDate, numPastDays);
    }

    public Single<Map<LocalDate, WateringLogDetailsDay>> parallelWateringLogDetails(final
                                                                                    LocalDate
                                                                                            startDate,
                                                                                    final int
                                                                                            numDays,
                                                                                    final int
                                                                                            numDaysPerBatch) {
        return databaseRepository
                .wateringLogs(device.deviceId)
                .flatMap(wateringLogs -> {
                    List<Single<Map<LocalDate, WateringLogDetailsDay>>> list = new
                            ArrayList<>();
                    LocalDate date = startDate;
                    if (wateringLogs != null && !date.isBefore(wateringLogs.startDate) &&
                            !date.isAfter(wateringLogs.endDate)) {
                        // Ignore last date because it may be today and we need to fetch
                        // again because new watering may have happened
                        date = wateringLogs.endDate;
                        wateringLogs.days.remove(date);
                        // WateringLogs may contain older data than startDate but it's OK
                        // because screen logic does not ask for this data
                        Timber.d("Add observable for watering logs and date becomes %s", date
                                .toString());
                        list.add(Single.just(wateringLogs.days));
                    }

                    LocalDate endDate = startDate.plusDays(numDays);
                    int days;
                    while (date.isBefore(endDate)) {
                        days = Math.min(numDaysPerBatch, Days.daysBetween(date, endDate)
                                .getDays());
                        Timber.d("Add observable starting from %s for %d days", date.toString
                                (), days);
                        list.add(wateringLogDetails(date, days));
                        date = date.plusDays(days);
                    }
                    return Single
                            .zip(list, args -> {
                                Map<LocalDate, WateringLogDetailsDay> map = new HashMap<>();
                                for (Object arg : args) {
                                    @SuppressWarnings("unchecked")
                                    Map<LocalDate, WateringLogDetailsDay> map1 = (Map<LocalDate,
                                            WateringLogDetailsDay>) arg;
                                    map.putAll(map1);
                                }
                                return map;
                            })
                            .doOnSuccess(days1 -> {
                                WateringLogs wateringLogs1 = new WateringLogs();
                                wateringLogs1.deviceId = device.deviceId;
                                wateringLogs1.startDate = startDate;
                                wateringLogs1.endDate = startDate.plusDays(numDays - 1);
                                wateringLogs1.days = days1;
                                databaseRepository.saveWateringLogs(wateringLogs1);
                            });
                });
    }

    private Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogDetails(LocalDate
                                                                                     startDate,
                                                                             int numDays) {
        return sprinklerApiDelegate.wateringLogDetails(startDate, numDays);
    }

    public Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogSimulatedDetails(int numDays) {
        return sprinklerApiDelegate.wateringLogSimulatedDetails(numDays);
    }

    public Single<Irrelevant> sendDiagnostics() {
        return sprinklerApiDelegate.sendDiagnostics();
    }

    public Single<DiagnosticsUploadStatus> getDiagnosticsUpload() {
        return sprinklerApiDelegate.getDiagnosticsUpload();
    }

    @Override
    public Single<Provision> provision() {
        Maybe<CacheData<Provision>> memory = Single
                .fromCallable(() -> cacheManager.<Provision>get(CacheEntryKey.PROVISION))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY PROVISION"));
        Maybe<CacheData<Provision>> network = provisionNetwork()
                .map(provision -> cacheManager.save(provision))
                .toMaybe()
                .compose(logSource("NETWORK PROVISION"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<Provision> provisionNetwork() {
        return sprinklerApiDelegate
                .provision()
                .doOnSuccess(provision -> {
                    // We need to update the flag because we might have gone through the
                    // initial setup and finished it
                    device.wizardHasRun = provision.system.wizardHasRun;

                    if (features.hasDeviceUnits()) {
                        if (sprinklerPrefsRepository.transitionedDeviceUnits()) {
                            databaseRepository.updateSprinklerSettingsUnits(device, provision.system
                                    .uiUnitsMetric);
                        } else {
                            sprinklerSettings()
                                    .flatMap(sprinklerSettings -> saveUnits(sprinklerSettings
                                            .isUnitsMetric()))
                                    .doOnSuccess(irrelevant -> sprinklerPrefsRepository
                                            .saveTransitionedDeviceUnits(true))
                                    .onErrorReturnItem(Irrelevant.INSTANCE)
                                    .subscribe();
                        }
                    }
                });
    }

    public Single<Irrelevant> saveUnits(boolean isUnitsMetric) {
        if (features.hasDeviceUnits()) {
            return sprinklerApiDelegate
                    .saveUnits(isUnitsMetric)
                    .doOnSuccess(irrelevant -> {
                        cacheManager.invalidate(CacheEntryKey.PROVISION);
                        databaseRepository.updateSprinklerSettingsUnits(device, isUnitsMetric);
                    });
        } else {
            return saveLocalUnits(isUnitsMetric);
        }
    }

    public Single<Irrelevant> saveRainSensor(boolean enabled) {
        return sprinklerApiDelegate
                .saveRainSensor(enabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveRainSensorSnoozeDuration(Provision.RainSensorSnoozeDuration
                                                                   snoozeDuration) {
        return sprinklerApiDelegate
                .saveRainSensorSnoozeDuration(snoozeDuration)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveLocation(final LocationInfo location, final String
            timezone, final double elevation) {
        return sprinklerApiDelegate
                .saveLocation(location, timezone, elevation)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    @Override
    public Completable saveZoneDurations(List<Long> zoneDurations) {
        return sprinklerApiDelegate
                .saveZoneDurations(zoneDurations)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION))
                .toCompletable();
    }

    public Single<Irrelevant> setProvisionMasterValve(boolean enabled) {
        return sprinklerApiDelegate
                .setProvisionMasterValve(enabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveTimezone(final String timezone) {
        return sprinklerApiDelegate
                .saveTimezone(timezone)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveMinWateringDurationThreshold(int threshold) {
        return sprinklerApiDelegate
                .saveMinWateringDurationThreshold(threshold)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveMaxWateringCoefficient(float value) {
        return sprinklerApiDelegate
                .saveMaxWateringCoefficient(value)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveProvisionDefaults(int minWateringDurationThreshold, float
            maxWateringCoefficient) {
        return sprinklerApiDelegate
                .saveProvisionDefaults(minWateringDurationThreshold, maxWateringCoefficient)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveAmazonAlexa(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveAmazonAlexa(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveBonjourService(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveBonjourService(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveUseCorrection(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveUseCorrection(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveTouchProgramToRun(int programId) {
        return sprinklerApiDelegate
                .saveTouchProgramToRun(programId)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveTouchAdvanced(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveTouchAdvanced(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Completable saveRestrictionsOnLed(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveRestrictionsOnLed(isEnabled)
                .doOnComplete(() -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveMinLedBrightness(int value) {
        return sprinklerApiDelegate
                .saveMinLedBrightness(value)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveMaxLedBrightness(int value) {
        return sprinklerApiDelegate
                .saveMaxLedBrightness(value)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveTouchSleepTimeout(int value) {
        return sprinklerApiDelegate
                .saveTouchSleepTimeout(value)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveTouchLongPressTimeout(int value) {
        return sprinklerApiDelegate
                .saveTouchLongPressTimeout(value)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveRainSensorNormallyClosed(boolean isEnabled) {
        return sprinklerApiDelegate
                .saveRainSensorNormallyClosed(isEnabled)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveWindSensitivity(float windSensitivity) {
        return sprinklerApiDelegate
                .saveWindSensitivity(windSensitivity)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveWeatherSensitivity(float rainSensitivity, int wsDays, float
            windSensitivity) {
        return sprinklerApiDelegate
                .saveWeatherSensitivity(rainSensitivity, wsDays, windSensitivity)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> saveRainSensitivity(final float rainSensitivity) {
        return sprinklerApiDelegate
                .saveRainSensitivity(rainSensitivity)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROVISION));
    }

    public Single<Irrelevant> resetProvision() {
        return sprinklerApiDelegate
                .resetProvision()
                .doOnSuccess(irrelevant -> cacheManager.invalidateAll());
    }

    public Single<String> deviceName() {
        Maybe<CacheData<String>> memory = Single
                .fromCallable(() -> cacheManager.<String>get(CacheEntryKey.DEVICE_NAME))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY DEVICE NAME"));
        Maybe<CacheData<String>> network = deviceNameNetwork()
                .map(deviceName -> cacheManager.save(deviceName))
                .toMaybe()
                .compose(logSource("NETWORK DEVICE NAME"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<String> deviceNameNetwork() {
        return sprinklerApiDelegate
                .deviceName()
                .doOnSuccess(deviceName -> backupRepository.updateBackupIfPossible(deviceName))
                .doOnSuccess(deviceName -> crashReporter.logDeviceName(deviceName));
    }

    public Single<Irrelevant> saveDeviceName(final String deviceName) {
        return sprinklerApiDelegate
                .saveDeviceName(deviceName)
                .doOnSuccess(baseResponse -> backupRepository.updateBackupIfPossible(deviceName))
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.DEVICE_NAME));
    }

    public Single<List<ZoneProperties>> zonesProperties() {
        Maybe<CacheData<List<ZoneProperties>>> memory = Single
                .fromCallable(() -> cacheManager.<List<ZoneProperties>>get(CacheEntryKey
                        .ZONES_PROPERTIES))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY ZONES PROPERTIES"));
        Maybe<CacheData<List<ZoneProperties>>> network = zonesPropertiesNetwork()
                .map(zoneProperties -> cacheManager.save(zoneProperties))
                .toMaybe()
                .compose(logSource("NETWORK ZONES PROPERTIES"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<List<ZoneProperties>> zonesPropertiesNetwork() {
        return sprinklerApiDelegate
                .zonesProperties()
                .doOnSuccess(response -> backupRepository.updateBackupIfPossible(response))
                .map(ZonesPropertiesResponseMapper.instance())
                .compose(dealWithError());
    }

    @Override
    public Completable saveZoneProperties(final ZoneProperties zoneProperties) {
        return sprinklerApiDelegate
                .saveZoneProperties(zoneProperties)
                .doOnSuccess(baseResponse -> backupRepository.markZonesPropertiesNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.ZONES_PROPERTIES))
                .toCompletable();
    }

    public Single<ZoneProperties> zoneProperties(long zoneId) {
        return sprinklerApiDelegate.zoneProperties(zoneId);
    }

    @Override
    public Single<GlobalRestrictions> globalRestrictions() {
        Maybe<CacheData<GlobalRestrictions>> memory = Single
                .fromCallable(() -> cacheManager.<GlobalRestrictions>get(CacheEntryKey
                        .GLOBAL_RESTRICTIONS))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY GLOBAL RESTRICTIONS"));
        Maybe<CacheData<GlobalRestrictions>> network = globalRestrictionsNetwork()
                .map(restrictions -> cacheManager.save(restrictions))
                .toMaybe()
                .compose(logSource("NETWORK GLOBAL RESTRICTIONS"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<GlobalRestrictions> globalRestrictionsNetwork() {
        return sprinklerApiDelegate
                .globalRestrictions()
                .doOnSuccess(response -> backupRepository.updateBackupIfPossible(response))
                .map(GlobalRestrictionsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveGlobalRestrictions(final GlobalRestrictions restrictions) {
        return sprinklerApiDelegate
                .saveGlobalRestrictions(restrictions)
                .doOnSuccess(baseResponse -> backupRepository.markGlobalRestrictionsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey
                        .GLOBAL_RESTRICTIONS));
    }

    @Override
    public Completable saveGlobalRestrictionsRaw(final String body) {
        return sprinklerApiDelegate
                .saveGlobalRestrictionsRaw(body)
                .doOnSuccess(irrelevant ->
                        cacheManager.invalidate(CacheEntryKey.GLOBAL_RESTRICTIONS))
                .toCompletable();
    }

    public Single<Irrelevant> saveRainDelayRestriction(int numSeconds) {
        return sprinklerApiDelegate
                .saveRainDelayRestriction(numSeconds)
                .doOnSuccess(response -> backupRepository.markGlobalRestrictionsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey
                        .GLOBAL_RESTRICTIONS));
    }

    @Override
    public Single<List<HourlyRestriction>> hourlyRestrictions() {
        Maybe<CacheData<List<HourlyRestriction>>> memory = Single
                .fromCallable(() -> cacheManager.<List<HourlyRestriction>>get(CacheEntryKey
                        .HOURLY_RESTRICTIONS))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY HOURLY RESTRICTIONS"));
        Maybe<CacheData<List<HourlyRestriction>>> network = hourlyRestrictionsNetwork()
                .map(hourlyRestrictions -> cacheManager.saveRestrictions(hourlyRestrictions))
                .toMaybe()
                .compose(logSource("NETWORK HOURLY RESTRICTIONS"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<List<HourlyRestriction>> hourlyRestrictionsNetwork() {
        return sprinklerApiDelegate
                .hourlyRestrictions()
                .doOnSuccess(response -> backupRepository.updateBackupIfPossible(response))
                .map(HourlyRestrictionsResponseMapper.instance())
                .compose(dealWithError());
    }

    @Override
    public Completable saveHourlyRestriction(final HourlyRestriction restriction) {
        return sprinklerApiDelegate
                .saveHourlyRestriction(restriction)
                .doOnSuccess(response -> backupRepository.markHourlyRestrictionsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey
                        .HOURLY_RESTRICTIONS))
                .toCompletable();
    }

    public Completable updateHourlyRestriction(HourlyRestriction restriction) {
        return sprinklerApiDelegate
                .updateHourlyRestriction(restriction)
                .doOnSuccess(response -> backupRepository.markHourlyRestrictionsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey
                        .HOURLY_RESTRICTIONS))
                .toCompletable();
    }

    @Override
    public Completable deleteHourlyRestriction(long restrictionId) {
        return sprinklerApiDelegate
                .deleteHourlyRestriction(restrictionId)
                .doOnSuccess(baseResponse -> backupRepository.markHourlyRestrictionsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey
                        .HOURLY_RESTRICTIONS))
                .toCompletable();
    }

    @Override
    public Single<List<Program>> programs() {
        return programs(false);
    }

    public Single<List<Program>> programs(boolean forceNetwork) {
        if (forceNetwork) {
            return programsNetwork();
        }
        Maybe<CacheData<List<Program>>> memory = Single
                .fromCallable(() -> cacheManager.<List<Program>>get(CacheEntryKey.PROGRAMS))
                .filter(data -> data != CacheData.NOT_FOUND && data.isUpToDate(device.isLocal()))
                .compose(logSource("MEMORY PROGRAMS"));
        Maybe<CacheData<List<Program>>> network = programsNetwork()
                .map(programs -> cacheManager.savePrograms(programs))
                .toMaybe()
                .compose(logSource("NETWORK PROGRAMS"));
        return Maybe
                .concat(memory, network)
                .compose(selectSource())
                .singleOrError();
    }

    private Single<List<Program>> programsNetwork() {
        return sprinklerApiDelegate
                .programs()
                .doOnSuccess(programsResponse ->
                        backupRepository.updateBackupIfPossible(programsResponse))
                .map(ProgramsResponseMapper.instance())
                .compose(dealWithError());
    }

    @Override
    public Completable createProgram(Program program) {
        return sprinklerApiDelegate
                .createProgram(program)
                .doOnSuccess(baseResponse -> backupRepository.markProgramsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROGRAMS))
                .toCompletable();
    }

    @Override
    public Completable updateProgram(final Program program) {
        return sprinklerApiDelegate
                .updateProgram(program)
                .doOnSuccess(baseResponse -> backupRepository.markProgramsNeedsUpdate())
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROGRAMS))
                .toCompletable();
    }

    public Single<Irrelevant> startProgram(long id) {
        return sprinklerApiDelegate
                .startProgram(id)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROGRAMS));
    }

    public Single<Irrelevant> stopProgram(long id) {
        return sprinklerApiDelegate
                .stopProgram(id)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROGRAMS));
    }

    @Override
    public Completable deleteProgram(long id) {
        return sprinklerApiDelegate
                .deleteProgram(id)
                .doOnSuccess(irrelevant -> cacheManager.invalidate(CacheEntryKey.PROGRAMS))
                .toCompletable();
    }

    private <T> FlowableTransformer<CacheData<T>, T> selectSource() {
        return dataObservable -> dataObservable
                .firstOrError()
                .map(cacheData -> cacheData.getData())
                .toFlowable();
    }

    // Simple logging to let us know what each source is returning
    private <T> MaybeTransformer<CacheData<T>, CacheData<T>> logSource(final String source) {
        return dataObservable -> dataObservable.doOnSuccess(data -> {
            Timber.d("%s has the data you are looking for!", source);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) sprinklerRemoteErrorTransformer;
    }

    /**
     * API 3
     **/
    public Single<Boolean> login3(final String username, final String pass, final boolean
            isRemember) {
        return sprinklerApiDelegate3
                .login(username, pass, isRemember)
                .doOnSuccess(cookie -> {
                    if (!Strings.isBlank(cookie)) {
                        sprinklerPrefsRepository.saveSessionCookie(cookie);
                    }
                })
                .map(cookie -> !Strings.isBlank(cookie));
    }

    public Single<Irrelevant> changePassword3(final String oldPass, final String newPass) {
        return sprinklerApiDelegate3
                .changePassword(oldPass, newPass)
                .doOnSuccess(irrelevant -> {
                    // Now the session expired because we have changed the password
                    sprinklerUtils.dealWithSessionExpiration();
                });
    }

    public Single<List<DayStats>> weatherData3() {
        return sprinklerApiDelegate3.weatherData();
    }

    @Override
    public Single<Update> update3(boolean dealWithError) {
        return sprinklerApiDelegate3.update(dealWithError);
    }

    @Override
    public Single<Irrelevant> triggerUpdate3() {
        return sprinklerApiDelegate3.triggerUpdate();
    }

    @Override
    public Single<Pair<List<Program>, Boolean>> programs3() {
        return sprinklerApiDelegate3
                .programs()
                .doOnSuccess(response -> backupRepository.updateBackupIfPossible(response))
                .map(ProgramsResponseMapper3.instance())
                .compose(dealWithError3());
    }

    @Override
    public Completable createUpdateProgram3(Program program, boolean use24HourFormat) {
        return sprinklerApiDelegate3
                .createUpdateProgram(program, use24HourFormat)
                .doOnSuccess(irrelevant -> backupRepository.markProgramsNeedsUpdate())
                .toCompletable();
    }

    public Completable copyProgram3(Program program, boolean use24HourFormat) {
        program.id = -1;
        return createUpdateProgram3(program, use24HourFormat);
    }

    public Single<Irrelevant> runStopProgram3(long id) {
        return sprinklerApiDelegate3.runStopProgram(id);
    }

    @Override
    public Completable deleteProgram3(long id) {
        return sprinklerApiDelegate3
                .deleteProgram(id)
                .doOnSuccess(irrelevant -> backupRepository.markProgramsNeedsUpdate())
                .toCompletable();
    }

    public Single<List<ZoneProperties>> zonesProperties3() {
        return sprinklerApiDelegate3
                .zonesProperties()
                .doOnSuccess(response -> backupRepository.updateBackupIfPossible(response))
                .map(ZonesPropertiesResponseMapper3.instance())
                .compose(dealWithError3());
    }

    @Override
    public Completable saveZonesProperties3(final ZoneProperties zoneProperties) {
        return sprinklerApiDelegate3
                .saveZonesProperties(zoneProperties)
                .doOnSuccess(irrelevant -> backupRepository.markZonesPropertiesNeedsUpdate())
                .toCompletable();
    }

    public Single<TimeDate3> timeDate3() {
        return sprinklerApiDelegate3.timeDate();
    }

    public Single<Boolean> units3() {
        return sprinklerApiDelegate3.units();
    }

    public Single<Irrelevant> saveUnits3(boolean isUnitsMetric) {
        return sprinklerApiDelegate3.saveUnits(isUnitsMetric);
    }

    public Single<Irrelevant> saveTimeDate3(LocalDateTime appDate, boolean use24HourFormat) {
        return sprinklerApiDelegate3.saveTimeDate(appDate, use24HourFormat);
    }

    @Override
    public Single<Long> rainDelay3() {
        return sprinklerApiDelegate3.rainDelay();
    }

    public Single<Irrelevant> saveRainDelay3(final int rainDelayValue) {
        return sprinklerApiDelegate3.saveRainDelay(rainDelayValue);
    }

    @Override
    public Single<List<Zone>> zones3() {
        return sprinklerApiDelegate3.zones();
    }

    @Override
    public Single<Zone> zone3(long zoneId) {
        return sprinklerApiDelegate3.zone(zoneId);
    }

    public Single<Irrelevant> startZone3(final long zoneId, final String zoneName, final int
            seconds) {
        return sprinklerApiDelegate3.startZone(zoneId, zoneName, seconds);
    }

    public Single<Irrelevant> stopWatering3() {
        return sprinklerApiDelegate3.stopWatering();
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError3() {
        return (SingleTransformer<T, T>) sprinklerRemoteErrorTransformer3;
    }
}
