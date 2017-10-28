package com.rainmachine.presentation.screens.wizarddevicename;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WizardDeviceNameActivity.class,
                WizardDeviceNameView.class,
        }
)
class WizardDeviceNameModule {

    private WizardDeviceNameActivity activity;

    WizardDeviceNameModule(WizardDeviceNameActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WizardDeviceNameActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    WizardDeviceNamePresenter providePresenter(WizardDeviceNameActivity activity,
                                               WizardDeviceNameMixer mixer,
                                               Device device) {
        return new WizardDeviceNamePresenter(activity, mixer, device);
    }

    @Provides
    @Singleton
    WizardDeviceNameMixer provideWizardDeviceNameMixer(Device device,
                                                       SprinklerRepositoryImpl sprinklerRepository,
                                                       DatabaseRepositoryImpl databaseRepository,
                                                       DeviceNameStore deviceNameStore,
                                                       SprinklerState sprinklerState) {
        return new WizardDeviceNameMixer(device, sprinklerRepository, databaseRepository,
                deviceNameStore, sprinklerState);
    }
}
