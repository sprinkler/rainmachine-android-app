package com.rainmachine.infrastructure.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.injection.Injector;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import timber.log.Timber;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    @Inject
    Bus bus;

    @Override
    public void onReceive(Context context, Intent initialIntent) {
        Injector.inject(this);
        if (initialIntent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo ni = initialIntent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            // Sometimes the extra is not there (seen in crash reports)
            if (ni == null) {
                return;
            }
            NetworkInfo.DetailedState state = ni.getDetailedState();
            Timber.d("Wifi state %s", state.toString());
            if (state.equals(NetworkInfo.DetailedState.CONNECTED)) {
//                String bssid = initialIntent.getStringExtra(WifiManager.EXTRA_BSSID);
                WifiInfo wifiInfo = initialIntent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                // Sometimes the parcelable extra is not there (seen in crash reports)
                if (wifiInfo == null) {
                    return;
                }
                String ssid = wifiInfo.getSSID();
                if (Strings.isBlank(ssid) || ssid.contains("<unknown ssid>")) {
                    return;
                }
//                Toasts.show(R.string.all_connected_to_ap, ssid);

                if (!InfrastructureUtils.isRainmachineSSID(ssid)) {
                    WifiUtils.setHomeWifiSSID(ssid);
                }

                bus.post(new WifiNetworkEvent(true, ssid, wifiInfo.getBSSID()));
            } else if (state.equals(NetworkInfo.DetailedState.DISCONNECTED)) {
                Timber.d("Wifi disconnected");
                bus.post(new WifiNetworkEvent(false));
            }
        }
    }

    public static class WifiNetworkEvent extends BaseEvent {
        public boolean connected;
        public String ssid;
        public String mac;

        public WifiNetworkEvent(boolean connected) {
            this(connected, null, null);
        }

        public WifiNetworkEvent(boolean connected, String ssid, String mac) {
            this.connected = connected;
            this.ssid = ssid;
            this.mac = mac;
        }
    }
}
