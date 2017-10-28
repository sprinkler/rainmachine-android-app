package com.rainmachine.presentation.screens.programdetailsfrequency;

import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.DatePickerDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsFrequencyActivity.class,
                ProgramDetailsFrequencyView.class,
                RadioOptionsDialogFragment.class,
                DatePickerDialogFragment.class,
                SelectedDaysDialogFragment.class
        }
)
class ProgramDetailsFrequencyModule {

    private ProgramDetailsFrequencyContract.Container container;

    ProgramDetailsFrequencyModule(ProgramDetailsFrequencyContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ProgramDetailsFrequencyContract.Container provideContainer() {
        return container;
    }

    @Provides
    @Singleton
    ProgramDetailsFrequencyContract.Presenter providePresenter(ProgramDetailsFrequencyContract
                                                                       .Container container) {
        return new ProgramDetailsFrequencyPresenter(container);
    }

    @Provides
    @Singleton
    SelectedDaysDialogFragment.Callback provideSelectedDaysDialogCallback
            (ProgramDetailsFrequencyContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideRadioOptionsDialogCallback
            (ProgramDetailsFrequencyContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    DatePickerDialogFragment.Callback provideDatePickerDialogCallback
            (ProgramDetailsFrequencyContract.Presenter presenter) {
        return presenter;
    }
}
