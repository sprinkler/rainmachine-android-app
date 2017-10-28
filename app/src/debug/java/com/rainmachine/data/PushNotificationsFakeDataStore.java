package com.rainmachine.data;


import com.rainmachine.data.util.PushNotificationDataStore;
import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.util.Irrelevant;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class PushNotificationsFakeDataStore implements PushNotificationDataStore {

    private List<PushNotification> data;

    public PushNotificationsFakeDataStore() {
        data = new ArrayList<>();
        PushNotification pushNotification;
        pushNotification = new PushNotification(PushNotification.Type.BACK_ONLINE, true);
        data.add(pushNotification);
        pushNotification = new PushNotification(PushNotification.Type.DISCONNECTED, true);
        data.add(pushNotification);
        pushNotification = new PushNotification(PushNotification.Type.PROGRAM, true);
        data.add(pushNotification);
        pushNotification = new PushNotification(PushNotification.Type.ZONE, true);
        data.add(pushNotification);
        pushNotification = new PushNotification(PushNotification.Type.RAIN_DELAY, true);
        data.add(pushNotification);
        pushNotification = new PushNotification(PushNotification.Type.GLOBAL, true);
        data.add(pushNotification);
    }

    @Override
    public Single<List<PushNotification>> getPushNotifications(String pushToken) {
        return Single.just(data);
    }

    @Override
    public Single<Irrelevant> togglePushNotification(final PushNotification pushNotification,
                                                     String pushToken, final boolean enable) {
        return Single.fromCallable(() -> {
            for (PushNotification pushNotification1 : data) {
                if (pushNotification1.type == pushNotification.type) {
                    pushNotification1.enabled = enable;
                }
            }
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Single<Irrelevant> updatePushRegistration(String pushToken, String deviceId, boolean
            enabled, boolean isUnitsMetric, boolean use24HourFormat) {
        for (PushNotification pushNotification1 : data) {
            if (pushNotification1.type == PushNotification.Type.GLOBAL) {
                pushNotification1.enabled = enabled;
                break;
            }
        }
        return Single.just(Irrelevant.INSTANCE);
    }

    @Override
    public Single<Irrelevant> triggerNotification(String token, int notificationType, String
            event, boolean isUnitsMetric, boolean use24HourFormat) {
        return Single.just(Irrelevant.INSTANCE);
    }
}
