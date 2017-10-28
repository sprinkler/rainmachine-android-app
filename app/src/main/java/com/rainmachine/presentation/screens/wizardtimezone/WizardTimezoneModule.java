package com.rainmachine.presentation.screens.wizardtimezone;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WizardTimezoneActivity.class,
                WizardTimezoneView.class,
                DatePickerDialogFragment.class,
                TimePickerDialogFragment.class,
                TimezoneDialogFragment.class
        }
)
class WizardTimezoneModule {

    private WizardTimezoneActivity activity;

    WizardTimezoneModule(WizardTimezoneActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WizardTimezoneActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    DatePickerDialogFragment.Callback provideDatePickerCallback(WizardTimezonePresenter
                                                                        wizardTimezonePresenter) {
        return wizardTimezonePresenter;
    }

    @Provides
    @Singleton
    TimePickerDialogFragment.Callback provideTimePickerCallback(WizardTimezonePresenter
                                                                        wizardTimezonePresenter) {
        return wizardTimezonePresenter;
    }

    @Provides
    @Singleton
    TimezoneDialogFragment.Callback provideTimezoneDialogCallback(WizardTimezonePresenter
                                                                          wizardTimezonePresenter) {
        return wizardTimezonePresenter;
    }

    @Provides
    @Singleton
    WizardTimezonePresenter providePresenter(WizardTimezoneActivity activity, Device device,
                                             WizardTimezoneMixer mixer) {
        return new WizardTimezonePresenter(activity, device, mixer);
    }

    @Provides
    @Singleton
    WizardTimezoneMixer provideWizardTimezoneMixer(SprinklerRepositoryImpl sprinklerRepository) {
        return new WizardTimezoneMixer(sprinklerRepository);
    }
}
