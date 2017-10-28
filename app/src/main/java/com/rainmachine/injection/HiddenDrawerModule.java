package com.rainmachine.injection;

import android.content.SharedPreferences;

import com.rainmachine.BuildConfig;
import com.rainmachine.data.local.pref.util.IntPreference;
import com.rainmachine.data.local.pref.util.StringPreference;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.presentation.screens.hiddendrawer.CloudDrawerModule;
import com.rainmachine.presentation.screens.hiddendrawer.PushDrawerModule;
import com.rainmachine.presentation.screens.hiddendrawer.ScanDrawerModule;
import com.rainmachine.presentation.screens.hiddendrawer.SprinklerBehaviorDrawerModule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                CloudDrawerModule.class,
                PushDrawerModule.class,
                ScanDrawerModule.class,
                SprinklerBehaviorDrawerModule.class
        },
        overrides = true
)
class HiddenDrawerModule {

    @Provides
    @Singleton
    @Named("cloud_endpoint_pref")
    StringPreference provideCloudEndpointPreference(SharedPreferences preferences) {
        return new StringPreference(preferences, "cloud_endpoint_pref",
                BuildConfig.CLOUD_SPRINKLERS_LIVE_URL);
    }

    @Provides
    @Singleton
    @Named("cloud_validate_endpoint_pref")
    StringPreference provideCloudValidateEndpointPreference(SharedPreferences preferences) {
        return new StringPreference(preferences, "cloud_validate_endpoint_pref",
                BuildConfig.CLOUD_VALIDATE_LIVE_URL);
    }

    @Provides
    @Singleton
    @Named("cloud_push_endpoint_pref")
    StringPreference provideCloudPushEndpointPreference(SharedPreferences preferences) {
        return new StringPreference(preferences, "cloud_push_endpoint_pref",
                BuildConfig.CLOUD_PUSH_LIVE_URL);
    }

    @Provides
    @Singleton
    @Named("device_cache_timeout_pref")
    IntPreference provideDeviceCacheTimeoutPreference(SharedPreferences preferences) {
        return new IntPreference(preferences, "device_cache_timeout_pref",
                DomainUtils.DEVICE_CACHE_TIMEOUT);
    }

    @Provides
    @Singleton
    @Named("cloud_endpoint")
    String provideCloudEndpoint(@Named("cloud_endpoint_pref") StringPreference endpoint) {
        return endpoint.get();
    }

    @Provides
    @Singleton
    @Named("cloud_validate_endpoint")
    String provideCloudValidateEndpoint(@Named("cloud_validate_endpoint_pref") StringPreference
                                                endpoint) {
        return endpoint.get();
    }

    @Provides
    @Singleton
    @Named("cloud_push_endpoint")
    String provideCloudPushEndpoint(@Named("cloud_push_endpoint_pref") StringPreference endpoint) {
        return endpoint.get();
    }

    @Provides
    @Singleton
    @Named("device_cache_timeout")
    int provideDeviceCacheTimeout(@Named("device_cache_timeout_pref") IntPreference
                                          deviceCacheTimeout) {
        return deviceCacheTimeout.get();
    }
}
