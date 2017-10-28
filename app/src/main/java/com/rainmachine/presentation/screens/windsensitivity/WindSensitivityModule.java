package com.rainmachine.presentation.screens.windsensitivity;

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
                WindSensitivityActivity.class,
                WindSensitivityView.class
        }
)
class WindSensitivityModule {

    private WindSensitivityActivity activity;

    WindSensitivityModule(WindSensitivityActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WindSensitivityActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    WindSensitivityPresenter providePresenter(WindSensitivityActivity activity,
                                              WindSensitivityMixer mixer) {
        return new WindSensitivityPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    WindSensitivityMixer provideWindSensitivityMixer(SprinklerRepositoryImpl sprinklerRepository) {
        return new WindSensitivityMixer(sprinklerRepository);
    }
}
