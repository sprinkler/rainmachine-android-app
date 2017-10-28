package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Context;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.util.Features;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WateringHistoryActivity.class,
                WateringHistoryView.class,
                RadioOptionsDialogFragment.class
        }
)
class WateringHistoryModule {

    private WateringHistoryActivity activity;

    WateringHistoryModule(WateringHistoryActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WateringHistoryActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    RadioOptionsDialogFragment.Callback provideRadioOptionsCallback(WateringHistoryPresenter
                                                                            presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    WateringHistoryPresenter providePresenter(WateringHistoryActivity activity,
                                              WateringHistoryMixer mixer) {
        return new WateringHistoryPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    WateringHistoryMixer provideWateringHistoryMixer(Context context, SprinklerRepositoryImpl
            sprinklerRepository, Device device, CalendarFormatter calendarFormatter,
                                                     DecimalFormatter decimalFormatter, Features
                                                             features) {
        return new WateringHistoryMixer(context, sprinklerRepository, device, calendarFormatter,
                decimalFormatter, features);
    }
}
