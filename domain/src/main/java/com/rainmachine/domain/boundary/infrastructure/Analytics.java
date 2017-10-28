package com.rainmachine.domain.boundary.infrastructure;

public interface Analytics {

    void trackDevice(String deviceId, String apiVersion, String softwareVersion, String
            hardwareVersion);
}
