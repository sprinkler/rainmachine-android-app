package com.rainmachine.domain.boundary.infrastructure;

public interface CrashReporter {

    void init();

    void logUserEmail(String email);

    void logDeviceVersion(String version);

    void logDeviceName(String deviceName);

    void logDeviceType(String deviceType);
}
