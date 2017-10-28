package com.rainmachine.presentation.screens.wizardpassword;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WizardPasswordActivity.class,
                WizardPasswordView.class,
        }
)
class WizardPasswordModule {

    private WizardPasswordActivity activity;

    WizardPasswordModule(WizardPasswordActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WizardPasswordActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    WizardPasswordPresenter providePresenter(WizardPasswordActivity activity,
                                             WizardPasswordMixer mixer) {
        return new WizardPasswordPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    WizardPasswordMixer provideWizardPasswordMixer(SprinklerState sprinklerState,
                                                   SprinklerRepositoryImpl sprinklerRepository,
                                                   DatabaseRepositoryImpl databaseRepository) {
        return new WizardPasswordMixer(sprinklerRepository, databaseRepository, sprinklerState);
    }
}
