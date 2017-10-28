package com.rainmachine.domain.boundary.infrastructure;

import com.rainmachine.domain.model.LatLong;

import java.io.IOException;

public interface InfrastructureService {

    String getSystemId();

    String getPhoneId();

    String getInstaller();

    int getCurrentVersionCode();

    String getUpdatedPushToken() throws IOException;

    void scheduleUpdatePushNotificationsSettingsRetry();

    void scheduleDeleteZoneImageRetry(String macAddress, long zoneId, LatLong coordinates);

    void scheduleUploadZoneImageRetry(String macAddress, long zoneId, LatLong coordinates);

    byte[] getResizedImageWithLargestSide(String imagePath, int side);

    byte[] getImageAsBytes(String imagePath);
}
