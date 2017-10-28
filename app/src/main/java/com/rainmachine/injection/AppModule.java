package com.rainmachine.injection;

import android.app.Application;
import android.content.Context;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.pref.PrefRepositoryImpl;
import com.rainmachine.data.util.DataModule;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.InfrastructureModule;
import com.rainmachine.infrastructure.bus.AndroidBus;
import com.rainmachine.infrastructure.gcm.RainGcmListenerService;
import com.rainmachine.infrastructure.gcm.RainInstanceIDListenerService;
import com.rainmachine.infrastructure.receivers.OnConnectivityChangeReceiver;
import com.rainmachine.infrastructure.receivers.WifiBroadcastReceiver;
import com.rainmachine.infrastructure.scanner.StaleDeviceScanner;
import com.rainmachine.infrastructure.tasks.DeleteZoneImageService;
import com.rainmachine.infrastructure.tasks.UpdatePushNotificationSettingsService;
import com.rainmachine.infrastructure.tasks.UploadZoneImageService;
import com.rainmachine.infrastructure.util.RainApplication;
import com.rainmachine.presentation.util.ForegroundDetector;
import com.rainmachine.presentation.util.PresentationModule;
import com.squareup.otto.Bus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                DomainModule.class,
                DataModule.class,
                InfrastructureModule.class,
                PresentationModule.class,
        },
        injects = {
                RainApplication.class,

                AppManager.class,
                PrefRepositoryImpl.class,
                PrefRepository.class,

                StaleDeviceScanner.class,

                WifiBroadcastReceiver.class,
                OnConnectivityChangeReceiver.class,

                RainInstanceIDListenerService.class,
                RainGcmListenerService.class,
                UpdatePushNotificationSettingsService.class,
                UploadZoneImageService.class,
                DeleteZoneImageService.class,
        }
)
public final class AppModule {

    private final RainApplication app;

    public AppModule(RainApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    Context provideContext(Application app) {
        return app.getApplicationContext();
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new AndroidBus();
    }

    @Provides
    @Singleton
    AppManager provideAppManager(Context context, DatabaseRepositoryImpl databaseRepository,
                                 ForegroundDetector foregroundDetector,
                                 PrefRepository prefRepository,
                                 UpdatePushNotificationSettings updatePushNotificationSettings,
                                 InfrastructureService infrastructureService) {
        return new AppManager(context, databaseRepository, foregroundDetector, prefRepository,
                updatePushNotificationSettings, infrastructureService);
    }

    @Provides
    @Singleton
    ForegroundDetector provideForeground(Application app) {
        return new ForegroundDetector(app);
    }

    @Provides
    @Singleton
    @Named("device_cache_timeout")
    int provideDeviceCacheTimeout() {
        return DomainUtils.DEVICE_CACHE_TIMEOUT;
    }
}
