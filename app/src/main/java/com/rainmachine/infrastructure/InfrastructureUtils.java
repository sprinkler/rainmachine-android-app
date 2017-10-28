package com.rainmachine.infrastructure;

import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.rainmachine.BuildConfig;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.util.BaseUrlSelectionInterceptor;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.util.BaseApplication;

import java.util.Locale;

import timber.log.Timber;

public class InfrastructureUtils {

    public static final String ACTION_FINISH_ACTIVITY = "com.rainmachine.ACTION_FINISH_ACTIVITY";
    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.PROVIDER_FILE_AUTHORITY;

    public static boolean shouldSwitchToCloud(Device device) {
        return device.isUdp() && !Strings.isBlank(device.alternateCloudUrl) && !WifiUtils
                .isWifiConnected();
    }

    public static boolean shouldSwitchToWifi(Device device) {
        return device.isCloud() && !Strings.isBlank(device.alternateCloudUrl) && !NetworkUtils
                .isMobileConnected();
    }

    public static void switchToCloud(Device device, BaseUrlSelectionInterceptor
            baseUrlSelectionInterceptor) {
        Timber.i("Switching to using cloud url %s", device.alternateCloudUrl);
        device.type = Device.SPRINKLER_TYPE_CLOUD;
        baseUrlSelectionInterceptor.setBaseUrl(device.alternateCloudUrl);
        NetworkUtils.clearNetworkTrafficRouting();
    }

    public static void switchToWifi(Device device, BaseUrlSelectionInterceptor
            baseUrlSelectionInterceptor) {
        Timber.i("Switching to using Wi-Fi url %s", device.getUrl());
        device.type = Device.SPRINKLER_TYPE_UDP;
        baseUrlSelectionInterceptor.setBaseUrl(device.getUrl());
        if (shouldRouteNetworkTrafficToWiFi(device)) {
            NetworkUtils.routeNetworkTrafficToCurrentWiFi();
        }
    }

    public static boolean shouldRouteNetworkTrafficToWiFi(Device device) {
        return device.isLocal() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void finishAllSprinklerActivities() {
        /* Finish all authenticated activities to get back to devices screen */
        Intent intent = new Intent(ACTION_FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(BaseApplication.getContext())
                .sendBroadcastSync(intent);
    }

    public static boolean isRainmachineSSID(String ssid) {
        return !Strings.isBlank(ssid)
                && (ssid.toLowerCase(Locale.ENGLISH).startsWith("rainmachine")
                || ssid.toLowerCase(Locale.ENGLISH).startsWith("\"rainmachine"));
    }
}
