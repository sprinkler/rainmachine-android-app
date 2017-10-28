package com.rainmachine.infrastructure;

import android.os.Handler;
import android.os.Looper;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.util.BaseUrlSelectionInterceptor;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.usecases.zoneimage.SyncZoneImages;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.presentation.util.ForegroundDetector;
import com.rainmachine.presentation.util.RunOnProperThreads;

import org.joda.time.DateTimeConstants;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SprinklerManager {

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private Device device;
    private ForegroundDetector foregroundDetector;
    private SprinklerState sprinklerState;
    private SprinklerUtils sprinklerUtils;
    private SyncZoneImages syncZoneImages;
    private BaseUrlSelectionInterceptor baseUrlSelectionInterceptor;
    private GetRestrictionsLive getRestrictionsLive;

    private Disposable subscriptionForeground;
    private Disposable subscriptionSyncZoneImages;
    private Runnable runForgetAp;
    private Runnable runStopRefreshers;
    private boolean isRefreshersStarted;

    private CompositeDisposable disposables;

    public SprinklerManager(Device device, ForegroundDetector foregroundDetector,
                            SprinklerState sprinklerState,
                            SprinklerUtils sprinklerUtils,
                            SyncZoneImages syncZoneImages,
                            BaseUrlSelectionInterceptor baseUrlSelectionInterceptor,
                            GetRestrictionsLive getRestrictionsLive) {
        this.device = device;
        this.foregroundDetector = foregroundDetector;
        this.sprinklerState = sprinklerState;
        this.sprinklerUtils = sprinklerUtils;
        this.syncZoneImages = syncZoneImages;
        this.baseUrlSelectionInterceptor = baseUrlSelectionInterceptor;
        this.getRestrictionsLive = getRestrictionsLive;
        disposables = new CompositeDisposable();
    }

    public void init() {
        if (device.isAp()) {
            runForgetAp = () -> {
                Timber.d("Remove all Wi-Fi configurations for AP (%s) and switch to home " +
                        "Wi-Fi (%s) if possible", device.name, WifiUtils.getHomeWifiSSID());
                WifiUtils.removeWifiConfigurations(device.name);
                WifiUtils.moveToAlreadyEstablishedWifiConfiguration(WifiUtils.getHomeWifiSSID
                        ());
            };
        }

        isRefreshersStarted = false;
        runStopRefreshers = () -> stopRefreshers();

        subscriptionForeground = foregroundDetector
                .refresher()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ForegroundSubscriber());
    }

    public void syncZoneImages() {
        subscriptionSyncZoneImages = syncZoneImages.execute(
                new SyncZoneImages.RequestModel(device.deviceId, device.isManual()))
                .onErrorResumeNext(throwable -> Completable.complete())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void cleanUp() {
        if (subscriptionForeground != null && !subscriptionForeground.isDisposed()) {
            subscriptionForeground.dispose();
            subscriptionForeground = null;
        }
        if (subscriptionSyncZoneImages != null && !subscriptionSyncZoneImages.isDisposed()) {
            subscriptionSyncZoneImages.dispose();
            subscriptionSyncZoneImages = null;
        }
    }

    private void handleNetworkTrafficForeground() {
        if (device.isLocal()) {
            if (InfrastructureUtils.shouldSwitchToCloud(device)) {
                InfrastructureUtils.switchToCloud(device, baseUrlSelectionInterceptor);
            } else if (InfrastructureUtils.shouldSwitchToWifi(device)) {
                InfrastructureUtils.switchToWifi(device, baseUrlSelectionInterceptor);
            } else if (InfrastructureUtils.shouldRouteNetworkTrafficToWiFi(device)) {
                boolean success = NetworkUtils.routeNetworkTrafficToCurrentWiFi();
                if (!success) {
                    InfrastructureUtils.finishAllSprinklerActivities();
                }
            }
        } else {
            NetworkUtils.clearNetworkTrafficRouting();
        }
    }

    private void handleApResume() {
        if (device.isAp()) {
            MAIN_THREAD.removeCallbacks(runForgetAp);
            if (!WifiUtils.isWifiEnabled()) {
                Timber.i("Wi-Fi is not enabled and there is no way to access the AP so we finish " +
                        "all sprinkler screens");
                InfrastructureUtils.finishAllSprinklerActivities();
            } else if (!sprinklerState.isInitialSetup() && !isConnectedToRightSSID()) {
                Timber.i("The current connected Wi-Fi (%s) is different from the AP (%s)",
                        WifiUtils.getCurrentSSID(), device.name);
                InfrastructureUtils.finishAllSprinklerActivities();
            }
        }
    }

    private boolean isConnectedToRightSSID() {
        // When changing the name of the device, the AP name does not change so we may be
        // connected to the right AP although the device name is different
        // All APs start with RainMachine- so we do this simple check
        String ssid = WifiUtils.getCurrentSSID();
        return ssid != null && ssid.startsWith("\"RainMachine-");
    }

    private void handleApPause() {
        if (device.isAp()) {
            MAIN_THREAD.postDelayed(runForgetAp, 2 * DateTimeConstants.MILLIS_PER_SECOND);
        }
    }

    private void handleRefreshersResume() {
        MAIN_THREAD.removeCallbacks(runStopRefreshers);
        if (!isRefreshersStarted && device.wizardHasRun && sprinklerUtils.isAuthenticated() &&
                !sprinklerState.isInitialSetup()) {
            isRefreshersStarted = true;
            startRefreshers();
        }
    }

    private void handleRefreshersPause() {
        // If onResume is not called in a few seconds, it means we left the app and we destroy all
        // refreshers
        MAIN_THREAD.postDelayed(runStopRefreshers, DateTimeConstants.MILLIS_PER_SECOND);
    }

    private void startRefreshers() {
        Timber.d("Starting all refreshers");
        disposables.add(getRestrictionsLive.execute(new GetRestrictionsLive.RequestModel(false))
                .compose(RunOnProperThreads.instance())
                .subscribe());
    }

    private void stopRefreshers() {
        Timber.d("Stopping all refreshers");
        disposables.clear();
        isRefreshersStarted = false;
    }

    private final class ForegroundSubscriber extends DisposableObserver<ForegroundDetector
            .ForegroundState> {
        @Override
        public void onComplete() {
            // Do nothing
        }

        @Override
        public void onError(Throwable e) {
            // Do nothing
        }

        @Override
        public void onNext(ForegroundDetector.ForegroundState foregroundState) {
            if (foregroundState == ForegroundDetector.ForegroundState.APP_BECAME_FOREGROUND) {
                handleNetworkTrafficForeground();
            } else if (foregroundState == ForegroundDetector.ForegroundState.ACTIVITY_RESUMED) {
                handleApResume();
                handleRefreshersResume();
            } else if (foregroundState == ForegroundDetector.ForegroundState.ACTIVITY_PAUSED) {
                handleApPause();
                handleRefreshersPause();
            }
        }
    }
}
