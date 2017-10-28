package com.rainmachine.infrastructure.scanner;

import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.receivers.OnConnectivityChangeReceiver;
import com.rainmachine.infrastructure.receivers.WifiBroadcastReceiver;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import timber.log.Timber;

public class DeviceScanner {

    private Bus bus;
    private UDPDeviceScanner udpDeviceScanner;
    private CloudDeviceScanner cloudDeviceScanner;
    private WifiDeviceScanner wifiDeviceScanner;
    private StaleDeviceScanner staleDeviceScanner;
    private PrefRepository prefRepository;

    public DeviceScanner(Bus bus, UDPDeviceScanner udpDeviceScanner, CloudDeviceScanner
            cloudDeviceScanner, WifiDeviceScanner wifiDeviceScanner, StaleDeviceScanner
                                 staleDeviceScanner, PrefRepository prefRepository) {
        this.bus = bus;
        this.udpDeviceScanner = udpDeviceScanner;
        this.cloudDeviceScanner = cloudDeviceScanner;
        this.wifiDeviceScanner = wifiDeviceScanner;
        this.staleDeviceScanner = staleDeviceScanner;
        this.prefRepository = prefRepository;
    }

    public void startScanning() {
        Timber.d("Start device scanning");
        startThreads();
        try {
            bus.register(this);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
    }

    public void stopScanning() {
        Timber.d("Stop device scanning");
        stopThreads();
        try {
            bus.unregister(this);
        } catch (IllegalArgumentException iae) {
            Timber.w(iae, iae.getMessage());
        }
    }

    private synchronized void startThreads() {
        if (prefRepository.localDiscovery() && WifiUtils.isWifiEnabled()) {
            wifiDeviceScanner.start();
            if (WifiUtils.isWifiConnected()) {
                udpDeviceScanner.start();
            }
        }
        cloudDeviceScanner.start();
        staleDeviceScanner.start(3);
    }

    @Subscribe
    public void onWifiNetworkEvent(WifiBroadcastReceiver.WifiNetworkEvent event) {
        Timber.d("Wifi network %s connected %s", event.ssid, event.connected);
        if (event.connected) {
            if (prefRepository.localDiscovery()) {
                udpDeviceScanner.start();
            }
        } else {
            udpDeviceScanner.stop();
        }
    }

    @Subscribe
    public void onWifiStateEvent(OnConnectivityChangeReceiver.WifiStateEvent event) {
        Timber.d("Wifi state %s", event.enabled);
        if (event.enabled) {
            if (prefRepository.localDiscovery()) {
                wifiDeviceScanner.start();
            }
        } else {
            wifiDeviceScanner.stop();
        }
    }

    private synchronized void stopThreads() {
        udpDeviceScanner.stop();
        wifiDeviceScanner.stop();
        cloudDeviceScanner.stop();
        staleDeviceScanner.stop();
    }
}
