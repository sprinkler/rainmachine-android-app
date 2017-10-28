package com.rainmachine.presentation.screens.softwareupdate;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                SoftwareUpdateActivity.class,
                SoftwareUpdateView.class
        }
)
class SoftwareUpdateModule {

    private SoftwareUpdateActivity activity;

    SoftwareUpdateModule(SoftwareUpdateActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    SoftwareUpdateActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    SoftwareUpdatePresenter providePresenter(SoftwareUpdateActivity activity,
                                             SoftwareUpdateMixer mixer) {
        return new SoftwareUpdatePresenter(activity, mixer);
    }

    @Provides
    @Singleton
    SoftwareUpdateMixer provideSoftwareUpdateMixer(Features features, UpdateHandler
            updateHandler, SprinklerRepositoryImpl sprinklerRepository, TriggerUpdateCheck
                                                           triggerUpdateCheck) {
        return new SoftwareUpdateMixer(features, updateHandler, triggerUpdateCheck,
                sprinklerRepository);
    }
}
