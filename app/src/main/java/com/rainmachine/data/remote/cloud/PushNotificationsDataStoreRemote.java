package com.rainmachine.data.remote.cloud;

import com.google.gson.Gson;
import com.rainmachine.data.remote.cloud.mapper.NotificationsStatusResponseMapper;
import com.rainmachine.data.remote.cloud.mapper.ToggleNotificationRequestMapper;
import com.rainmachine.data.remote.cloud.request.UpdatePushRegistrationRequest;
import com.rainmachine.data.remote.util.RemoteErrorTransformer;
import com.rainmachine.data.remote.util.RemoteRetry;
import com.rainmachine.data.util.PushNotificationDataStore;
import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.util.Irrelevant;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;

public class PushNotificationsDataStoreRemote implements PushNotificationDataStore {

    private final CloudPushApi cloudPushApi;
    private final Gson gson;
    private RemoteRetry remoteRetry;
    private RemoteErrorTransformer remoteErrorTransformer;

    public PushNotificationsDataStoreRemote(CloudPushApi cloudPushApi, Gson gson) {
        this.cloudPushApi = cloudPushApi;
        this.gson = gson;
        remoteRetry = new RemoteRetry();
        remoteErrorTransformer = new RemoteErrorTransformer();
    }

    @Override
    public Single<List<PushNotification>> getPushNotifications(String deviceId) {
        return cloudPushApi
                .notificationsStatus(deviceId)
                .retry(remoteRetry)
                .map(NotificationsStatusResponseMapper.instance(gson))
                .compose(dealWithError());
    }

    @Override
    public Single<Irrelevant> togglePushNotification(final PushNotification pushNotification,
                                                     final String deviceId, final boolean
                                                             enable) {
        return Single
                .fromCallable(() -> new ToggleNotificationRequestMapper()
                        .map(pushNotification.type, deviceId, enable))
                .flatMap(request -> cloudPushApi
                        .toggleNotification(request)
                        .retry(remoteRetry))
                .map(response -> Irrelevant.INSTANCE)
                .compose(dealWithError());
    }

    @Override
    public Single<Irrelevant> updatePushRegistration(final String pushToken, final String
            deviceId, final boolean enabled, final boolean isUnitsMetric, final boolean
                                                             use24HourFormat) {
        return Single
                .fromCallable(() -> {
                    UpdatePushRegistrationRequest request = new UpdatePushRegistrationRequest();
                    request.token = pushToken;
                    request.os = "android";
                    request.phoneId = deviceId;
                    int millisecondOffset = DateTimeZone.getDefault().getOffsetFromLocal
                            (DateTime.now().getMillis());
                    request.timezone = millisecondOffset / DateTimeConstants.MILLIS_PER_SECOND;
                    request.send_notifications = enabled ? 1 : 0;
                    request.isUnitsMetric = isUnitsMetric ? 1 : 0;
                    request.use24HourFormat = use24HourFormat ? 1 : 0;
                    return request;
                })
                .flatMap(request -> cloudPushApi
                        .updateRegistration(request)
                        .retry(remoteRetry))
                .map(response -> Irrelevant.INSTANCE)
                .compose(dealWithError());
    }

    @Override
    public Single<Irrelevant> triggerNotification(String token, int notificationType, String
            event, boolean isUnitsMetric, boolean use24HourFormat) {
        return cloudPushApi.triggerNotification(token, notificationType, "android", event,
                isUnitsMetric ? 1 : 0, use24HourFormat ? 1 : 0)
                .map(responseBody -> Irrelevant.INSTANCE)
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) remoteErrorTransformer;
    }
}
