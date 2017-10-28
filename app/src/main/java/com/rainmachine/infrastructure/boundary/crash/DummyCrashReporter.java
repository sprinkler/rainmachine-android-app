package com.rainmachine.infrastructure.boundary.crash;

import com.rainmachine.domain.boundary.infrastructure.CrashReporter;

public class DummyCrashReporter implements CrashReporter {

    @Override
    public void init() {
    }

    @Override
    public void logUserEmail(String email) {
    }

    @Override
    public void logDeviceVersion(String version) {
    }

    @Override
    public void logDeviceName(String deviceName) {
    }

    @Override
    public void logDeviceType(String deviceType) {
    }
}
