package com.rainmachine.presentation.screens.statsdetails;

import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.screens.stats.StatsMixer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                StatsDetailsActivity.class,
                StatsDetailsView.class
        }
)
class StatsDetailsModule {

    private StatsDetailsActivity activity;

    StatsDetailsModule(StatsDetailsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    StatsDetailsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    StatsDetailsPresenter providePresenter(StatsDetailsActivity activity, StatsMixer mixer) {
        return new StatsDetailsPresenter(activity, mixer);
    }
}
