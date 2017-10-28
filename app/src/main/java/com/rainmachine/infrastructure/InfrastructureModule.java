package com.rainmachine.infrastructure;


import android.content.Context;

import com.google.gson.Gson;
import com.rainmachine.BuildConfig;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.infrastructure.Analytics;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.infrastructure.boundary.InfrastructureServiceImpl;
import com.rainmachine.infrastructure.boundary.analytics.AnswersAnalyticsImpl;
import com.rainmachine.infrastructure.boundary.analytics.DummyAnalyticsImpl;
import com.rainmachine.infrastructure.boundary.crash.CrashlyticsCrashReporter;
import com.rainmachine.infrastructure.boundary.crash.DummyCrashReporter;
import com.rainmachine.infrastructure.scanner.CloudDeviceScanner;
import com.rainmachine.infrastructure.scanner.DeviceScanner;
import com.rainmachine.infrastructure.scanner.StaleDeviceScanner;
import com.rainmachine.infrastructure.scanner.UDPDeviceScanner;
import com.rainmachine.infrastructure.scanner.WifiDeviceScanner;
import com.rainmachine.injection.HandlerModule;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                HandlerModule.class
        },
        complete = false,
        library = true
)
public class InfrastructureModule {

    @Provides
    @Singleton
    InfrastructureService provideInfrastructureService(Context context) {
        return new InfrastructureServiceImpl(context);
    }

    @Provides
    @Singleton
    DeviceScanner provideDeviceScanner(Bus bus, UDPDeviceScanner udpDeviceScanner,
                                       CloudDeviceScanner cloudDeviceScanner,
                                       WifiDeviceScanner wifiDeviceScanner,
                                       StaleDeviceScanner staleDeviceScanner,
                                       PrefRepository prefRepository) {
        return new DeviceScanner(bus, udpDeviceScanner, cloudDeviceScanner, wifiDeviceScanner,
                staleDeviceScanner, prefRepository);
    }

    @Provides
    @Singleton
    CrashReporter provideCrashReporter(Context context,
                                       InfrastructureService infrastructureService) {
        return BuildConfig.DEBUG ? new DummyCrashReporter()
                : new CrashlyticsCrashReporter(context, infrastructureService);
    }

    @Provides
    @Singleton
    Analytics provideAnalytics(Context context, Gson gson) {
        return BuildConfig.DEBUG ? new DummyAnalyticsImpl()
                : new AnswersAnalyticsImpl(context, gson);
    }

    /*@Provides
    @Singleton
    PlayServices providePlayServices(Context context) {
        return new PlayServices(context);
    }*/
}
