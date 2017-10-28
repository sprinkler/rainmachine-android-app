package com.rainmachine.presentation.screens.programdetailszones;

import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                ProgramDetailsZonesActivity.class,
                ProgramDetailsZonesView.class,
                ZoneDurationDialogFragment.class,
                ProgramDetailsZonesSuggestedDialogFragment.class
        }
)
class ProgramDetailsZonesModule {

    private ProgramDetailsZonesContract.Container container;

    ProgramDetailsZonesModule(ProgramDetailsZonesContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ProgramDetailsZonesContract.Container provideContainer() {
        return container;
    }

    @Provides
    @Singleton
    ProgramDetailsZonesContract.Presenter providePresenter(ProgramDetailsZonesContract.Container
                                                                   container) {
        return new ProgramDetailsZonesPresenter(container);
    }

    @Provides
    @Singleton
    ZoneDurationDialogFragment.Callback provideMinutesZoneDialogCallback(ProgramDetailsZonesContract
                                                                                 .Presenter
                                                                                 presenter) {
        return presenter;
    }
}
