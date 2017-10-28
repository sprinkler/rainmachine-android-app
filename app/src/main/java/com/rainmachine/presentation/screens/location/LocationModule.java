package com.rainmachine.presentation.screens.location;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.domain.usecases.backup.GetBackups;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.LocationHandler;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                LocationActivity.class,
                LocationFragment.class,
                ActionMessageDialogFragment.class,
                EnterAddressDialogFragment.class,
                GoogleErrorDialogFragment.class
        }
)
class LocationModule {

    private LocationContract.Container container;

    LocationModule(LocationContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(LocationContract
                                                                                    .Presenter
                                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    GoogleErrorDialogFragment.Callback provideGoogleErrorDialogCallback() {
        return container;
    }

    @Provides
    @Singleton
    LocationContract.Presenter providePresenter(Bus bus, LocationHandler locationHandler,
                                                LocationMixer mixer) {
        return new LocationPresenter(container, bus, locationHandler, mixer);
    }

    @Provides
    @Singleton
    LocationMixer provideLocationMixer(SprinklerRepositoryImpl sprinklerRepository,
                                       GoogleApiDelegate googleApiDelegate, GetBackups
                                               getBackups, Features features) {
        return new LocationMixer(sprinklerRepository, googleApiDelegate, getBackups, features);
    }
}
