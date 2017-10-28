package com.rainmachine.data.remote.cloud.mapper;


import com.rainmachine.domain.model.PushNotification;

import static com.rainmachine.domain.model.PushNotification.Type.BACK_ONLINE;
import static com.rainmachine.domain.model.PushNotification.Type.DISCONNECTED;
import static com.rainmachine.domain.model.PushNotification.Type.FREEZE_TEMPERATURE;
import static com.rainmachine.domain.model.PushNotification.Type.GLOBAL;
import static com.rainmachine.domain.model.PushNotification.Type.NEW_SOFTWARE_VERSION;
import static com.rainmachine.domain.model.PushNotification.Type.PROGRAM;
import static com.rainmachine.domain.model.PushNotification.Type.RAIN_DELAY;
import static com.rainmachine.domain.model.PushNotification.Type.RAIN_SENSOR;
import static com.rainmachine.domain.model.PushNotification.Type.REBOOT;
import static com.rainmachine.domain.model.PushNotification.Type.REMOTE_ACCESS;
import static com.rainmachine.domain.model.PushNotification.Type.SHORT;
import static com.rainmachine.domain.model.PushNotification.Type.TEXT_MODE;
import static com.rainmachine.domain.model.PushNotification.Type.WEATHER;
import static com.rainmachine.domain.model.PushNotification.Type.ZONE;

public class PushNotificationUtils {

    public static String code(PushNotification.Type type) throws Exception {
        switch (type) {
            case DISCONNECTED:
                return "1";
            case BACK_ONLINE:
                return "3";
            case NEW_SOFTWARE_VERSION:
                return "2";
            case REMOTE_ACCESS:
                return "11111";
            case ZONE:
                return "10001";
            case PROGRAM:
                return "10002";
            case WEATHER:
                return "10003";
            case RAIN_SENSOR:
                return "10004";
            case RAIN_DELAY:
                return "10005";
            case FREEZE_TEMPERATURE:
                return "10006";
            case REBOOT:
                return "10007";
            case SHORT:
                return "10008";
            case TEXT_MODE:
                return "99999";
        }
        throw new Exception("No existing code for this type of push notification " + type);
    }

    public static PushNotification.Type type(String code) throws Exception {
        switch (code) {
            case "1":
                return DISCONNECTED;
            case "3":
                return BACK_ONLINE;
            case "2":
                return NEW_SOFTWARE_VERSION;
            case "11111":
                return REMOTE_ACCESS;
            case "10001":
                return ZONE;
            case "10002":
                return PROGRAM;
            case "10003":
                return WEATHER;
            case "10004":
                return RAIN_SENSOR;
            case "10005":
                return RAIN_DELAY;
            case "10006":
                return FREEZE_TEMPERATURE;
            case "10007":
                return REBOOT;
            case "10008":
                return SHORT;
            case "99999":
                return TEXT_MODE;
            case "master":
                return GLOBAL;
        }
        throw new Exception("Invalid code " + code);
    }
}
