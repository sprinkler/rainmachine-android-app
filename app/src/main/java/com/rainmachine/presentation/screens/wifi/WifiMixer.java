package com.rainmachine.presentation.screens.wifi;

import android.net.wifi.WifiManager;
import android.os.Build;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.model.WifiScan;
import com.rainmachine.domain.model.WifiSettings;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.infrastructure.scanner.UDPDeviceScanner;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.util.Toasts;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class WifiMixer {

    private static final int DELAY_WAIT_SPRINKLER_RESPONSE_SECONDS = 150;

    private Device device;
    private Bus bus;
    private SprinklerState sprinklerState;
    private UDPDeviceScanner udpDeviceScanner;
    private AppManager appManager;
    private SprinklerRepositoryImpl sprinklerRepository;

    private WifiItemViewModelComparator wifiItemViewModelComparator;
    private boolean requestedToStopWaiting;

    WifiMixer(Bus bus, Device device, SprinklerState sprinklerState,
              UDPDeviceScanner udpDeviceScanner, AppManager appManager,
              SprinklerRepositoryImpl sprinklerRepository) {
        this.device = device;
        this.bus = bus;
        this.sprinklerState = sprinklerState;
        this.udpDeviceScanner = udpDeviceScanner;
        this.appManager = appManager;
        this.sprinklerRepository = sprinklerRepository;
        this.wifiItemViewModelComparator = new WifiItemViewModelComparator();
        this.bus.register(this);
    }

    Observable<WifiViewModel> refresh() {
        return sprinklerRepository.wifiScan().toObservable()
                .map(wifiScans -> {
                    WifiViewModel viewModel = new WifiViewModel();
                    List<WifiItemViewModel> items = new ArrayList<>(wifiScans.size());
                    String homeWifiSSID = WifiUtils.getDesanitizedHomeWifiSSID();
                    for (WifiScan wifiScan : wifiScans) {
                        WifiItemViewModel wifiItemViewModel = new WifiItemViewModel();
                        wifiItemViewModel.sSID = wifiScan.sSID;
                        wifiItemViewModel.isEncrypted = wifiScan.isEncrypted;
                        wifiItemViewModel.level = WifiManager.calculateSignalLevel(wifiScan.rSSI,
                                WifiItemViewModel.NUM_SIGNAL_LEVELS);
                        wifiItemViewModel.rSSI = wifiScan.rSSI;
                        wifiItemViewModel.isWEP = wifiScan.isWEP;
                        wifiItemViewModel.isWPA = wifiScan.isWPA;
                        wifiItemViewModel.isWPA2 = wifiScan.isWPA2;
                        if (!Strings.isBlank(homeWifiSSID)
                                && (homeWifiSSID.equals(wifiScan.sSID))) {
                            wifiItemViewModel.isHomeWifi = true;
                        }
                        wifiItemViewModel.isHidden = wifiScan.isHidden;
                        items.add(wifiItemViewModel);
                    }

                    Collections.sort(items, wifiItemViewModelComparator);

                    // Remove duplicate entries and keep only the one with the greatest strength
                    LinkedHashMap<String, WifiItemViewModel> map = new LinkedHashMap<>();
                    for (WifiItemViewModel wifiItemViewModel : items) {
                        if (!wifiItemViewModel.isHidden) {
                            if (!map.containsKey(wifiItemViewModel.sSID)) {
                                map.put(wifiItemViewModel.sSID, wifiItemViewModel);
                            }
                        }
                    }
                    Collection<WifiItemViewModel> collection = map.values();
                    if (collection instanceof List) {
                        items = (List<WifiItemViewModel>) collection;
                    } else {
                        items = new ArrayList<>(collection);
                    }
                    Collections.sort(items, wifiItemViewModelComparator);

                    viewModel.items = items;
                    return viewModel;
                });
    }

    void setSprinklerWifi(final WifiSettings wifiSettings) {
        bus.post(new SetSprinklerWifiEvent(BaseEvent.EVENT_TYPE_PROGRESS));
        Observable
                .fromCallable(() -> {
                    doSetSprinklerWifi(wifiSettings);
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void doSetSprinklerWifi(final WifiSettings wifiSettings) {
        setRefreshersBlocked(true);
        String previousSSID = WifiUtils.getCurrentSSID();
        int wifiSecurity = WifiUtils.WIFI_SECURITY_OPEN;
        if (wifiSettings.isWEP) {
            wifiSecurity = WifiUtils.WIFI_SECURITY_WEP;
        } else if (wifiSettings.isWPA || wifiSettings.isWPA2) {
            wifiSecurity = WifiUtils.WIFI_SECURITY_WPA;
        }
        Timber.d("Test if smartphone can connect to the wifi %s", wifiSettings.sSID);
        boolean isSuccess;

        // Disable Wifi password check on Android 5.0+ because it is not possible for now
        isSuccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || WifiUtils.testWifi
                (wifiSettings.sSID, wifiSettings.password, wifiSecurity, previousSSID);
        if (!isSuccess) {
            bus.post(new SetSprinklerWifiEvent(BaseEvent.EVENT_TYPE_ERROR, wifiSettings.sSID,
                    true));
        } else {
            setWifiSettings(wifiSettings);
        }
        setRefreshersBlocked(false);
    }

    private void setWifiSettings(final WifiSettings wifiSettings) {
        Timber.d("Set wifi settings on RainMachine");
        try {
            sprinklerRepository.saveWifiSettings(wifiSettings).toObservable().blockingFirst();

            Timber.d("Saving settings suceeded. Now activate wifi %s", wifiSettings.sSID);
            WifiUtils.moveToAlreadyEstablishedWifiConfiguration(wifiSettings.sSID);

            Toasts.show(R.string.wifi_rebooting_rain_machine, wifiSettings.sSID);

            discoverUdpDevice(wifiSettings.sSID);
        } catch (Throwable throwable) {
            Timber.d("Saving settings failed");
            bus.post(new SetSprinklerWifiEvent(BaseEvent.EVENT_TYPE_ERROR));
        }
    }

    private void discoverUdpDevice(String ssid) {
        Timber.d("Discover UDP device");

//        Sleeper.sleep(30 * DateTimeConstants.MILLIS_PER_SECOND);

        udpDeviceScanner.start(device);

        DateTime start = new DateTime();
        DateTime end = start.plusSeconds(DELAY_WAIT_SPRINKLER_RESPONSE_SECONDS);
        int pingInterval = 5 * DateTimeConstants.MILLIS_PER_SECOND;
        requestedToStopWaiting = false;
        while (!requestedToStopWaiting && end.isAfterNow()) {
            Sleeper.sleep(pingInterval);
        }
        Timber.d("Waited %d seconds", Seconds.secondsBetween(start, DateTime.now()).getSeconds());
        if (!requestedToStopWaiting) {
            // If we reach here and the device was not discovered through UDP,
            // it means error.
            udpDeviceScanner.stop();
            bus.post(new SetSprinklerWifiEvent(BaseEvent.EVENT_TYPE_ERROR, true, ssid));
        }
    }

    @Subscribe
    public void onDeviceDiscoveredEvent(UDPDeviceScanner.DeviceDiscoveredEvent event) {
        // If we already received the event, ignore duplicates
        if (requestedToStopWaiting) {
            return;
        }
        requestedToStopWaiting = true;
        udpDeviceScanner.stop();

        Timber.d("Device discovered %s vs %s", device.deviceId, event.device.deviceId);
        Injector.buildSprinklerGraph(device);
        bus.post(new SetSprinklerWifiEvent(BaseEvent.EVENT_TYPE_SUCCESS));
    }

    static class SetSprinklerWifiEvent extends BaseEvent {
        boolean isAuthenticationError;
        String ssid;
        boolean noUdpResponse;

        SetSprinklerWifiEvent(int type) {
            super(type);
        }

        SetSprinklerWifiEvent(int type, boolean noUdpResponse, String ssid) {
            super(type);
            this.noUdpResponse = noUdpResponse;
            this.ssid = ssid;
        }

        SetSprinklerWifiEvent(int type, String ssid, boolean isAuthenticationError) {
            super(type);
            this.ssid = ssid;
            this.isAuthenticationError = isAuthenticationError;
        }
    }

    private void setRefreshersBlocked(boolean isBlocked) {
        sprinklerState.setRefreshersBlocked(isBlocked);
    }

    private static class WifiItemViewModelComparator implements Comparator<WifiItemViewModel> {
        @Override
        public int compare(WifiItemViewModel lhs, WifiItemViewModel rhs) {
            // make home wifi be the first in the list no matter what
            if (lhs.isHomeWifi && rhs.isHomeWifi) {
                int diff = rhs.rSSI - lhs.rSSI; // sort descending
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (lhs.isHomeWifi) {
                return -1;
            }
            if (rhs.isHomeWifi) {
                return 1;
            }
            int diff = rhs.rSSI - lhs.rSSI; // sort descending
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
