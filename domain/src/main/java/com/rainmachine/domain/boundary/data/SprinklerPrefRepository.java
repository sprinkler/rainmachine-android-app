package com.rainmachine.domain.boundary.data;

public interface SprinklerPrefRepository {

    String apiVersion();

    void saveApiVersion(String apiVersion);

    String sessionCookie();

    void saveSessionCookie(String sessionCookie);

    void clearSessionCookie();

    long lastUpdate();

    void saveLastUpdate(long lastUpdate);

    String username();

    void saveUsername(String username);

    String softwareVersion();

    void saveSoftwareVersion(String softwareVersion);

    String hardwareVersion();

    void saveHardwareVersion(String hardwareVersion);

    long lastCloudEmailPending();

    void saveLastCloudEmailPending(long lastCloudEmailPending);

    boolean shownCloudSetupDialog();

    void saveShownCloudSetupDialog(boolean shownCloudSetupDialog);

    boolean transitionedDeviceUnits();

    void saveTransitionedDeviceUnits(boolean transitioned);

    boolean migratedToNewFirebaseFormat();

    void saveMigratedToNewFirebaseFormat(boolean migrated);
}
