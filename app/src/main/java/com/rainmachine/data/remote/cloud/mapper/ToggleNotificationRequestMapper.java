package com.rainmachine.data.remote.cloud.mapper;


import com.rainmachine.data.remote.cloud.request.ToggleNotificationRequest;
import com.rainmachine.domain.model.PushNotification;

public class ToggleNotificationRequestMapper {

    public ToggleNotificationRequest map(PushNotification.Type type, String deviceId, boolean
            enable) throws Exception {
        ToggleNotificationRequest request = new ToggleNotificationRequest();
        request.phoneId = deviceId;
        request.code = PushNotificationUtils.code(type);
        request.state = enable ? 1 : 0;
        return request;
    }
}
