package com.rainmachine.presentation.screens.weathersettings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WeatherSettingsActivity.class,
                WeatherSettingsView.class,
                ActionMessageDialogFragment.class
        }
)
class WeatherSettingsModule {

    private WeatherSettingsContract.Container container;

    WeatherSettingsModule(WeatherSettingsContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    WeatherSettingsContract.Container provideContainer() {
        return container;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (WeatherSettingsContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    WeatherSettingsContract.Presenter providePresenter(WeatherSettingsContract.Container container,
                                                       WeatherSettingsMixer mixer) {
        return new WeatherSettingsPresenter(container, mixer);
    }

    @Provides
    @Singleton
    WeatherSettingsMixer provideWeatherMixer(SprinklerRepositoryImpl sprinklerRepository,
                                             GoogleApiDelegate googleApiDelegate) {
        return new WeatherSettingsMixer(sprinklerRepository, googleApiDelegate);
    }
}
