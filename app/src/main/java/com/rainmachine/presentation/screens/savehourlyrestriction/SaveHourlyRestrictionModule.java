package com.rainmachine.presentation.screens.savehourlyrestriction;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                SaveHourlyRestrictionActivity.class,
                SaveHourlyRestrictionView.class,
                TimePickerDialogFragment.class,
                MultiChoiceDialogFragment.class
        }
)
class SaveHourlyRestrictionModule {

    private SaveHourlyRestrictionActivity activity;

    SaveHourlyRestrictionModule(SaveHourlyRestrictionActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    SaveHourlyRestrictionActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    TimePickerDialogFragment.Callback provideTimePickerDialogCallback
            (SaveHourlyRestrictionPresenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    MultiChoiceDialogFragment.Callback provideMultiChoiceDialogCallback
            (SaveHourlyRestrictionPresenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    SaveHourlyRestrictionPresenter providePresenter(SaveHourlyRestrictionActivity activity,
                                                    SaveHourlyRestrictionMixer mixer) {
        return new SaveHourlyRestrictionPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    SaveHourlyRestrictionMixer provideSaveHourlyRestrictionMixer(SprinklerRepositoryImpl
                                                                         sprinklerRepository,
                                                                 GetRestrictionsLive
                                                                         getRestrictionsLive,
                                                                 Features features) {
        return new SaveHourlyRestrictionMixer(sprinklerRepository, getRestrictionsLive, features);
    }
}
