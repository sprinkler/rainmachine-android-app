package com.rainmachine.presentation.screens.weathersensitivity;

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
                WeatherSensitivityActivity.class,
                WeatherSensitivityView.class
        }
)
class WeatherSensitivityModule {

    private WeatherSensitivityActivity activity;

    WeatherSensitivityModule(WeatherSensitivityActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WeatherSensitivityActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    WeatherSensitivityPresenter providePresenter(WeatherSensitivityActivity activity,
                                                 WeatherSensitivityMixer mixer) {
        return new WeatherSensitivityPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    WeatherSensitivityMixer provideWeatherSensitivityMixer(SprinklerRepositoryImpl
                                                                   sprinklerRepository) {
        return new WeatherSensitivityMixer(sprinklerRepository);
    }
}
