package com.rainmachine.presentation.screens.programdetailsadvanced;

import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ClickableRadioOptionsDialogFragment;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.CycleSoakDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.StationDelayDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsAdvancedActivity.class,
                ProgramDetailsAdvancedView.class,
                RadioOptionsDialogFragment.class,
                StationDelayDialogFragment.class,
                CycleSoakDialogFragment.class,
                ClickableRadioOptionsDialogFragment.class
        }
)
class ProgramDetailsAdvancedModule {

    private ProgramDetailsAdvancedContract.Container container;

    ProgramDetailsAdvancedModule(ProgramDetailsAdvancedContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ProgramDetailsAdvancedContract.Container provideContainer() {
        return container;
    }

    @Provides
    @Singleton
    ProgramDetailsAdvancedContract.Presenter providePresenter(ProgramDetailsAdvancedContract
                                                                      .Container container) {
        return new ProgramDetailsAdvancedPresenter(container);
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideRadioOptionsDialogCallback
            (ProgramDetailsAdvancedContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    ClickableRadioOptionsDialogFragment.Callback provideClickableRadioOptionsDialogCallback
            (ProgramDetailsAdvancedContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    StationDelayDialogFragment.Callback provideStationDelayDialogCallback
            (ProgramDetailsAdvancedContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    CycleSoakDialogFragment.Callback provideCycleSoakDialogCallback(
            ProgramDetailsAdvancedContract.Presenter presenter) {
        return presenter;
    }
}
