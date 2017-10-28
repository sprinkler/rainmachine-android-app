package com.rainmachine.presentation.screens.rainsensor;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsDetails;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                RainSensorActivity.class,
                RainSensorView.class,
                RadioOptionsDialogFragment.class
        }
)
class RainSensorModule {

    private RainSensorContract.Container container;

    RainSensorModule(RainSensorContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    RainSensorContract.Presenter providePresenter(RainSensorMixer mixer, GetRestrictionsDetails
            getRestrictionsDetails, Features features) {
        return new RainSensorPresenter(container, mixer, getRestrictionsDetails, features);
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideCallback(RainSensorContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RainSensorMixer provideRainSensorMixer(SprinklerRepositoryImpl sprinklerRepository,
                                           GetRestrictionsLive getRestrictionsLive,
                                           Features features) {
        return new RainSensorMixer(sprinklerRepository, getRestrictionsLive, features);
    }
}
