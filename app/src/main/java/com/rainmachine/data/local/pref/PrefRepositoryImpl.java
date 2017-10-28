package com.rainmachine.data.local.pref;

import android.content.SharedPreferences;

import com.rainmachine.data.local.pref.util.BooleanPreference;
import com.rainmachine.data.local.pref.util.IntPreference;
import com.rainmachine.data.local.pref.util.StringPreference;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;
import javax.inject.Named;

public class PrefRepositoryImpl implements PrefRepository {

    private SharedPreferences preferences;

    @Inject
    @Named("first_time")
    BooleanPreference firstTimePreference;
    @Inject
    @Named("version_code")
    IntPreference versionCodePreference;
    @Inject
    @Named("current_device_id")
    StringPreference currentDeviceIdPreference;
    @Inject
    @Named("current_device_type")
    IntPreference currentDeviceTypePreference;
    @Inject
    @Named("local_discovery_setting")
    BooleanPreference localDiscoveryPreference;
    @Inject
    @Named("notifications")
    BooleanPreference notificationsPreference;
    @Inject
    @Named("shown_at_least_one_device")
    BooleanPreference shownAtLeastOneDevicePreference;
    @Inject
    @Named("push_token")
    StringPreference pushTokenPreference;

    public PrefRepositoryImpl(SharedPreferences preferences) {
        this.preferences = preferences;
        Injector.inject(this);
    }

    @Override
    public boolean firstTime() {
        return firstTimePreference.get();
    }

    @Override
    public void saveFirstTime(boolean firstTime) {
        firstTimePreference.set(firstTime);
    }

    @Override
    public int versionCode() {
        return versionCodePreference.get();
    }

    @Override
    public void saveVersionCode(int value) {
        versionCodePreference.set(value);
    }

    @Override
    public String currentDeviceId() {
        return currentDeviceIdPreference.get();
    }

    @Override
    public void saveCurrentDeviceId(String currentDeviceId) {
        this.currentDeviceIdPreference.set(currentDeviceId);
    }

    @Override
    public void clearCurrentDeviceId() {
        currentDeviceIdPreference.delete();
    }

    @Override
    public int currentDeviceType() {
        return currentDeviceTypePreference.get();
    }

    @Override
    public void saveCurrentDeviceType(int currentDeviceType) {
        this.currentDeviceTypePreference.set(currentDeviceType);
    }

    @Override
    public void clearCurrentDeviceType() {
        currentDeviceTypePreference.delete();
    }

    @Override
    public boolean localDiscovery() {
        return localDiscoveryPreference.get();
    }

    @Override
    public void saveLocalDiscovery(boolean localDiscovery) {
        this.localDiscoveryPreference.set(localDiscovery);
    }

    /**
     * This saves the global push notification status
     */
    @Override
    public boolean isGlobalPushNotificationsEnabled() {
        return notificationsPreference.get();
    }

    @Override
    public void saveGlobalPushNotificationsEnabled(boolean enabled) {
        notificationsPreference.set(enabled);
    }

    @Override
    public boolean shownAtLeastOneDeviceInThePast() {
        return shownAtLeastOneDevicePreference.get();
    }

    @Override
    public void saveShownAtLeastOneDeviceInThePast(boolean shown) {
        shownAtLeastOneDevicePreference.set(shown);
    }

    @Override
    public String pushToken() {
        return pushTokenPreference.get();
    }

    @Override
    public void savePushToken(String pushToken) {
        pushTokenPreference.set(pushToken);
    }

    @Override
    public void cleanupDevicePrefs(String deviceId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(deviceId + "_api_version");
        editor.remove(deviceId + "_session_cookie");
        editor.remove(deviceId + "_last_update");
        editor.remove(deviceId + "_last_manual_update");
        editor.remove(deviceId + "_username");
        editor.remove(deviceId + "_software_version");
        editor.remove(deviceId + "_hardware_version");
        editor.remove(deviceId + "_last_cloud_email_pending");
        editor.remove(deviceId + "shown_cloud_setup_dialog");
        editor.apply();
    }
}
