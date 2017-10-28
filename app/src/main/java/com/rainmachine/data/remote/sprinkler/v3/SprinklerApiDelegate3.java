package com.rainmachine.data.remote.sprinkler.v3;

import com.rainmachine.data.remote.sprinkler.v3.mapper.BaseResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.MessageResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ProgramRequestMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.RainDelayResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.TimeDateResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.UpdateResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.WeatherDataResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ZonePropertiesRequestMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ZoneResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ZonesResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.request.PasswordRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.RainDelayRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.TimeDateRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.UnitsRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.UpdateRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.WaterZoneRequest3;
import com.rainmachine.data.remote.sprinkler.v3.response.ProgramsResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZoneSubResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesPropertiesResponse3;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer3;
import com.rainmachine.data.remote.util.SprinklerRemoteRetry;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.TimeDate3;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.Zone;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.Irrelevant;

import org.javatuples.Pair;
import org.joda.time.LocalDateTime;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import retrofit2.HttpException;
import retrofit2.Response;

public class SprinklerApiDelegate3 {

    private SprinklerApi3 sprinklerApi3;
    private SprinklerApiLogin3 sprinklerApiLogin3;
    private SprinklerRemoteRetry sprinklerRemoteRetry;
    private SprinklerRemoteErrorTransformer3 sprinklerRemoteErrorTransformer3;

    public SprinklerApiDelegate3(SprinklerApi3 sprinklerApi3,
                                 SprinklerApiLogin3 sprinklerApiLogin3,
                                 SprinklerRemoteRetry sprinklerRemoteRetry,
                                 SprinklerRemoteErrorTransformer3
                                         sprinklerRemoteErrorTransformer3) {
        this.sprinklerApi3 = sprinklerApi3;
        this.sprinklerApiLogin3 = sprinklerApiLogin3;
        this.sprinklerRemoteRetry = sprinklerRemoteRetry;
        this.sprinklerRemoteErrorTransformer3 = sprinklerRemoteErrorTransformer3;
    }

    public Single<String> login(final String username, final String pass, final boolean
            isRemember) {
        final String EMPTY_COOKIE = "";
        return sprinklerApiLogin3
                .login3("login", username, pass, isRemember ? "true" : "")
                .map(response -> EMPTY_COOKIE)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        HttpException httpException = (HttpException) throwable;
                        Response response = httpException.response();
                        if (response != null && response.code() == 302) {
                            return Single.just(getCookie3(response));
                        }
                    }
                    return Single.just(EMPTY_COOKIE);
                });
    }

    private String getCookie3(Response response) {
        StringBuilder sb = new StringBuilder(50);
        okhttp3.Headers headers = response.headers();
        List<String> values = headers.values("Set-Cookie");
        for (String value : values) {
            sb.append(value).append(";");
        }
        return sb.toString();
    }

    public Single<Irrelevant> changePassword(final String oldPass, final String
            newPass) {
        PasswordRequest3 request = new PasswordRequest3();
        request.oldPass = oldPass;
        request.newPass = newPass;
        request.confirmPass = newPass;
        return sprinklerApi3
                .changePassword3(request)
                .retry(sprinklerRemoteRetry)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<List<DayStats>> weatherData() {
        return sprinklerApi3
                .getWeatherData3()
                .map(WeatherDataResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Update> update(boolean dealWithError) {
        Single<Update> observable = sprinklerApi3
                .getUpdate3()
                .map(UpdateResponseMapper3.instance());
        if (dealWithError) {
            observable = observable
                    .compose(dealWithError());
        }
        return observable;
    }

    public Single<Irrelevant> triggerUpdate() {
        UpdateRequest3 request = new UpdateRequest3();
        request.update = true;
        return sprinklerApi3
                .makeUpdate3(request)
                .map(BaseResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<ProgramsResponse3> programs() {
        return sprinklerApi3.getPrograms3();
    }

    public Single<Irrelevant> createUpdateProgram(Program program, boolean use24HourFormat) {
        return Single
                .just(Pair.with(program, use24HourFormat))
                .map(ProgramRequestMapper3.instance())
                .flatMap(programRequest3 -> sprinklerApi3
                        .createUpdateProgram3(programRequest3)
                        .retry(sprinklerRemoteRetry))
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> runStopProgram(long id) {
        return sprinklerApi3
                .runStopProgram3(id, "")
                .retry(sprinklerRemoteRetry)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> deleteProgram(long id) {
        return sprinklerApi3
                .deleteProgram3("settings", "delete_program", "" + id)
                .retry(sprinklerRemoteRetry)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<ZonesPropertiesResponse3> zonesProperties() {
        return sprinklerApi3.getZonesProperties3();
    }

    public Single<Irrelevant> saveZonesProperties(final ZoneProperties zoneProperties) {
        return Single.just(zoneProperties)
                .map(ZonePropertiesRequestMapper3.instance())
                .flatMap(zonePropertiesRequest3 -> sprinklerApi3.saveZoneProperties3
                        (zoneProperties.id, zonePropertiesRequest3))
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<TimeDate3> timeDate() {
        return sprinklerApi3
                .getTimeDate3()
                .map(TimeDateResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Boolean> units() {
        return sprinklerApi3
                .getUnits3()
                .map(unitsResponse3 -> "C".equalsIgnoreCase(unitsResponse3.settings.units))
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveUnits(boolean isUnitsMetric) {
        UnitsRequest3 request = new UnitsRequest3();
        request.units = isUnitsMetric ? "C" : "F";
        return sprinklerApi3
                .setUnits3(request)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveTimeDate(LocalDateTime appDate, boolean use24HourFormat) {
        TimeDateRequest3 request = new TimeDateRequest3();
        request.appDate = appDate.toString("yyyy/M/d H:m");
        request.timeFormat = use24HourFormat ? 24 : 12;
        return sprinklerApi3
                .setTimeDate3(request)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Long> rainDelay() {
        return sprinklerApi3
                .getRainDelay3()
                .map(RainDelayResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> saveRainDelay(final int rainDelayValue) {
        RainDelayRequest3 request = new RainDelayRequest3();
        request.rainDelay = rainDelayValue;
        return sprinklerApi3
                .setRainDelay3(request)
                .map(MessageResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<List<Zone>> zones() {
        return sprinklerApi3
                .getZones3()
                .map(ZonesResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Zone> zone(long zoneId) {
        return sprinklerApi3
                .getZone3(zoneId)
                .map(ZoneResponseMapper3.instance())
                .compose(dealWithError());
    }

    public Single<Irrelevant> startZone(final long zoneId, final String zoneName, final int
            seconds) {
        WaterZoneRequest3 request = new WaterZoneRequest3();
        request.counter = seconds;
        request.id = zoneId;
        request.name = zoneName;
        request.state = ZoneSubResponse3.STATE_IDLE;
        return sprinklerApi3
                .waterZone3(zoneId, request)
                .retry(sprinklerRemoteRetry)
                .map(response -> Irrelevant.INSTANCE)
                .compose(dealWithError());
    }

    public Single<Irrelevant> stopWatering() {
        return sprinklerApi3
                .stopAll3()
                .retry(sprinklerRemoteRetry)
                .map(response -> Irrelevant.INSTANCE)
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) sprinklerRemoteErrorTransformer3;
    }
}
