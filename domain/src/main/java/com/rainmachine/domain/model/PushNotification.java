package com.rainmachine.domain.model;


public class PushNotification {
    public Type type;
    public boolean enabled;

    public PushNotification() {
    }

    public PushNotification(Type type, boolean enabled) {
        this.type = type;
        this.enabled = enabled;
    }

    // The order of the type values is important because we sort based on this
    public enum Type {
        GLOBAL, DISCONNECTED, BACK_ONLINE, NEW_SOFTWARE_VERSION, REMOTE_ACCESS, ZONE, PROGRAM,
        WEATHER, RAIN_SENSOR, RAIN_DELAY, FREEZE_TEMPERATURE, REBOOT, SHORT, TEXT_MODE
    }
}
