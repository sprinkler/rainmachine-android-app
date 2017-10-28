package com.rainmachine.infrastructure.boundary.analytics;

import com.rainmachine.domain.boundary.infrastructure.Analytics;

public class DummyAnalyticsImpl implements Analytics {

    @Override
    public void trackDevice(String deviceId, String apiVersion, String softwareVersion, String
            hardwareVersion) {
    }
}
