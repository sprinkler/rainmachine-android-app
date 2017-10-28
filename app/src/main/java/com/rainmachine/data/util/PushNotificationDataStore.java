package com.rainmachine.data.util;


import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.util.Irrelevant;

import java.util.List;

import io.reactivex.Single;

public interface PushNotificationDataStore {

    Single<List<PushNotification>> getPushNotifications(String pushToken);

    Single<Irrelevant> togglePushNotification(PushNotification pushNotification, String
            pushToken, boolean enable);

    Single<Irrelevant> updatePushRegistration(String pushToken, String deviceId,
                                              boolean enabled, boolean isUnitsMetric,
                                              boolean use24HourFormat);

    Single<Irrelevant> triggerNotification(String token, int notificationType,
                                           String event, boolean isUnitsMetric,
                                           boolean use24HourFormat);
}
