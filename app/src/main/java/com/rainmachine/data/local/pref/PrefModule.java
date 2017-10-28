package com.rainmachine.data.local.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.util.BooleanPreference;
import com.rainmachine.data.local.pref.util.IntPreference;
import com.rainmachine.data.local.pref.util.StringPreference;
import com.rainmachine.domain.boundary.data.PrefRepository;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class PrefModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences("prefs_utils", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    PrefRepository providePrefsRepository(SharedPreferences preferences) {
        return new PrefRepositoryImpl(preferences);
    }

    @Provides
    @Named("first_time")
    @Singleton
    BooleanPreference provideFirstTimePreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "first_time", true);
    }

    @Provides
    @Named("version_code")
    @Singleton
    IntPreference provideVersionCodePreference(SharedPreferences preferences) {
        return new IntPreference(preferences, "version_code", 0);
    }

    @Provides
    @Singleton
    @Named("current_device_id")
    StringPreference provideCurrentDeviceIdPreference(SharedPreferences preferences) {
        return new StringPreference(preferences, "current_device_id");
    }

    @Provides
    @Singleton
    @Named("current_device_type")
    IntPreference provideCurrentDeviceTypePreference(SharedPreferences preferences) {
        return new IntPreference(preferences, "current_device_type", Device.SPRINKLER_TYPE_UDP);
    }

    @Provides
    @Singleton
    @Named("local_discovery_setting")
    BooleanPreference provideLocalDiscoveryPreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "local_discovery_setting", true);
    }

    @Provides
    @Singleton
    @Named("notifications")
    BooleanPreference provideNotificationsPreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "notifications", true);
    }

    @Provides
    @Singleton
    @Named("shown_at_least_one_device")
    BooleanPreference provideShownAtLeastOneDevicePreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "shown_at_least_one_device", false);
    }

    @Provides
    @Named("push_token")
    @Singleton
    StringPreference providePushTokenPreference(SharedPreferences preferences) {
        return new StringPreference(preferences, "push_token", null);
    }

    @Provides
    @Singleton
    @Named("migrated_to_new_firebase_format")
    BooleanPreference provideMigratedToNewFirebaseFormatPreference(SharedPreferences preferences) {
        return new BooleanPreference(preferences, "migrated_to_new_firebase_format", false);
    }
}
