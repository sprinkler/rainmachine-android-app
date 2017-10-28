package com.rainmachine.domain.boundary.data;

public interface PrefRepository {
    boolean firstTime();

    void saveFirstTime(boolean firstTime);

    int versionCode();

    void saveVersionCode(int value);

    String currentDeviceId();

    void saveCurrentDeviceId(String currentDeviceId);

    void clearCurrentDeviceId();

    int currentDeviceType();

    void saveCurrentDeviceType(int currentDeviceType);

    void clearCurrentDeviceType();

    boolean localDiscovery();

    void saveLocalDiscovery(boolean localDiscovery);

    boolean isGlobalPushNotificationsEnabled();

    void saveGlobalPushNotificationsEnabled(boolean enabled);

    boolean shownAtLeastOneDeviceInThePast();

    void saveShownAtLeastOneDeviceInThePast(boolean shown);

    String pushToken();

    void savePushToken(String pushToken);

    void cleanupDevicePrefs(String deviceId);
}
