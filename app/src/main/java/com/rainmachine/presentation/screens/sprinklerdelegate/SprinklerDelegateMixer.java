package com.rainmachine.presentation.screens.sprinklerdelegate;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.model.Diagnostics;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.usecases.auth.CheckAuthenticationValid;
import com.rainmachine.domain.usecases.auth.LogInDefault;
import com.rainmachine.domain.usecases.remoteaccess.ToggleRemoteAccess;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.NetworkUtils;
import com.rainmachine.infrastructure.SprinklerUtils;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.infrastructure.util.BaseApplication;
import com.rainmachine.presentation.util.Toasts;
import com.squareup.otto.Bus;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SprinklerDelegateMixer {

    public static boolean DEV_SPRINKLER_WIZARD = false; // used only for testing through dev drawer

    private SprinklerUtils sprinklerUtils;
    private Device device;
    private ToggleRemoteAccess toggleRemoteAccess;
    private CheckAuthenticationValid checkAuthenticationValid;
    private Features features;
    private DatabaseRepositoryImpl databaseRepository;
    private Bus bus;
    private CrashReporter crashReporter;
    private LogInDefault logInDefault;
    private SprinklerRepositoryImpl sprinklerRepository;

    SprinklerDelegateMixer(Bus bus, Device device, SprinklerUtils sprinklerUtils,
                           Features features, DatabaseRepositoryImpl databaseRepository,
                           SprinklerRepositoryImpl sprinklerRepository,
                           ToggleRemoteAccess toggleRemoteAccess,
                           CrashReporter crashReporter, LogInDefault logInDefault,
                           CheckAuthenticationValid checkAuthenticationValid) {
        this.sprinklerUtils = sprinklerUtils;
        this.device = device;
        this.toggleRemoteAccess = toggleRemoteAccess;
        this.checkAuthenticationValid = checkAuthenticationValid;
        this.sprinklerRepository = sprinklerRepository;
        this.features = features;
        this.databaseRepository = databaseRepository;
        this.bus = bus;
        this.crashReporter = crashReporter;
        this.logInDefault = logInDefault;
    }

    void makeDelegateDecision(boolean skipPoorWifiDetection) {
        Observable
                .fromCallable(() -> {
                    doMakeDelegateDecision(skipPoorWifiDetection);
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void doMakeDelegateDecision(boolean skipPoorWifiDetection) {
        logDeviceType();

        if (device.isAp()) {
            doMakeDelegateDecisionAp4(true, skipPoorWifiDetection);
        } else {
            if (device.isUdp()) {
                if (InfrastructureUtils.shouldRouteNetworkTrafficToWiFi(device)) {
                    // If the device is discovered through UDP, we need to use the current  WiFi for
                    // API calls
                    boolean success = NetworkUtils.routeNetworkTrafficToCurrentWiFi();
                    if (!success) {
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
                        return;
                    }
                }
            } else {
                // Cloud or manual devices need Internet so we clear any routing and let it up to
                // the Android device to use the right connection for Internet access
                NetworkUtils.clearNetworkTrafficRouting();
            }

            try {
                sprinklerRepository.versions().toObservable().blockingFirst();
            } catch (Throwable throwable) {
                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
                return;
            }

            if (features.isAtLeastSpk2()) {
                if (device.isUdp() && device.deviceId.equalsIgnoreCase(WifiUtils
                        .getCurrentWifiMac())) {
                    Timber.i("Device discovered through UDP but is active AP");
                    doMakeDelegateDecisionAp4(false, skipPoorWifiDetection);
                } else {
                    doMakeDelegateDecisionRunning4();
                }
            } else {
                doMakeDelegateDecision3();
            }
        }
    }

    private void logDeviceType() {
        String deviceType = "Unknown";
        if (device.isAp()) {
            deviceType = "AP";
        } else if (device.isUdp()) {
            deviceType = "UDP";
        } else if (device.isCloud()) {
            deviceType = "Cloud";
        } else if (device.isManual()) {
            deviceType = "Manual";
        }
        crashReporter.logDeviceType(deviceType);
    }

    private void doMakeDelegateDecisionAp4(boolean doWifiLogic, boolean skipPoorWifiDetection) {
        if (!skipPoorWifiDetection && WifiUtils.isPoorNetworkAvoidanceEnabled(BaseApplication
                .getContext())) {
            Timber.i("Poor network avoidance enabled");
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent
                    .GO_TO_SETTINGS_AND_DISABLE_POOR_NETWORK_AVOIDANCE));
            return;
        }
        if (doWifiLogic) {
            String apSSID = device.name;
            if (!WifiUtils.isCurrentlyActiveSSID(apSSID)) {
                if (!WifiUtils.moveToAlreadyEstablishedWifiConfiguration(apSSID)) {
                    int wifiSecurity = WifiUtils.WIFI_SECURITY_OPEN;
                    boolean connectedToAp = WifiUtils.moveToNewWifiConfiguration(apSSID, null,
                            wifiSecurity);
                    if (!connectedToAp) {
                        Toasts.show(R.string.all_failed_connect_ap, apSSID);
                        WifiUtils.moveToAlreadyEstablishedWifiConfiguration(WifiUtils
                                .getHomeWifiSSID());
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
                        return;
                    }
                }
            }
        }

        if (InfrastructureUtils.shouldRouteNetworkTrafficToWiFi(device)) {
            // If the device is discovered through UDP, we need to use the WiFi for API calls
            boolean success = NetworkUtils.routeNetworkTrafficToCurrentWiFi();
            if (!success) {
                if (doWifiLogic) {
                    WifiUtils.moveToAlreadyEstablishedWifiConfiguration(WifiUtils.getHomeWifiSSID
                            ());
                }

                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
                return;
            }
        }

        try {
            sprinklerRepository.versions().toObservable().blockingFirst();
        } catch (Throwable throwable) {
            if (doWifiLogic) {
                WifiUtils.moveToAlreadyEstablishedWifiConfiguration(WifiUtils.getHomeWifiSSID());
            }
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
            return;
        }

        boolean wizardHasRun;
        boolean standaloneMode;
        try {
            Diagnostics diagnostics = sprinklerRepository.diagnostics().toObservable()
                    .blockingFirst();
            wizardHasRun = diagnostics.wizardHasRun;
            standaloneMode = diagnostics.standaloneMode;
        } catch (Throwable throwable) {
            if (doWifiLogic) {
                WifiUtils.moveToAlreadyEstablishedWifiConfiguration(WifiUtils.getHomeWifiSSID());

            }
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_BACK));
            return;
        }

        if (wizardHasRun) {
            if (isReallyAuthenticated()) {
                if (doCheckDefaultPassword())
                // the user pressed the hardware black button
                {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent
                            .GO_TO_WIFI_AND_PASSWORD));
                } else {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_MAIN));
                }
            } else {
                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_LOGIN));
            }
        } else {
            if (isReallyAuthenticated()) {
                // Disable cloud client in initial setup due to some cloud bug in RainMachine device
                toggleRemoteAccess
                        .execute(new ToggleRemoteAccess.RequestModel(false))
                        .onErrorComplete()
                        .blockingAwait();
                if (standaloneMode) {
                    if (doCheckDefaultPassword()) {
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_WIZARD));
                    } else {
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent
                                .GO_TO_WIZARD_OLD_PASS));
                    }
                } else {
                    if (doCheckDefaultPassword()) {
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_WIFI));
                    } else {
                        bus.post(new DelegateDecisionEvent(DelegateDecisionEvent
                                .GO_TO_WIFI_OLD_PASS));
                    }
                }
            } else {
                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_PHYSICAL_TOUCH));
            }
        }
    }

    private void doMakeDelegateDecisionRunning4() {
        if (DEV_SPRINKLER_WIZARD) {
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_WIZARD_OLD_PASS));
            return;
        }

        // We know from UDP if the wizard has run or not
        boolean wizardHasRun = device.wizardHasRun;
        if (wizardHasRun) {
            if (isReallyAuthenticated()) {
                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_MAIN));
            } else {
                boolean successfullyAuthenticated = false;
                if (device.isCloud() && !Strings.isBlank(device.cloudEmail)) {
                    CloudInfo cloudInfo = databaseRepository.getCloudInfo(device.cloudEmail);
                    if (cloudInfo != null) {
                        LoginStatus loginStatus = sprinklerRepository
                                .login(cloudInfo.password, true)
                                .toObservable()
                                .blockingFirst();
                        if (loginStatus == LoginStatus.SUCCESS) {
                            successfullyAuthenticated = true;
                        }
                    }
                }
                if (successfullyAuthenticated) {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_MAIN));
                } else {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_LOGIN));
                }
            }
        } else {
            if (isReallyAuthenticated()) {
                if (doCheckDefaultPassword()) {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_WIZARD));
                } else {
                    bus.post(new DelegateDecisionEvent(DelegateDecisionEvent
                            .GO_TO_WIZARD_OLD_PASS));
                }
            } else {
                bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_LOGIN));
            }
        }
    }

    private boolean isReallyAuthenticated() {
        boolean isReallyAuthenticated = false;
        if (sprinklerUtils.isAuthenticated()) {
            isReallyAuthenticated = checkAuthenticationValid
                    .execute(new CheckAuthenticationValid.RequestModel()).blockingFirst().success;
        }
        if (!isReallyAuthenticated) {
            sprinklerUtils.logout();
            isReallyAuthenticated = logInDefault.execute(new LogInDefault.RequestModel())
                    .blockingFirst().success;
        }
        return isReallyAuthenticated;
    }

    private void doMakeDelegateDecision3() {
        if (sprinklerUtils.isAuthenticated()) {
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_MAIN));
        } else {
            bus.post(new DelegateDecisionEvent(DelegateDecisionEvent.GO_TO_LOGIN));
        }
    }

    private boolean doCheckDefaultPassword() {
        return sprinklerRepository
                .checkDefaultPassword().toObservable()
                .onErrorResumeNext(Observable.empty())
                .blockingFirst(false);
    }

    static class DelegateDecisionEvent extends BaseEvent {
        static final int GO_TO_MAIN = 0;
        static final int GO_TO_WIFI = 1;
        static final int GO_TO_LOGIN = 2;
        static final int GO_TO_WIZARD = 3;
        static final int GO_BACK = 4;
        static final int GO_TO_WIZARD_OLD_PASS = 5;
        static final int GO_TO_WIFI_OLD_PASS = 6;
        static final int GO_TO_PHYSICAL_TOUCH = 7;
        static final int GO_TO_WIFI_AND_PASSWORD = 8;
        static final int GO_TO_SETTINGS_AND_DISABLE_POOR_NETWORK_AVOIDANCE = 9;
        int goToScreen;

        DelegateDecisionEvent(int goToScreen) {
            this.goToScreen = goToScreen;
        }
    }
}
