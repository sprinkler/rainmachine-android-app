package com.rainmachine.data.remote.cloud.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rainmachine.domain.model.PushNotification;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class NotificationsStatusResponseMapper implements Function<ResponseBody,
        List<PushNotification>> {

    private static volatile NotificationsStatusResponseMapper instance;
    private final Gson gson;

    public NotificationsStatusResponseMapper(Gson gson) {
        this.gson = gson;
    }

    public static NotificationsStatusResponseMapper instance(Gson gson) {
        if (instance == null) {
            instance = new NotificationsStatusResponseMapper(gson);
        }
        return instance;
    }

    @Override
    public List<PushNotification> apply(@NonNull ResponseBody responseBody) throws Exception {
        String json;
        try {
            json = responseBody.string();
            Type type = new TypeToken<Map<String, Integer>>() {
            }.getType();
            Map<String, Integer> map = gson.fromJson(json, type);
            List<PushNotification> notifications = new ArrayList<>(map.keySet().size());
            for (String code : map.keySet()) {
                try {
                    PushNotification.Type notificationType = PushNotificationUtils.type(code);
                    Integer state = map.get(code);
                    PushNotification pushNotification = new PushNotification();
                    pushNotification.type = notificationType;
                    pushNotification.enabled = state == 1;
                    notifications.add(pushNotification);
                } catch (Exception e) {
                    Timber.w(e, e.getMessage());
                }
            }
            return notifications;
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to read the json");
        }
    }
}
