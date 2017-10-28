package com.rainmachine.presentation.screens.rainsensitivity;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                RainSensitivityActivity.class,
                RainSensitivityView.class
        }
)
class RainSensitivityModule {

    private RainSensitivityActivity activity;

    RainSensitivityModule(RainSensitivityActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    RainSensitivityActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    RainSensitivityPresenter providePresenter(RainSensitivityActivity activity,
                                              RainSensitivityMixer mixer) {
        return new RainSensitivityPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    RainSensitivityMixer provideRainSensitivityMixer(SprinklerRepositoryImpl sprinklerRepository) {
        return new RainSensitivityMixer(sprinklerRepository);
    }
}
