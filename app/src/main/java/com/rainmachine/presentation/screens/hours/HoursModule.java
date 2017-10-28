package com.rainmachine.presentation.screens.hours;

import android.content.Context;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                HoursActivity.class,
                HoursView.class
        }
)
class HoursModule {

    private HoursActivity activity;

    HoursModule(HoursActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    HoursActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    HoursPresenter providePresenter(HoursActivity activity, HoursMixer mixer) {
        return new HoursPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    HoursMixer provideHoursMixer(Context context, SprinklerRepositoryImpl sprinklerRepository,
                                 GetRestrictionsLive getRestrictionsLive) {
        return new HoursMixer(context, sprinklerRepository, getRestrictionsLive);
    }
}
