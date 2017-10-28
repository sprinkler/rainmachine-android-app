package com.rainmachine.infrastructure.boundary.analytics;

class DeviceVersions {
    String apiVersion;
    String softwareVersion;
    String hardwareVersion;
    String history;

    DeviceVersions(String apiVersion, String softwareVersion, String hardwareVersion, String
            history) {
        this.apiVersion = apiVersion;
        this.softwareVersion = softwareVersion;
        this.hardwareVersion = hardwareVersion;
        this.history = history;
    }
}
