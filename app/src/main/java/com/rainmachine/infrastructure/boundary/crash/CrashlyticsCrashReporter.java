package com.rainmachine.infrastructure.boundary.crash;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.infrastructure.util.CrashlyticsTree;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class CrashlyticsCrashReporter implements CrashReporter {

    private Context context;
    private InfrastructureService infrastructureService;

    public CrashlyticsCrashReporter(Context context, InfrastructureService infrastructureService) {
        this.context = context;
        this.infrastructureService = infrastructureService;
    }

    @Override
    public void init() {
        Fabric.with(context, new Crashlytics());
        Crashlytics.setUserIdentifier(infrastructureService.getSystemId());
        Crashlytics.setString("Installer", infrastructureService.getInstaller());
        Timber.plant(new CrashlyticsTree());
    }

    @Override
    public void logUserEmail(String email) {
        Crashlytics.setUserEmail(email);
    }

    @Override
    public void logDeviceVersion(String version) {
        Crashlytics.setString("Device Version", version);
    }

    @Override
    public void logDeviceName(String deviceName) {
        Crashlytics.setUserName(deviceName);
    }

    @Override
    public void logDeviceType(String deviceType) {
        Crashlytics.setString("Device Type", deviceType);
    }
}
