package com.rainmachine.presentation.screens.nearbystations;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                NearbyStationsActivity.class,
                NearbyStationsView.class,
                NearbyStationsLocationFragment.class
        }
)
class NearbyStationsModule {

    private NearbyStationsActivity activity;

    NearbyStationsModule(NearbyStationsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    NearbyStationsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    NearbyStationsPresenter providePresenter(NearbyStationsActivity activity, NearbyStationsMixer
            mixer) {
        return new NearbyStationsPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    NearbyStationsMixer provideNearbyStationsMixer(SprinklerRepositoryImpl sprinklerRepository) {
        return new NearbyStationsMixer(sprinklerRepository);
    }
}
