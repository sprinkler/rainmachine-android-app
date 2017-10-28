package com.rainmachine.presentation.screens.currentrestrictions;

import com.rainmachine.domain.usecases.restriction.GetRestrictionsDetails;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                CurrentRestrictionsActivity.class,
                CurrentRestrictionsView.class
        }
)
class CurrentRestrictionsModule {

    private CurrentRestrictionsContract.Container container;

    CurrentRestrictionsModule(CurrentRestrictionsContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    CurrentRestrictionsContract.Presenter providePresenter(GetRestrictionsDetails
                                                                   getRestrictionsDetails) {
        return new CurrentRestrictionsPresenter(container, getRestrictionsDetails);
    }
}
