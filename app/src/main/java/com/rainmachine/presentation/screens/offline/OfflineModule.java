package com.rainmachine.presentation.screens.offline;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.injection.AppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                OfflineActivity.class
        }
)
class OfflineModule {

    @Provides
    @Singleton
    OfflineContract.Presenter providePresenter(DeviceRepository deviceRepository) {
        return new OfflinePresenter(deviceRepository);
    }
}
