package com.rainmachine.presentation.screens.physicaltouch;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.auth.LogInDefault;
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
                PhysicalTouchActivity.class,
                PhysicalTouchView.class
        }
)
class PhysicalTouchModule {

    private PhysicalTouchActivity activity;

    PhysicalTouchModule(PhysicalTouchActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    PhysicalTouchActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    PhysicalTouchPresenter providePresenter(PhysicalTouchActivity activity, Features features,
                                            PhysicalTouchMixer mixer) {
        return new PhysicalTouchPresenter(activity, features, mixer);
    }

    @Provides
    @Singleton
    PhysicalTouchMixer providePhysicalTouchMixer(SprinklerRepositoryImpl sprinklerRepository,
                                                 LogInDefault logInDefault) {
        return new PhysicalTouchMixer(sprinklerRepository, logInDefault);
    }
}
