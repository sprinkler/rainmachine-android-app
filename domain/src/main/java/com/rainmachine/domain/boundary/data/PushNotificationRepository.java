package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.PushNotification;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface PushNotificationRepository {

    Single<List<PushNotification>> getPushNotifications(String pushToken);

    Completable togglePushNotification(PushNotification pushNotification,
                                       String pushToken, boolean enable,
                                       String deviceId, boolean isUnitsMetric,
                                       boolean use24HourFormat);

    Completable updatePushNotificationSettings(String pushToken, String deviceId,
                                               boolean enabled, boolean isUnitsMetric,
                                               boolean use24HourFormat);
}
