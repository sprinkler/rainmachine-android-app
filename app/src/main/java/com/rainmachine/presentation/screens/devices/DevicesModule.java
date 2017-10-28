package com.rainmachine.presentation.screens.devices;

import android.content.Context;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.scanner.DeviceScanner;
import com.rainmachine.injection.AppModule;
import com.rainmachine.presentation.screens.drawer.DrawerPresenter;
import com.rainmachine.presentation.screens.drawer.DrawerView;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                DevicesView.class,
                DrawerView.class,
                DevicesActivity.class
        }
)
class DevicesModule {

    private DevicesActivity activity;

    DevicesModule(DevicesActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    DevicesActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    DevicesContract.Presenter providePresenter(DevicesActivity activity, Bus bus, AppManager
            appManager, DevicesMixer mixer, PrefRepository prefRepository) {
        return new DevicesPresenter(activity, bus, appManager, mixer, prefRepository);
    }

    @Provides
    @Singleton
    DrawerPresenter providePresenter(DevicesActivity activity) {
        return new DrawerPresenter(activity, 0);
    }

    @Provides
    @Singleton
    DevicesMixer provideDevicesMixer(Context context, Bus bus, DatabaseRepositoryImpl
            databaseRepository, DeviceScanner deviceScanner, DeviceRepository deviceRepository) {
        return new DevicesMixer(context, bus, databaseRepository, deviceScanner, deviceRepository);
    }
}
