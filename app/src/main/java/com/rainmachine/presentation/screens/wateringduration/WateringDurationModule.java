package com.rainmachine.presentation.screens.wateringduration;

import com.rainmachine.domain.usecases.wateringduration.GetWateringDurationForZones;
import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.injection.AppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                WateringDurationActivity.class,
                WateringDurationDialogFragment.class
        }
)
class WateringDurationModule {

    private WateringDurationContract.View view;

    WateringDurationModule(WateringDurationContract.View view) {
        this.view = view;
    }

    @Provides
    @Singleton
    WateringDurationContract.Presenter providePresenter(GetWateringDurationForZones
                                                                getWateringDurationForZones,
                                                        SaveWateringDuration saveWateringDuration,
                                                        SchedulerProvider schedulerProvider) {
        return new WateringDurationPresenter(getWateringDurationForZones, saveWateringDuration,
                schedulerProvider);
    }

    @Provides
    @Singleton
    WateringDurationDialogFragment.Callback provideCallback() {
        return view;
    }
}
