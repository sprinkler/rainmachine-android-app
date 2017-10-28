package com.rainmachine.presentation.screens.programdetailsstarttime;

import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.TimePickerDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.SunriseSunsetDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsStartTimeActivity.class,
                ProgramDetailsStartTimeView.class,
                SunriseSunsetDialogFragment.class,
                TimePickerDialogFragment.class,
        }
)
class ProgramDetailsStartTimeModule {

    private ProgramDetailsStartTimeContract.Container container;

    ProgramDetailsStartTimeModule(ProgramDetailsStartTimeContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ProgramDetailsStartTimeContract.Container provideContainer() {
        return container;
    }

    @Provides
    @Singleton
    ProgramDetailsStartTimeContract.Presenter providePresenter(ProgramDetailsStartTimeContract
                                                                       .Container
                                                                       container) {
        return new ProgramDetailsStartTimePresenter(container);
    }

    @Provides
    @Singleton
    SunriseSunsetDialogFragment.Callback provideSunriseSunsetDialogCallback
            (ProgramDetailsStartTimeContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    TimePickerDialogFragment.Callback provideTimePickerDialogCallback
            (ProgramDetailsStartTimeContract.Presenter presenter) {
        return presenter;
    }
}
