package com.rainmachine.presentation.screens.weathersourcedetails;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.netatmo.NetatmoApiDelegate;
import com.rainmachine.data.remote.wunderground.WundergroundApiDelegate;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.MultiChoiceDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WeatherSourceDetailsActivity.class,
                WeatherSourceDetailsView.class,
                WUndergroundParamsView.class,
                NetatmoParamsView.class,
                WeatherSourceParamsDialogFragment.class,
                MultiChoiceDialogFragment.class
        }
)
class WeatherSourceDetailsModule {

    private WeatherSourceDetailsActivity activity;

    WeatherSourceDetailsModule(WeatherSourceDetailsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WeatherSourceDetailsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    MultiChoiceDialogFragment.Callback provideMultiChoiceDialogCallback
            (WeatherSourceDetailsPresenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    WeatherSourceDetailsPresenter providePresenter(WeatherSourceDetailsActivity activity,
                                                   WeatherSourceDetailsMixer mixer) {
        return new WeatherSourceDetailsPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    WeatherSourceDetailsMixer provideWeatherSourceDetailsMixer(SprinklerRepositoryImpl
                                                                       sprinklerRepository,
                                                               NetatmoApiDelegate
                                                                       netatmoApiDelegate,
                                                               WundergroundApiDelegate
                                                                       wundergroundApiDelegate) {
        return new WeatherSourceDetailsMixer(sprinklerRepository, netatmoApiDelegate,
                wundergroundApiDelegate);
    }
}
