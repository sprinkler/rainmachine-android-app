package com.rainmachine.infrastructure;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import com.rainmachine.infrastructure.util.BaseApplication;

import java.io.IOException;
import java.net.DatagramSocket;

import timber.log.Timber;

public class NetworkUtils {

    public static boolean routeNetworkTrafficToCurrentWiFi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context ctx = BaseApplication.getContext();
            ConnectivityManager connectivityManager = ((ConnectivityManager) ctx.getSystemService
                    (Context.CONNECTIVITY_SERVICE));
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null) {
                return false;
            }
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info
                        .isAvailable() && info.isConnected()) {
                    Timber.i("Route network traffic to Wi-Fi %s", WifiUtils.getCurrentSSID());
                    return connectivityManager.bindProcessToNetwork(network);
                }
            }
            return false;
        }
        return true;
    }

    public static void clearNetworkTrafficRouting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Timber.d("Clear network traffic routing");
            Context ctx = BaseApplication.getContext();
            ConnectivityManager connectivityManager = ((ConnectivityManager) ctx.getSystemService
                    (Context.CONNECTIVITY_SERVICE));
            connectivityManager.bindProcessToNetwork(null);
        }
    }

    public static boolean bindToWifiNetwork(DatagramSocket socket) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context ctx = BaseApplication.getContext();
            ConnectivityManager connectivityManager = ((ConnectivityManager) ctx.getSystemService
                    (Context.CONNECTIVITY_SERVICE));
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null) {
                return false;
            }
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info
                        .isAvailable() && info.isConnected()) {
                    try {
                        network.bindSocket(socket);
                        return true;
                    } catch (IOException ioe) {
                        Timber.w(ioe, ioe.getMessage());
                        return false;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isMobileConnected() {
        Context ctx = BaseApplication.getContext();
        ConnectivityManager connectivityManager = ((ConnectivityManager) ctx.getSystemService
                (Context.CONNECTIVITY_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null) {
                return false;
            }
            boolean isMobileConnected = false;
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (info.isAvailable() && info.isConnected()) {
                        isMobileConnected = true;
                        break;
                    }
                }
            }
            return isMobileConnected;
        } else {
            //noinspection deprecation
            NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return info != null && info.isAvailable() && info.isConnected();
        }
    }
}
