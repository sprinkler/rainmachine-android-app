package com.rainmachine.presentation.screens.programdetailsold;

import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsOldActivity.class,
                ProgramDetailsOldView.class,
                DatePickerDialogFragment.class,
                ZoneDurationDialogFragment.class,
                ActionMessageDialogFragment.class,
                CycleSoakDialogFragment.class,
                StationDelayDialogFragment.class,
                TimePickerDialogFragment.class,
                MultiChoiceDialogFragment.class,
                SunriseSunsetDialogFragment.class
        }
)
class ProgramDetailsOldModule {

    private ProgramDetailsOldActivity activity;

    ProgramDetailsOldModule(ProgramDetailsOldActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    ProgramDetailsOldActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    DatePickerDialogFragment.Callback provideDatePickerCallback(ProgramDetailsOldPresenter
                                                                        programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    ZoneDurationDialogFragment.Callback provideMinutesZoneDialogCallback(ProgramDetailsOldPresenter
                                                                                 programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (ProgramDetailsOldPresenter

                     programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    CycleSoakDialogFragment.Callback provideCycleSoakDialogCallback(ProgramDetailsOldPresenter
                                                                            programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    SunriseSunsetDialogFragment.Callback provideSunriseSunsetDialogCallback
            (ProgramDetailsOldPresenter programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    StationDelayDialogFragment.Callback provideStationDelayDialogCallback(ProgramDetailsOldPresenter
                                                                                  programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    TimePickerDialogFragment.Callback provideTimePickerDialogCallback(ProgramDetailsOldPresenter
                                                                              programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    MultiChoiceDialogFragment.Callback provideMultiChoiceDialogCallback(ProgramDetailsOldPresenter
                                                                                programDetailsOldPresenter) {
        return programDetailsOldPresenter;
    }

    @Provides
    @Singleton
    ProgramDetailsOldPresenter providePresenter(ProgramDetailsOldActivity activity, Bus bus,
                                                Features features, SaveProgram saveProgram) {
        return new ProgramDetailsOldPresenter(activity, features, saveProgram);
    }
}
