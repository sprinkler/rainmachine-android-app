package com.rainmachine.data.local.pref;

import com.rainmachine.data.local.pref.util.BooleanPreference;
import com.rainmachine.data.local.pref.util.LongPreference;
import com.rainmachine.data.local.pref.util.StringPreference;
import com.rainmachine.domain.boundary.data.SprinklerPrefRepository;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;
import javax.inject.Named;

public class SprinklerPrefRepositoryImpl implements SprinklerPrefRepository {

    @Inject
    @Named("api_version")
    StringPreference apiVersionPreference;
    @Inject
    @Named("session_cookie")
    StringPreference sessionCookiePreference;
    @Inject
    @Named("last_update")
    LongPreference lastUpdatePreference;
    @Inject
    @Named("username")
    StringPreference usernamePreference;
    @Inject
    @Named("software_version")
    StringPreference softwareVersionPreference;
    @Inject
    @Named("hardware_version")
    StringPreference hardwareVersionPreference;
    @Inject
    @Named("last_cloud_email_pending")
    LongPreference lastCloudEmailPendingPreference;
    @Inject
    @Named("shown_cloud_setup_dialog")
    BooleanPreference shownCloudSetupDialogPreference;
    @Inject
    @Named("transitioned_device_units")
    BooleanPreference transitionedDeviceUnitsPreference;
    @Inject
    @Named("migrated_to_new_firebase_format")
    BooleanPreference migratedToNewFirebaseFormatPreference;

    public SprinklerPrefRepositoryImpl() {
        Injector.injectSprinklerGraph(this);
    }

    @Override
    public String apiVersion() {
        return apiVersionPreference.get();
    }

    @Override
    public void saveApiVersion(String apiVersion) {
        apiVersionPreference.set(apiVersion);
    }

    @Override
    public String sessionCookie() {
        return sessionCookiePreference.get();
    }

    @Override
    public void saveSessionCookie(String sessionCookie) {
        sessionCookiePreference.set(sessionCookie);
    }

    @Override
    public void clearSessionCookie() {
        sessionCookiePreference.delete();
    }

    @Override
    public long lastUpdate() {
        return lastUpdatePreference.get();
    }

    @Override
    public void saveLastUpdate(long lastUpdate) {
        lastUpdatePreference.set(lastUpdate);
    }

    @Override
    public String username() {
        return usernamePreference.get();
    }

    @Override
    public void saveUsername(String username) {
        usernamePreference.set(username);
    }

    @Override
    public String softwareVersion() {
        return softwareVersionPreference.get();
    }

    @Override
    public void saveSoftwareVersion(String softwareVersion) {
        softwareVersionPreference.set(softwareVersion);
    }

    @Override
    public String hardwareVersion() {
        return hardwareVersionPreference.get();
    }

    @Override
    public void saveHardwareVersion(String hardwareVersion) {
        hardwareVersionPreference.set(hardwareVersion);
    }

    @Override
    public long lastCloudEmailPending() {
        return lastCloudEmailPendingPreference.get();
    }

    @Override
    public void saveLastCloudEmailPending(long lastCloudEmailPending) {
        lastCloudEmailPendingPreference.set(lastCloudEmailPending);
    }

    @Override
    public boolean shownCloudSetupDialog() {
        return shownCloudSetupDialogPreference.get();
    }

    @Override
    public void saveShownCloudSetupDialog(boolean shownCloudSetupDialog) {
        shownCloudSetupDialogPreference.set(shownCloudSetupDialog);
    }

    @Override
    public boolean transitionedDeviceUnits() {
        return transitionedDeviceUnitsPreference.get();
    }

    @Override
    public void saveTransitionedDeviceUnits(boolean transitioned) {
        transitionedDeviceUnitsPreference.set(transitioned);
    }

    @Override
    public boolean migratedToNewFirebaseFormat() {
        return migratedToNewFirebaseFormatPreference.get();
    }

    @Override
    public void saveMigratedToNewFirebaseFormat(boolean migrated) {
        migratedToNewFirebaseFormatPreference.set(migrated);
    }
}
