package com.rainmachine.presentation.screens.wifi;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.scanner.UDPDeviceScanner;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WifiView.class,
                WifiActivity.class,
                WifiSettingsDialogFragment.class,
                WifiNetworkDialogFragment.class,
                ActionMessageDialogFragment.class,
                InfoMessageDialogFragment.class
        }
)
class WifiModule {

    private WifiContract.Container container;

    WifiModule(WifiContract.Container container) {
        this.container = container;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(WifiContract.Presenter
                                                                                    wifiPresenter) {
        return wifiPresenter;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(WifiContract.Presenter
                                                                                wifiPresenter) {
        return wifiPresenter;
    }

    @Provides
    @Singleton
    WifiContract.Presenter providePresenter(Bus bus, WifiMixer mixer) {
        return new WifiPresenter(container, bus, mixer);
    }

    @Provides
    @Singleton
    WifiMixer provideWifiMixer(Bus bus, Device device, SprinklerState
            sprinklerState, UDPDeviceScanner udpDeviceScanner, AppManager appManager,
                               SprinklerRepositoryImpl sprinklerRepository) {
        return new WifiMixer(bus, device, sprinklerState, udpDeviceScanner,
                appManager, sprinklerRepository);
    }
}
