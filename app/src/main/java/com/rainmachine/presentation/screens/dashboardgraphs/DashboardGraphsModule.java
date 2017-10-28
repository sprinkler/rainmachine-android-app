package com.rainmachine.presentation.screens.dashboardgraphs;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
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
                DashboardGraphsActivity.class,
                DashboardGraphsView.class
        }
)
class DashboardGraphsModule {

    private DashboardGraphsActivity activity;

    DashboardGraphsModule(DashboardGraphsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    DashboardGraphsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    DashboardGraphsPresenter providePresenter(DashboardGraphsActivity activity,
                                              DashboardGraphsMixer mixer) {
        return new DashboardGraphsPresenter(mixer);
    }

    @Provides
    @Singleton
    DashboardGraphsMixer provideDashboardGraphsMixer(SprinklerRepositoryImpl sprinklerRepository,
                                                     Features features) {
        return new DashboardGraphsMixer(sprinklerRepository, features);
    }
}
