package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.notifiers.ZonePropertiesChangeNotifier;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.usecases.zoneimage.DeleteZoneImage;
import com.rainmachine.domain.usecases.zoneimage.UploadZoneImage;
import com.rainmachine.domain.util.Features;
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
                ZoneDetailsActivity.class,
                ZoneDetailsMainView.class,
                ZoneDetailsAdvancedView.class,
                ZoneDetailsWeatherView.class,
                MinutesDialogFragment.class,
                ActionMessageDialogFragment.class,
                MasterValveDurationDialogFragment.class,
                FieldCapacityDialogFragment.class
        }
)
class ZoneDetailsModule {

    private ZoneDetailsActivity activity;

    ZoneDetailsModule(ZoneDetailsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    ZoneDetailsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    MinutesDialogFragment.Callback provideMinutesDialogCallback(ZoneDetailsMainPresenter
                                                                        presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(ZoneDetailsPresenter
                                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    MasterValveDurationDialogFragment.Callback provideMasterValveDurationDialogCallback
            (ZoneDetailsMainPresenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    ZoneDetailsPresenter providePresenter(ZoneDetailsMixer mixer) {
        return new ZoneDetailsPresenter(mixer);
    }

    @Provides
    @Singleton
    ZoneDetailsMainPresenter provideMainPresenter(ZoneDetailsActivity activity, Features
            features, DeleteZoneImage deleteZoneImage, SprinklerRepository sprinklerRepository) {
        return new ZoneDetailsMainPresenter(activity, features, deleteZoneImage,
                sprinklerRepository);
    }

    @Provides
    @Singleton
    ZoneDetailsWeatherPresenter provideWeatherPresenter(ZoneDetailsActivity activity) {
        return new ZoneDetailsWeatherPresenter();
    }

    @Provides
    @Singleton
    ZoneDetailsAdvancedPresenter provideAdvancedPresenter(ZoneDetailsActivity activity, Features
            features, ZoneDetailsMixer mixer) {
        return new ZoneDetailsAdvancedPresenter(activity, features, mixer);
    }

    @Provides
    @Singleton
    ZoneDetailsMixer provideZoneDetailsMixer(Features features,
                                             SprinklerRepositoryImpl sprinklerRepository,
                                             Device device, UploadZoneImage uploadZoneImage,
                                             GetMacAddress getMacAddress,
                                             ZonePropertiesChangeNotifier
                                                     zonePropertiesChangeNotifier) {
        return new ZoneDetailsMixer(features, sprinklerRepository, device, uploadZoneImage,
                getMacAddress, zonePropertiesChangeNotifier);
    }
}
