package com.rainmachine.presentation.screens.programdetails;

import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsActivity.class,
                ProgramDetailsView.class,
                ActionMessageDialogFragment.class
        }
)
class ProgramDetailsModule {

    private ProgramDetailsContract.Container container;

    ProgramDetailsModule(ProgramDetailsContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ProgramDetailsContract.Presenter providePresenter(SaveProgram saveProgram) {
        return new ProgramDetailsPresenter(container, saveProgram);
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (ProgramDetailsContract.Presenter presenter) {
        return presenter;
    }
}
