package com.rainmachine.infrastructure.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.injection.Injector;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import timber.log.Timber;

public class OnConnectivityChangeReceiver extends BroadcastReceiver {

    @Inject
    Bus bus;

    @Override
    public void onReceive(Context ctx, Intent initialIntent) {
        Timber.d("Connectivity change receiver");
        Injector.inject(this);
        if (initialIntent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            bus.post(new WifiStateEvent(WifiUtils.isWifiEnabled()));
        }
    }

    public static class WifiStateEvent extends BaseEvent {
        public boolean enabled;

        public WifiStateEvent(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
