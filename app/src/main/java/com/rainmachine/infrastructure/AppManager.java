package com.rainmachine.infrastructure;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.receivers.WifiBroadcastReceiver;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.util.ForegroundDetector;

import org.joda.time.DateTimeConstants;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

public class AppManager {

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private Context context;
    private DatabaseRepositoryImpl databaseRepository;
    private ForegroundDetector foregroundDetector;
    private PrefRepository prefRepository;
    private UpdatePushNotificationSettings updatePushNotificationSettings;
    private InfrastructureService infrastructureService;

    private WifiBroadcastReceiver wifiReceiver;

    public AppManager(Context context, DatabaseRepositoryImpl databaseRepository, ForegroundDetector
            foregroundDetector, PrefRepository prefRepository, UpdatePushNotificationSettings
                              updatePushNotificationSettings, InfrastructureService
                              infrastructureService) {
        this.context = context;
        this.databaseRepository = databaseRepository;
        this.foregroundDetector = foregroundDetector;
        this.prefRepository = prefRepository;
        this.updatePushNotificationSettings = updatePushNotificationSettings;
        this.infrastructureService = infrastructureService;
    }

    public void initializeEveryColdStart() {
        Timber.i("Initialize every cold start");
        int currentVersionCode = infrastructureService.getCurrentVersionCode();
        if (prefRepository.firstTime()) {
            Timber.i("First time running the app");
            prefRepository.saveFirstTime(false);
            prefRepository.saveVersionCode(currentVersionCode);
            updatePushNotificationSettings();
        } else {
            if (prefRepository.versionCode() < currentVersionCode) {
                Timber.i("Old version code %d is replaced with new version code %d",
                        prefRepository.versionCode(), currentVersionCode);
                if (prefRepository.versionCode() < 40102) {
                    updatePushNotificationSettings();
                }
                prefRepository.saveVersionCode(currentVersionCode);
            } else {
                Timber.i("Just a basic cold start");
            }
        }

        foregroundDetector
                .refresher()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ForegroundSubscriber());

        // If we were on a sprinkler screen, try to recreate the sprinkler graph
        if (!Strings.isBlank(prefRepository.currentDeviceId())) {
            Device device = null;
            if (Device.SPRINKLER_TYPE_UDP == prefRepository.currentDeviceType()) {
                List<Device> devices = databaseRepository.getUdpAndCloudDevice(prefRepository
                        .currentDeviceId());
                if (devices.size() == 1) {
                    device = devices.get(0);
                } else if (devices.size() >= 2) {
                    for (Device device1 : devices) {
                        if (device1.isUdp()) {
                            device = device1;
                            for (Device device2 : devices) {
                                if (device2.isCloud()) {
                                    device.alternateCloudUrl = device2.getUrl();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            } else {
                device = databaseRepository.getDevice(prefRepository.currentDeviceId(),
                        prefRepository.currentDeviceType());
            }
            if (device != null) {
                Injector.buildSprinklerGraph(device);
            } else {
                Injector.removeSprinklerGraph();
            }
        }
    }

    private void startWifiReceiver() {
        if (wifiReceiver == null) {
            Timber.d("Started wifi receiver");
            wifiReceiver = new WifiBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(wifiReceiver, intentFilter);
        }
    }

    private void stopWifiReceiver() {
        if (wifiReceiver != null) {
            Timber.d("Stopped wifi receiver");
            context.unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }

    private Runnable runStopWifiReceiver = () -> stopWifiReceiver();

    private void updatePushNotificationSettings() {
        new Thread(() -> updatePushNotificationSettings
                .execute(new UpdatePushNotificationSettings.RequestModel())
                .blockingAwait()).start();
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
            Timber.d("foreground event: %s", foregroundState.toString());
            if (foregroundState == ForegroundDetector.ForegroundState.ACTIVITY_RESUMED) {
                MAIN_THREAD.removeCallbacks(runStopWifiReceiver);
                startWifiReceiver();
            } else if (foregroundState == ForegroundDetector.ForegroundState.ACTIVITY_PAUSED) {
                MAIN_THREAD.postDelayed(runStopWifiReceiver, 3 * DateTimeConstants
                        .MILLIS_PER_SECOND);
            } else if (foregroundState == ForegroundDetector.ForegroundState
                    .APP_BECAME_BACKGROUND) {
                Timber.i("APP WENT TO BACKGROUND");
            } else if (foregroundState == ForegroundDetector.ForegroundState
                    .APP_BECAME_FOREGROUND) {
                Timber.i("APP CAME TO FOREGROUND");
            }
        }
    }
}
