package com.rainmachine.presentation.screens.raindelay;

import android.content.Context;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                RainDelayActivity.class,
                RainDelayView.class
        }
)
class RainDelayModule {

    private RainDelayContract.Container container;

    RainDelayModule(RainDelayContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    RainDelayContract.Presenter providePresenter(RainDelayMixer mixer) {
        return new RainDelayPresenter(container, mixer);
    }

    @Provides
    @Singleton
    RainDelayMixer provideRainDelayMixer(Context context, Features features,
                                         SprinklerRepositoryImpl sprinklerRepository,
                                         GetRestrictionsLive getRestrictionsLive) {
        return new RainDelayMixer(context, features, sprinklerRepository, getRestrictionsLive);
    }
}
