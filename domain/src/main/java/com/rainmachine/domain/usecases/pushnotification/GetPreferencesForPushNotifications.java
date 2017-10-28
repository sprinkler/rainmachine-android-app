package com.rainmachine.domain.usecases.pushnotification;


import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Single;

public class GetPreferencesForPushNotifications extends
        ObservableUseCase<GetPreferencesForPushNotifications
                .RequestModel, GetPreferencesForPushNotifications.ResponseModel> {

    private final DeviceRepository deviceRepository;
    private PrefRepository prefRepository;

    public GetPreferencesForPushNotifications(DeviceRepository deviceRepository,
                                              PrefRepository prefRepository) {
        this.deviceRepository = deviceRepository;
        this.prefRepository = prefRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        final DevicePreferences devicePreferencesDefault = new DevicePreferences();
        devicePreferencesDefault.isUnitsMetric = false;
        devicePreferencesDefault.use24HourFormat = false;
        final Single<DevicePreferences> mostRecentTimestamp = deviceRepository
                .getDevicePreferencesForMostRecentDevice(devicePreferencesDefault);
        return Single
                .fromCallable(() -> {
                    String currentDeviceId = prefRepository.currentDeviceId();
                    if (currentDeviceId == null) {
                        return "";
                    }
                    return currentDeviceId;
                })
                .flatMap(deviceId -> {
                    if (!Strings.isBlank(deviceId)) {
                        return deviceRepository.getDevicePreferences(deviceId);
                    }
                    return Single.error(new Throwable());
                })
                .onErrorResumeNext(throwable -> mostRecentTimestamp)
                .map(devicePreferences -> new ResponseModel(devicePreferences.isUnitsMetric,
                        devicePreferences.use24HourFormat))
                .toObservable();
    }

    public static class RequestModel {
    }

    static class ResponseModel {
        public boolean isUnitsMetric;
        public boolean use24HourFormat;

        ResponseModel(boolean isUnitsMetric, boolean use24HourFormat) {
            this.isUnitsMetric = isUnitsMetric;
            this.use24HourFormat = use24HourFormat;
        }
    }
}
