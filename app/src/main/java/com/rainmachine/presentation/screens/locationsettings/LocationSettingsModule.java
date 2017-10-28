package com.rainmachine.presentation.screens.locationsettings;

import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                LocationSettingsActivity.class
        }
)
class LocationSettingsModule {

    private LocationSettingsActivity activity;

    LocationSettingsModule(LocationSettingsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    LocationSettingsActivity provideActivity() {
        return activity;
    }
}
