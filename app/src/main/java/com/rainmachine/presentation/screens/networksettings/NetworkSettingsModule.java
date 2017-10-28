package com.rainmachine.presentation.screens.networksettings;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.injection.AppModule;
import com.rainmachine.presentation.screens.drawer.DrawerPresenter;
import com.rainmachine.presentation.screens.drawer.DrawerView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                NetworkSettingsView.class,
                DrawerView.class,
                NetworkSettingsActivity.class
        }
)
class NetworkSettingsModule {

    private NetworkSettingsActivity activity;

    NetworkSettingsModule(NetworkSettingsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    NetworkSettingsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    NetworkSettingsPresenter providePresenter(NetworkSettingsActivity activity,
                                              NetworkSettingsMixer mixer) {
        return new NetworkSettingsPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    DrawerPresenter providePresenter(NetworkSettingsActivity activity) {
        return new DrawerPresenter(activity, 2);
    }

    @Provides
    @Singleton
    NetworkSettingsMixer provideNetworkSettingsMixer(DeviceRepository deviceRepository,
                                                     PrefRepository prefRepository) {
        return new NetworkSettingsMixer(deviceRepository, prefRepository);
    }
}
