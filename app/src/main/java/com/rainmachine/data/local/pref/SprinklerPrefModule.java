package com.rainmachine.data.local.pref;

import android.content.SharedPreferences;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.util.BooleanPreference;
import com.rainmachine.data.local.pref.util.LongPreference;
import com.rainmachine.data.local.pref.util.StringPreference;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class SprinklerPrefModule {

    @Provides
    @Singleton
    @Named("api_version")
    StringPreference provideApiVersionPreference(SharedPreferences preferences,
                                                 Device device) {
        return new StringPreference(preferences, device.deviceId + "_api_version");
    }

    @Provides
    @Singleton
    @Named("session_cookie")
    StringPreference provideSessionCookiePreference(SharedPreferences preferences,
                                                    Device device) {
        return new StringPreference(preferences, device.deviceId + "_session_cookie");
    }

    @Provides
    @Singleton
    @Named("last_update")
    LongPreference provideLastUpdatePreference(SharedPreferences preferences,
                                               Device device) {
        return new LongPreference(preferences, device.deviceId + "_last_update", 0L);
    }

    @Provides
    @Singleton
    @Named("username")
    StringPreference provideUsernamePreference(SharedPreferences preferences,
                                               Device device) {
        return new StringPreference(preferences, device.deviceId + "_username", "admin");
    }

    @Provides
    @Singleton
    @Named("software_version")
    StringPreference provideSoftwareVersionPreference(SharedPreferences preferences,
                                                      Device device) {
        return new StringPreference(preferences, device.deviceId + "_software_version");
    }

    @Provides
    @Singleton
    @Named("hardware_version")
    StringPreference provideHardwareVersionPreference(SharedPreferences preferences,
                                                      Device device) {
        return new StringPreference(preferences, device.deviceId + "_hardware_version");
    }

    @Provides
    @Singleton
    @Named("last_cloud_email_pending")
    LongPreference provideLastCloudEmailPendingPreference(SharedPreferences preferences,
                                                          Device device) {
        return new LongPreference(preferences, device.deviceId + "_last_cloud_email_pending", 0L);
    }

    @Provides
    @Singleton
    @Named("shown_cloud_setup_dialog")
    BooleanPreference provideShownCloudSetupDialogPreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "shown_cloud_setup_dialog", false);
    }

    @Provides
    @Singleton
    @Named("transitioned_device_units")
    BooleanPreference provideTransitionedDeviceUnitsPreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "transitioned_device_units", false);
    }
}
