package com.rainmachine.data.remote.sprinkler.v4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rainmachine.data.remote.sprinkler.v4.mapper.BaseResponseCheckPasswordMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.BaseResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.BetaUpdatesResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.CloudSettingsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.CurrentActiveRestrictionsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.DailyStatsDetailsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.DeviceNameResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.DiagUploadStatusResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.DiagnosticsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.GlobalRestrictionsRequestMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.LoginResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.MixersDateResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ParserParamsRequestMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ParserSingleResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ParsersResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ProgramRequestMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ProvisionResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.RainDelayResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.SimulateZoneResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.SupportPinResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.TimeDateResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.UpdateResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.VersionResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.WaterQueueResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.WateringLogDetailsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.WateringLogSimulatedDetailsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.WifiScanResultsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.WifiSettingsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZonePropertiesRequestMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZonePropertiesResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZoneResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZonesResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.request.ActivateParserRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.CloudEmailRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.CloudEnableRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.DeviceNameRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.EmptyRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.EnableRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.HourlyRestrictionDeleteRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.LogLevelRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.LoginRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.PasswordCheckRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.PasswordRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProgramDeleteRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionAmazonAlexaRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionBonjourServiceRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionMasterValveRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionMaxLedBrightnessRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionMaxWateringCoefficientRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionMinDurationThresholdRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionMinLedBrightnessRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRainSensitivityRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRainSensorNormallyClosedRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRainSensorRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRainSensorSnoozeDurationRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionRestrictionsOnLedRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionSaveDefaultsRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionTimezoneRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionTouchAdvancedRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionTouchLongPressTimeoutRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionTouchProgramToRunRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionTouchSleepTimeoutRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionUnitsRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionUseCorrectionRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionWeatherSensitivityRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionWifiSettingsRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionWindSensitivityRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ProvisionZoneDurationRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.RainDelayRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.RainDelayRestrictionRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.ResetRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.RunAllParsersRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.RunParserRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.SaveHourlyRestrictionRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.StartStopProgramRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.StopAllRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.StopZoneRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.TimeDateRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.UpdateRequest;
import com.rainmachine.data.remote.sprinkler.v4.request.WaterZoneRequest;
import com.rainmachine.data.remote.sprinkler.v4.response.GlobalRestrictionsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.HourlyRestrictionsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.ProgramsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.ZonesPropertiesResponse;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer;
import com.rainmachine.data.remote.util.SprinklerRemoteRetry;
import com.rainmachine.data.remote.util.SprinklerStatsRemoteRetry;
import com.rainmachine.domain.model.CloudSettings;
import com.rainmachine.domain.model.CurrentActiveRestrictions;
import com.rainmachine.domain.model.DayStatsDetails;
import com.rainmachine.domain.model.Diagnostics;
import com.rainmachine.domain.model.DiagnosticsUploadStatus;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.domain.model.Login;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.model.Mixer;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.Provision;
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
import com.rainmachine.domain.util.Irrelevant;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.HttpException;

public class SprinklerApiDelegate {

    private SprinklerApi sprinklerApi;
    private SprinklerApiUtils sprinklerApiUtils;
    private Gson gson;
    private SprinklerRemoteRetry sprinklerRemoteRetry;
    private SprinklerRemoteErrorTransformer sprinklerRemoteErrorTransformer;

    private SprinklerStatsRemoteRetry sprinklerStatsRemoteRetry;

    public SprinklerApiDelegate(SprinklerApi sprinklerApi, SprinklerApiUtils sprinklerApiUtils,
                                Gson gson, SprinklerRemoteRetry sprinklerRemoteRetry,
                                SprinklerRemoteErrorTransformer
                                        sprinklerRemoteErrorTransformer) {
        this.sprinklerApi = sprinklerApi;
        this.sprinklerApiUtils = sprinklerApiUtils;
        this.gson = gson;
        this.sprinklerRemoteRetry = sprinklerRemoteRetry;
        this.sprinklerRemoteErrorTransformer = sprinklerRemoteErrorTransformer;

        sprinklerStatsRemoteRetry = new SprinklerStatsRemoteRetry();
    }

    public Single<Boolean> testApiAuthenticated() {
        return sprinklerApi
                .getTimeDate()
                .map(timeDateResponse -> {
                    // If we reach here, it means the authenticated API call succeeded
                    return true;
                })
                .onErrorReturn(throwable -> false);
    }

    public Single<Boolean> testApiFunctional() {
        return sprinklerApi
                .getApiVersion()
                .map(apiVersionResponse -> {
                    // If we reach here, it means the API call finished successfully
                    return true;
                })
                .onErrorReturn(throwable -> false);
    }

    public Single<LocalDateTime> timeDate() {
        return sprinklerApi
                .getTimeDate()
                .retry(sprinklerRemoteRetry)
                .map(TimeDateResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTimeDate(final LocalDate date, LocalTime time) {
        return Single
                .fromCallable(() -> {
                    final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
                    TimeDateRequest request = new TimeDateRequest();
                    DateTime dt = date.toDateTime(time);
                    request.appDate = dt.toString(DATE_TIME_PATTERN);
                    return request;
                })
                .flatMap(request -> sprinklerApi.setTimeDate(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<CurrentActiveRestrictions> currentRestrictions() {
        return sprinklerApi
                .getCurrentActiveRestrictions()
                .retry(sprinklerRemoteRetry)
                .map(CurrentActiveRestrictionsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Long> rainDelay() {
        return sprinklerApi
                .getRainDelay()
                .retry(sprinklerRemoteRetry)
                .map(RainDelayResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainDelay(final int rainDelayValue) {
        return Single
                .fromCallable(() -> {
                    RainDelayRequest request = new RainDelayRequest();
                    request.rainDelay = rainDelayValue;
                    return request;
                })
                .flatMap(request -> sprinklerApi.setRainDelay(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<String> generateSupportPin() {
        return sprinklerApi
                .getSupportPin()
                .retry(sprinklerRemoteRetry)
                .map(SupportPinResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<CloudSettings> cloudSettings() {
        return sprinklerApi
                .getCloudSettings()
                .retry(sprinklerRemoteRetry)
                .map(CloudSettingsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> enableCloud(boolean enable) {
        return Single
                .fromCallable(() -> {
                    CloudEnableRequest request = new CloudEnableRequest();
                    request.enable = enable;
                    return request;
                })
                .flatMap(request -> sprinklerApi.setCloudEnable(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveCloudEmail(String email) {
        return Single
                .fromCallable(() -> {
                    CloudEmailRequest request = new CloudEmailRequest();
                    request.email = email;
                    return request;
                })
                .flatMap(request -> sprinklerApi.setCloudEmail(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> resetCloudCertificates() {
        return Single
                .fromCallable(() -> new EmptyRequest())
                .flatMap(request -> sprinklerApi.resetCloudCertificates(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> reboot() {
        return Single
                .fromCallable(() -> new EmptyRequest())
                .flatMap(request -> sprinklerApi.reboot(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }


    public Single<Boolean> betaUpdates() {
        return sprinklerApi
                .getBetaUpdates()
                .retry(sprinklerRemoteRetry)
                .map(BetaUpdatesResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveBetaUpdates(boolean isEnabled) {
        return Single
                .fromCallable(() -> {
                    EnableRequest request = new EnableRequest();
                    request.enabled = isEnabled;
                    return request;
                })
                .flatMap(request -> sprinklerApi.enableBetaUpdates(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveSshAccess(boolean isEnabled) {
        return Single
                .fromCallable(() -> {
                    EnableRequest request = new EnableRequest();
                    request.enabled = isEnabled;
                    return request;
                })
                .flatMap(request -> sprinklerApi.enableSsh(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Versions> versions() {
        return sprinklerApi
                .getApiVersion()
                .retry(sprinklerRemoteRetry)
                .onErrorResumeNext(sprinklerApi.getApiVersionFallback())
                .map(VersionResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Login> login(final String pass, final boolean isRemember) {
        return Single
                .fromCallable(() -> {
                    LoginRequest request = new LoginRequest();
                    request.pwd = pass;
                    request.remember = isRemember ? 1 : 0;
                    return request;
                })
                .flatMap(request -> sprinklerApi.login(request))
                .map(LoginResponseMapper.instance())
                .onErrorReturn(throwable -> {
                    if (throwable instanceof HttpException) {
                        return sprinklerApiUtils.isSprinklerException((HttpException) throwable)
                                ? new Login(LoginStatus.AUTHENTICATION_FAILED) :
                                new Login(LoginStatus.ERROR_NETWORK);
                    } else {
                        return new Login(LoginStatus.ERROR_NETWORK);
                    }
                });
    }

    public Single<String> changePassword(final String oldPass, final String newPass) {
        return Single
                .fromCallable(() -> {
                    PasswordRequest request = new PasswordRequest();
                    request.oldPass = oldPass;
                    request.newPass = newPass;
                    return request;
                })
                .flatMap(request -> sprinklerApi.changePassword(request))
                .retry(sprinklerRemoteRetry)
                .map(loginResponse -> loginResponse.accessToken)
                .compose(dealWithError());
    }

    public Single<Boolean> checkDefaultPassword() {
        return Single
                .fromCallable(() -> {
                    PasswordCheckRequest request = new PasswordCheckRequest();
                    request.pwd = "";
                    return request;
                })
                .flatMap(request -> sprinklerApi.checkPassword(request))
                .map(BaseResponseCheckPasswordMapper.instance())
                .onErrorReturn(throwable -> false);
    }

    public Single<List<Zone>> zones() {
        return sprinklerApi
                .getZones()
                .retry(sprinklerRemoteRetry)
                .map(ZonesResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Zone> zone(long zoneId) {
        return sprinklerApi
                .getZone(zoneId)
                .retry(sprinklerRemoteRetry)
                .map(ZoneResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> startZone(final long zoneId, final int seconds) {
        return Single
                .fromCallable(() -> {
                    WaterZoneRequest request = new WaterZoneRequest();
                    request.time = seconds;
                    request.id = zoneId;
                    return request;
                })
                .flatMap(request -> sprinklerApi.startZone(zoneId, request))
                .retry((integer, throwable) -> false)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> stopZone(final long zoneId) {
        return Single
                .fromCallable(() -> {
                    StopZoneRequest request = new StopZoneRequest();
                    request.zid = zoneId;
                    return request;
                })
                .flatMap(request -> sprinklerApi.stopZone(zoneId, request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<ZoneSimulation> simulateZone(final ZoneProperties zoneProperties) {
        return Single
                .just(zoneProperties)
                .map(ZonePropertiesRequestMapper.instance())
                .flatMap(zonePropertiesRequest -> sprinklerApi
                        .simulateZone(zonePropertiesRequest)
                        .retry(sprinklerRemoteRetry))
                .map(SimulateZoneResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<ZoneProperties> zoneProperties(long zoneId) {
        return sprinklerApi
                .getZoneProperties(zoneId)
                .retry(sprinklerRemoteRetry)
                .map(ZonePropertiesResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveZoneProperties(final ZoneProperties zoneProperties) {
        return Single
                .just(zoneProperties)
                .map(ZonePropertiesRequestMapper.instance())
                .flatMap(zonePropertiesRequest -> sprinklerApi
                        .setZoneProperties(zoneProperties.id, zonePropertiesRequest)
                        .retry(sprinklerRemoteRetry))
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<List<WateringQueueItem>> wateringQueue() {
        return sprinklerApi
                .getWateringQueue()
                .retry(sprinklerRemoteRetry)
                .map(WaterQueueResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> stopWatering() {
        return Single
                .fromCallable(() -> {
                    StopAllRequest request = new StopAllRequest();
                    request.all = true;
                    return request;
                })
                .flatMap(request -> sprinklerApi.stopWatering(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Update> update(boolean dealWithError) {
        Single<Update> single = sprinklerApi
                .getUpdate()
                .retry(sprinklerRemoteRetry)
                .map(UpdateResponseMapper.instance());
        if (dealWithError) {
            single = single.compose(dealWithError());
        }
        return single;
    }

    public Single<Irrelevant> triggerUpdateCheck() {
        return Single
                .fromCallable(() -> new EmptyRequest())
                .flatMap(request -> sprinklerApi.triggerUpdateCheck(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> triggerUpdate() {
        return Single
                .fromCallable(() -> {
                    UpdateRequest request = new UpdateRequest();
                    request.update = true;
                    return request;
                })
                .flatMap(request -> sprinklerApi.triggerUpdate(request))
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveLogLevel(int level) {
        return Single
                .fromCallable(() -> {
                    LogLevelRequest request = new LogLevelRequest();
                    request.level = level;
                    return request;
                })
                .flatMap(request -> sprinklerApi.setLogLevel(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Parser> parser(long uid) {
        return sprinklerApi
                .getParser(uid)
                .retry(sprinklerRemoteRetry)
                .map(ParserSingleResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveParserParams(final Parser parser) {
        return Single
                .just(parser)
                .map(ParserParamsRequestMapper.instance())
                .map(params -> gson.toJson(params))
                .flatMap(body -> {
                    RequestBody requestBody = RequestBody.create(MediaType.parse
                            ("application/json"), body);
                    return sprinklerApi
                            .setParserParams(parser.uid, requestBody)
                            .retry(sprinklerRemoteRetry)
                            .map(BaseResponseMapper.instance());
                })
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveParserEnabled(long parserId, boolean isEnabled) {
        return Single
                .fromCallable(() -> {
                    ActivateParserRequest request = new ActivateParserRequest();
                    request.activate = isEnabled;
                    return request;
                })
                .flatMap(request -> sprinklerApi.activateParser(parserId, request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> addParser(String filename, File file) {
        final String headerContentDisposition = "inline; filename=" + filename;
        return Single
                .fromCallable(() -> RequestBody.create(MediaType.parse("text/x-python"), file))
                .flatMap(requestBody -> sprinklerApi.addParser(headerContentDisposition,
                        requestBody))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> runParser(long id) {
        return Single
                .fromCallable(() -> {
                    RunParserRequest request = new RunParserRequest();
                    request.id = id;
                    request.parser = true;
                    return request;
                })
                .flatMap(request -> sprinklerApi.runParser(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> runAllParsers() {
        return Single
                .fromCallable(() -> {
                    RunAllParsersRequest request = new RunAllParsersRequest();
                    request.parser = true;
                    request.mixer = true;
                    request.simulator = false;
                    return request;
                })
                .flatMap(request -> sprinklerApi.runAllParsers(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> setParserDefaults(long id) {
        return sprinklerApi
                .setParserDefaults(id, new EmptyRequest())
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> enableLightLEDs(boolean enable) {
        return Single
                .fromCallable(() -> {
                    EnableRequest request = new EnableRequest();
                    request.enabled = enable;
                    return request;
                })
                .flatMap(request -> sprinklerApi.enableLightLEDs(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<List<WifiScan>> wifiScan() {
        return sprinklerApi
                .getWifiScanResults()
                .retry(sprinklerRemoteRetry)
                .map(WifiScanResultsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<WifiSettingsSimple> wifiSettings() {
        return sprinklerApi
                .getWifiSettings()
                .retry(sprinklerRemoteRetry)
                .map(WifiSettingsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveWifiSettings(final WifiSettings wifiSettings) {
        return Single
                .fromCallable(() -> {
                    ProvisionWifiSettingsRequest request = new ProvisionWifiSettingsRequest();
                    request.ssid = wifiSettings.sSID;
                    request.encryption = null;
                    if (wifiSettings.isWPA2) {
                        request.encryption = "psk2";
                    } else if (wifiSettings.isWPA) {
                        request.encryption = "psk";
                    } else if (wifiSettings.isWEP) {
                        request.encryption = "wep";
                    }
                    request.key = wifiSettings.password;
                    if (wifiSettings.networkType == WifiSettings.NETWORK_TYPE_DHCP) {
                        request.networkType = "dhcp";
                    } else if (wifiSettings.networkType == WifiSettings.NETWORK_TYPE_STATIC) {
                        request.networkType = "static";
                        request.addressInfo = new ProvisionWifiSettingsRequest.WifiAddressInfo();
                        request.addressInfo.ipaddr = wifiSettings.ipAddress;
                        request.addressInfo.netmask = wifiSettings.netmask;
                        request.addressInfo.gateway = wifiSettings.gateway;
                        request.addressInfo.dns = wifiSettings.dns;
                    }
                    return request;
                })
                .flatMap(request -> sprinklerApi.setWifiSettings(request))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Map<LocalDate, Mixer>> mixers(int numPastDays, int numFutureDays) {
        final String START_DATE_PATTERN = "yyyy-MM-dd";
        LocalDate startDate = new LocalDate().minusDays(numPastDays);
        return sprinklerApi
                .getMixers(startDate.toString(START_DATE_PATTERN), numPastDays +
                        numFutureDays)
                .retry(sprinklerRemoteRetry)
                .map(MixersDateResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Map<LocalDate, DayStatsDetails>> statsDetails() {
        return sprinklerApi
                .getDailyStatsDetails().toObservable()
                .retryWhen(sprinklerStatsRemoteRetry)
                .map(DailyStatsDetailsResponseMapper.instance())
                .singleOrError()
                .compose(dealWithError());
    }

    public Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogDetails(LocalDate
                                                                                    startDate,
                                                                            int numDays) {
        final String START_DATE_PATTERN = "yyyy-MM-dd";
        return sprinklerApi
                .getWateringLogDetails(startDate.toString(START_DATE_PATTERN), numDays)
                .retry(sprinklerRemoteRetry)
                .map(WateringLogDetailsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Map<LocalDate, WateringLogDetailsDay>> wateringLogSimulatedDetails(int numDays) {
        final String START_DATE_PATTERN = "yyyy-MM-dd";
        LocalDate startDate = new LocalDate().minusDays(numDays);
        return sprinklerApi
                .getWateringLogSimulatedDetails(startDate.toString(START_DATE_PATTERN),
                        numDays)
                .retry(sprinklerRemoteRetry)
                .map(WateringLogSimulatedDetailsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Diagnostics> diagnostics() {
        return sprinklerApi
                .getDiag()
                .retry(sprinklerRemoteRetry)
                .map(DiagnosticsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> sendDiagnostics() {
        EmptyRequest request = new EmptyRequest();
        return sprinklerApi
                .uploadDiag(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<DiagnosticsUploadStatus> getDiagnosticsUpload() {
        return sprinklerApi
                .getDiagUploadStatus()
                .retry(sprinklerRemoteRetry)
                .map(DiagUploadStatusResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<List<Parser>> parsers() {
        return sprinklerApi
                .getParsers()
                .retry(sprinklerRemoteRetry)
                .map(ParsersResponseMapper.instance())
                .compose(dealWithError());
    }

    /* Provision */
    public Single<Provision> provision() {
        return sprinklerApi
                .getProvision()
                .retry(sprinklerRemoteRetry)
                .map(ProvisionResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> resetProvision() {
        ResetRequest request = new ResetRequest();
        request.restart = true;
        return sprinklerApi
                .resetProvision(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainSensor(boolean enabled) {
        ProvisionRainSensorRequest request = new ProvisionRainSensorRequest();
        request.system = new ProvisionRainSensorRequest.System();
        request.system.useRainSensor = enabled;
        return sprinklerApi
                .setProvisionRainSensor(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainSensorSnoozeDuration(Provision.RainSensorSnoozeDuration
                                                                   snoozeDuration) {
        ProvisionRainSensorSnoozeDurationRequest request = new
                ProvisionRainSensorSnoozeDurationRequest();
        request.system = new ProvisionRainSensorSnoozeDurationRequest.System();
        switch (snoozeDuration) {
            case RESUME:
                request.system.rainSensorSnoozeDuration = 0;
                break;
            case UNTIL_MIDNIGHT:
                request.system.rainSensorSnoozeDuration = -1;
                break;
            case SNOOZE_6_HOURS:
                request.system.rainSensorSnoozeDuration = 21600;
                break;
            case SNOOZE_12_HOURS:
                request.system.rainSensorSnoozeDuration = 43200;
                break;
            case SNOOZE_24_HOURS:
                request.system.rainSensorSnoozeDuration = 86400;
                break;
            case SNOOZE_48_HOURS:
                request.system.rainSensorSnoozeDuration = 172800;
                break;
            default:
                request.system.rainSensorSnoozeDuration = 0;
                break;
        }
        return sprinklerApi
                .setProvisionRainSensorSnoozeDuration(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveLocation(final LocationInfo location, final String
            timezone, final double elevation) {
        ProvisionRequest request = new ProvisionRequest();
        request.location = new ProvisionRequest.Location();
        request.location.latitude = location.latitude;
        request.location.longitude = location.longitude;
        request.location.timezone = timezone;
        request.location.elevation = elevation;
        request.location.name = location.fullAddress;
        return sprinklerApi
                .setProvisionLocation(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveUnits(boolean isUnitsMetric) {
        ProvisionUnitsRequest request = new ProvisionUnitsRequest();
        request.system = new ProvisionUnitsRequest.System();
        request.system.uiUnitsMetric = isUnitsMetric;
        return sprinklerApi
                .setProvisionUnits(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveZoneDurations(List<Long> zoneDurations) {
        ProvisionZoneDurationRequest request = new ProvisionZoneDurationRequest();
        request.system = new ProvisionZoneDurationRequest.System();
        request.system.zoneDuration = zoneDurations;
        return sprinklerApi
                .setProvisionZoneDuration(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> setProvisionMasterValve(boolean enabled) {
        ProvisionMasterValveRequest request = new ProvisionMasterValveRequest();
        request.system = new ProvisionMasterValveRequest.System();
        request.system.useMasterValve = enabled;
        return sprinklerApi
                .setProvisionMasterValve(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTimezone(final String timezone) {
        ProvisionTimezoneRequest request = new ProvisionTimezoneRequest();
        request.location = new ProvisionTimezoneRequest.Location();
        request.location.timezone = timezone;
        return sprinklerApi
                .setProvisionTimezone(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveMinWateringDurationThreshold(int threshold) {
        ProvisionMinDurationThresholdRequest request = new ProvisionMinDurationThresholdRequest();
        request.system = new ProvisionMinDurationThresholdRequest.System();
        request.system.minWateringDurationThreshold = threshold;
        return sprinklerApi
                .setProvisionMinWateringDurationThreshold(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveMaxWateringCoefficient(float value) {
        ProvisionMaxWateringCoefficientRequest request = new
                ProvisionMaxWateringCoefficientRequest();
        request.system = new ProvisionMaxWateringCoefficientRequest.System();
        request.system.maxWateringCoef = value;
        return sprinklerApi
                .setProvisionMaxWateringCoefficient(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveProvisionDefaults(int minWateringDurationThreshold, float
            maxWateringCoefficient) {
        ProvisionSaveDefaultsRequest request = new ProvisionSaveDefaultsRequest();
        request.system = new ProvisionSaveDefaultsRequest.System();
        request.system.minWateringDurationThreshold = minWateringDurationThreshold;
        request.system.maxWateringCoef = maxWateringCoefficient;
        return sprinklerApi
                .setProvisionDefaults(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveAmazonAlexa(boolean isEnabled) {
        ProvisionAmazonAlexaRequest request = new ProvisionAmazonAlexaRequest();
        request.system = new ProvisionAmazonAlexaRequest.System();
        request.system.allowAlexaDiscovery = isEnabled;
        return sprinklerApi
                .setProvisionAmazonAlexa(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveBonjourService(boolean isEnabled) {
        ProvisionBonjourServiceRequest request = new ProvisionBonjourServiceRequest();
        request.system = new ProvisionBonjourServiceRequest.System();
        request.system.useBonjourService = isEnabled;
        return sprinklerApi
                .setProvisionBonjourService(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveUseCorrection(boolean isEnabled) {
        ProvisionUseCorrectionRequest request = new ProvisionUseCorrectionRequest();
        request.system = new ProvisionUseCorrectionRequest.System();
        request.system.useCorrectionForPast = isEnabled;
        return sprinklerApi
                .setProvisionUseCorrection(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTouchProgramToRun(int programId) {
        return Single
                .fromCallable(() -> {
                    ProvisionTouchProgramToRunRequest request = new
                            ProvisionTouchProgramToRunRequest();
                    request.system = new ProvisionTouchProgramToRunRequest.System();
                    boolean enabled = programId > 0;
                    request.system.touchCyclePrograms = enabled;
                    request.system.touchProgramToRun = enabled ? programId : null;
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.serializeNulls().create();
                    String body = gson.toJson(request);
                    return RequestBody.create(MediaType.parse("application/json"), body);
                })
                .flatMap(requestBody -> sprinklerApi.setProvisionTouchProgramToRun(requestBody))
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTouchAdvanced(boolean isEnabled) {
        ProvisionTouchAdvancedRequest request = new ProvisionTouchAdvancedRequest();
        request.system = new ProvisionTouchAdvancedRequest.System();
        request.system.touchAdvanced = isEnabled;
        return sprinklerApi
                .setProvisionTouchAdvanced(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Completable saveRestrictionsOnLed(boolean isEnabled) {
        ProvisionRestrictionsOnLedRequest request = new ProvisionRestrictionsOnLedRequest();
        request.system = new ProvisionRestrictionsOnLedRequest.System();
        request.system.showRestrictionsOnLed = isEnabled;
        return sprinklerApi
                .setProvisionRestrictionsOnLed(request)
                .retry(sprinklerRemoteRetry)
                .compose(dealWithError())
                .toCompletable();
    }

    public Single<Irrelevant> saveMinLedBrightness(int value) {
        ProvisionMinLedBrightnessRequest request = new ProvisionMinLedBrightnessRequest();
        request.system = new ProvisionMinLedBrightnessRequest.System();
        request.system.minLEDBrightness = value;
        return sprinklerApi
                .setProvisionMinLedBrightness(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveMaxLedBrightness(int value) {
        ProvisionMaxLedBrightnessRequest request = new ProvisionMaxLedBrightnessRequest();
        request.system = new ProvisionMaxLedBrightnessRequest.System();
        request.system.maxLEDBrightness = value;
        return sprinklerApi
                .setProvisionMaxLedBrightness(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTouchSleepTimeout(int value) {
        ProvisionTouchSleepTimeoutRequest request = new ProvisionTouchSleepTimeoutRequest();
        request.system = new ProvisionTouchSleepTimeoutRequest.System();
        request.system.touchSleepTimeout = value;
        return sprinklerApi
                .setProvisionTouchSleepTimeout(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTouchLongPressTimeout(int value) {
        ProvisionTouchLongPressTimeoutRequest request = new ProvisionTouchLongPressTimeoutRequest();
        request.system = new ProvisionTouchLongPressTimeoutRequest.System();
        request.system.touchLongPressTimeout = value;
        return sprinklerApi
                .setProvisionTouchLongPressTimeout(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainSensorNormallyClosed(boolean isEnabled) {
        ProvisionRainSensorNormallyClosedRequest request = new
                ProvisionRainSensorNormallyClosedRequest();
        request.system = new ProvisionRainSensorNormallyClosedRequest.System();
        request.system.rainSensorIsNormallyClosed = isEnabled;
        return sprinklerApi
                .setProvisionRainSensorNormallyClosed(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveWindSensitivity(float windSensitivity) {
        ProvisionWindSensitivityRequest request = new ProvisionWindSensitivityRequest();
        request.location = new ProvisionWindSensitivityRequest.Location();
        request.location.windSensitivity = windSensitivity;
        return sprinklerApi
                .setProvisionWindSensitivity(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveWeatherSensitivity(float rainSensitivity, int wsDays, float
            windSensitivity) {
        ProvisionWeatherSensitivityRequest request = new ProvisionWeatherSensitivityRequest();
        request.location = new ProvisionWeatherSensitivityRequest.Location();
        request.location.rainSensitivity = rainSensitivity;
        request.location.wsDays = wsDays;
        request.location.windSensitivity = windSensitivity;
        return sprinklerApi
                .setProvisionWeatherSensitivity(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainSensitivity(final float rainSensitivity) {
        ProvisionRainSensitivityRequest request = new ProvisionRainSensitivityRequest();
        request.location = new ProvisionRainSensitivityRequest.Location();
        request.location.rainSensitivity = rainSensitivity;
        return sprinklerApi
                .setProvisionRainSensitivity(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<ProgramsResponse> programs() {
        return sprinklerApi
                .getPrograms()
                .retry(sprinklerRemoteRetry);
    }

    public Single<Irrelevant> createProgram(Program program) {
        return Single
                .just(program)
                .map(ProgramRequestMapper.instance())
                .flatMap(programRequest -> sprinklerApi
                        .createProgram(programRequest)
                        .retry(sprinklerRemoteRetry))
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> updateProgram(final Program program) {
        return Single
                .just(program)
                .map(ProgramRequestMapper.instance())
                .flatMap(programRequest -> sprinklerApi
                        .setProgram(program.id, programRequest)
                        .retry(sprinklerRemoteRetry))
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> startProgram(long id) {
        StartStopProgramRequest request = new StartStopProgramRequest();
        request.pid = id;
        return sprinklerApi
                .startProgram(id, request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> stopProgram(long id) {
        StartStopProgramRequest request = new StartStopProgramRequest();
        request.pid = id;
        return sprinklerApi
                .stopProgram(id, request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> deleteProgram(long id) {
        ProgramDeleteRequest request = new ProgramDeleteRequest();
        request.pid = id;
        return sprinklerApi
                .deleteProgram(id, request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<HourlyRestrictionsResponse> hourlyRestrictions() {
        return sprinklerApi
                .getHourlyRestrictions()
                .retry(sprinklerRemoteRetry);
    }

    public Single<Irrelevant> saveHourlyRestriction(final HourlyRestriction restriction) {
        SaveHourlyRestrictionRequest request = new SaveHourlyRestrictionRequest();
        request.start = restriction.dayStartMinute;
        request.duration = restriction.minuteDuration;
        StringBuilder sb = new StringBuilder();
        for (boolean weekday : restriction.weekDays) {
            sb.append(weekday ? 1 : 0);
        }
        request.weekDays = sb.toString();
        return sprinklerApi
                .setHourlyRestrictions(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> updateHourlyRestriction(HourlyRestriction restriction) {
        SaveHourlyRestrictionRequest request = new SaveHourlyRestrictionRequest();
        request.start = restriction.dayStartMinute;
        request.duration = restriction.minuteDuration;
        StringBuilder sb = new StringBuilder();
        for (boolean weekday : restriction.weekDays) {
            sb.append(weekday ? 1 : 0);
        }
        request.weekDays = sb.toString();
        return sprinklerApi
                .updateHourlyRestriction(restriction.uid, request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> deleteHourlyRestriction(long restrictionId) {
        HourlyRestrictionDeleteRequest request = new HourlyRestrictionDeleteRequest();
        request.uid = restrictionId;
        return sprinklerApi
                .deleteHourlyRestriction(restrictionId, request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<String> deviceName() {
        return sprinklerApi
                .getDeviceName()
                .retry(sprinklerRemoteRetry)
                .map(DeviceNameResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveDeviceName(final String deviceName) {
        DeviceNameRequest request = new DeviceNameRequest();
        request.netName = deviceName;
        return sprinklerApi
                .setDeviceName(request)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<ZonesPropertiesResponse> zonesProperties() {
        return sprinklerApi
                .getZonesProperties()
                .retry(sprinklerRemoteRetry);
    }

    public Single<GlobalRestrictionsResponse> globalRestrictions() {
        return sprinklerApi
                .getGlobalRestrictions()
                .retry(sprinklerRemoteRetry);
    }

    public Single<Irrelevant> saveGlobalRestrictions(final GlobalRestrictions data) {
        return Single
                .just(GlobalRestrictionsRequestMapper.instance().call(data))
                .flatMap(request -> sprinklerApi
                        .setGlobalRestrictions(request)
                        .retry(sprinklerRemoteRetry)
                        .map(BaseResponseMapper.instance()))
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainDelayRestriction(int numSeconds) {
        RainDelayRestrictionRequest request = new RainDelayRestrictionRequest();
        request.rainDelayDuration = numSeconds;
        // todo: should be the time from the RainMachine device not from Android device
        request.rainDelayStartTime = DateTime.now().getMillis() / DateTimeConstants
                .MILLIS_PER_SECOND;
        return Single
                .just(request)
                .flatMap(rainDelayRequest -> sprinklerApi
                        .setRainDelayRestriction(rainDelayRequest)
                        .retry(sprinklerRemoteRetry)
                        .map(BaseResponseMapper.instance()))
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveGlobalRestrictionsRaw(final String body) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        return sprinklerApi
                .setGlobalRestrictionsRaw(requestBody)
                .retry(sprinklerRemoteRetry)
                .map(BaseResponseMapper.instance())
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) sprinklerRemoteErrorTransformer;
    }
}
