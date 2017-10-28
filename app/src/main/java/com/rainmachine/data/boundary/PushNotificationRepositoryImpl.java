package com.rainmachine.data.boundary;

import com.rainmachine.data.util.PushNotificationDataStore;
import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.model.PushNotification;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;


public class PushNotificationRepositoryImpl implements PushNotificationRepository {

    private final PushNotificationDataStore remoteDataStore;

    public PushNotificationRepositoryImpl(PushNotificationDataStore remoteDataStore) {
        this.remoteDataStore = remoteDataStore;
    }

    @Override
    public Completable updatePushNotificationSettings(String pushToken, String deviceId,
                                                      boolean enabled, boolean isUnitsMetric,
                                                      boolean use24HourFormat) {
        return remoteDataStore.updatePushRegistration(pushToken, deviceId, enabled,
                isUnitsMetric, use24HourFormat).toCompletable();
    }

    @Override
    public Completable togglePushNotification(PushNotification pushNotification,
                                              String pushToken, boolean enable, String deviceId,
                                              boolean isUnitsMetric, boolean use24HourFormat) {
        if (pushNotification.type == PushNotification.Type.GLOBAL) {
            return remoteDataStore.updatePushRegistration(pushToken, deviceId, enable,
                    isUnitsMetric, use24HourFormat).toCompletable();
        } else {
            return remoteDataStore.togglePushNotification(pushNotification, deviceId, enable)
                    .toCompletable();
        }
    }

    @Override
    public Single<List<PushNotification>> getPushNotifications(String deviceId) {
        return remoteDataStore.getPushNotifications(deviceId);
    }
}
