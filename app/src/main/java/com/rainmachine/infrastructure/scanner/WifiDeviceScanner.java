package com.rainmachine.infrastructure.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.util.RainApplication;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class WifiDeviceScanner {

    private static final String SPRINKLER_AP_URL = "https://192.168.13.1:8080/";
    private static final int TIMEOUT_SCAN_WIFI_MILLIS = 3 * DateTimeConstants.MILLIS_PER_SECOND;

    private PersistDeviceHandler persistDeviceHandler;

    private WifiScanThread wifiScanThread;

    public WifiDeviceScanner(PersistDeviceHandler persistDeviceHandler) {
        this.persistDeviceHandler = persistDeviceHandler;
    }

    public void start() {
        Timber.d("Start Wifi scanner...");
        persistDeviceHandler.start();
        if (wifiScanThread == null || !wifiScanThread.isAlive()) {
            wifiScanThread = new WifiScanThread();
            wifiScanThread.start();
        }
    }

    public void stop() {
        if (wifiScanThread != null) {
            wifiScanThread.requestStop();
            wifiScanThread = null;
        }
        persistDeviceHandler.stop();
    }

    public class WifiScanThread extends Thread {

        private WifiReceiver wifiReceiver;
        private boolean requestedToStop;

        @Override
        public void run() {
            final WifiManager wifiManager = (WifiManager) RainApplication.get()
                    .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiReceiver = new WifiReceiver();
            RainApplication.get().registerReceiver(wifiReceiver, new IntentFilter(WifiManager
                    .SCAN_RESULTS_AVAILABLE_ACTION));
            try {
                while (!Thread.currentThread().isInterrupted() && !requestedToStop) {
                    Timber.d("Request wifi scan");
                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.startScan();
                    }
                    Sleeper.sleepThrow(TIMEOUT_SCAN_WIFI_MILLIS);
                }
            } catch (InterruptedException ie) {
                Timber.d("The wifi scan thread received interrupt request");
                unregisterWifiReceiver();
            }
            Timber.d("Finish the wifi scan thread...");
        }

        public void requestStop() {
            unregisterWifiReceiver();
            requestedToStop = true;
            interrupt();
        }

        private void unregisterWifiReceiver() {
            if (wifiReceiver != null) {
                try {
                    RainApplication.get().unregisterReceiver(wifiReceiver);
                } catch (IllegalArgumentException iae) {
                    Timber.w(iae, iae.getMessage());
                }
                wifiReceiver = null;
            }
        }

        private class WifiReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Received scan result");
                WifiManager wifiManager = (WifiManager) RainApplication.get()
                        .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> wifis = wifiManager.getScanResults();
                if (wifis == null) {
                    return;
                }
                for (ScanResult result : wifis) {
                    Timber.d("Wifi: %s %s", result.SSID, result.BSSID);
                    if (InfrastructureUtils.isRainmachineSSID(result.SSID)) {
                        Device device = new Device();
                        device.deviceId = result.BSSID != null ? result.BSSID.toLowerCase(Locale
                                .getDefault()) : ""; // the MAC
                        device.name = result.SSID;
                        device.setUrl(SPRINKLER_AP_URL);
                        device.type = Device.SPRINKLER_TYPE_AP;
                        device.timestamp = new DateTime().getMillis();
                        // We don't know if wizard has run or not. We need to connect to AP and
                        // see what UDP message we receive
                        device.wizardHasRun = true;
                        device.cloudEmail = null;

                        // We save the sprinkler only if we are not already connected to this AP.
                        // If we are connected to the sprinkler AP, we rely on the UDP messages
                        if (!WifiUtils.isCurrentlyActiveSSID(device.name)) {
                            persistDeviceHandler.addToQueue(device);
                        }
                    }
                }
            }
        }
    }
}
