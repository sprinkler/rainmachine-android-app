package com.rainmachine.presentation.screens.weathersources;

import android.content.Context;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WeatherSourcesActivity.class,
                WeatherSourceDialogFragment.class,
                WeatherSourcesView.class
        }
)
class WeatherSourcesModule {

    private WeatherSourcesActivity activity;

    WeatherSourcesModule(WeatherSourcesActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WeatherSourcesActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    WeatherSourceDialogFragment.Callback provideDataSourceDialogCallback(WeatherSourcesPresenter
                                                                                 presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    WeatherSourcesPresenter providePresenter(WeatherSourcesActivity activity, WeatherSourcesMixer
            mixer, Features features) {
        return new WeatherSourcesPresenter(activity, mixer, features);
    }

    @Provides
    @Singleton
    WeatherSourcesMixer provideDataSourcesMixer(Context context, OkHttpClient okHttpClient,
                                                SprinklerRepositoryImpl sprinklerRepository,
                                                GoogleApiDelegate googleApiDelegate) {
        return new WeatherSourcesMixer(okHttpClient, sprinklerRepository, googleApiDelegate);
    }
}
