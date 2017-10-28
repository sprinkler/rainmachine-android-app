package com.rainmachine.injection;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.infrastructure.scanner.CloudDeviceScanner;
import com.rainmachine.infrastructure.scanner.PersistDeviceHandler;
import com.rainmachine.infrastructure.scanner.StaleDeviceScanner;
import com.rainmachine.infrastructure.scanner.UDPDeviceScanner;
import com.rainmachine.infrastructure.scanner.WifiDeviceScanner;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class HandlerModule {

    @Provides
    @Singleton
    UDPDeviceScanner provideUDPDeviceScanner(Bus bus, PersistDeviceHandler persistDeviceHandler) {
        return new UDPDeviceScanner(bus, persistDeviceHandler);
    }

    @Provides
    @Singleton
    CloudDeviceScanner provideCloudDeviceScanner(DatabaseRepositoryImpl databaseRepository,
                                                 CloudRepository cloudRepository) {
        return new CloudDeviceScanner(databaseRepository, cloudRepository);
    }

    @Provides
    @Singleton
    WifiDeviceScanner provideWifiDeviceScanner(PersistDeviceHandler persistDeviceHandler) {
        return new WifiDeviceScanner(persistDeviceHandler);
    }

    @Provides
    @Singleton
    PersistDeviceHandler providePersistDeviceHandler(DatabaseRepositoryImpl databaseRepository) {
        return new PersistDeviceHandler(databaseRepository);
    }

    @Provides
    @Singleton
    StaleDeviceScanner provideStaleDeviceScanner(DeviceRepository deviceRepository) {
        return new StaleDeviceScanner(deviceRepository);
    }
}
