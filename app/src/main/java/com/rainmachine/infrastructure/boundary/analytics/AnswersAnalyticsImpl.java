package com.rainmachine.infrastructure.boundary.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.Gson;
import com.rainmachine.domain.boundary.infrastructure.Analytics;

import timber.log.Timber;

public class AnswersAnalyticsImpl implements Analytics {

    private SharedPreferences preferences;
    private Gson gson;

    public AnswersAnalyticsImpl(Context context, Gson gson) {
        preferences = context.getSharedPreferences("pref_answers", Context.MODE_PRIVATE);
        this.gson = gson;
    }

    @Override
    public void trackDevice(String deviceId, String apiVersion, String softwareVersion, String
            hardwareVersion) {
        Timber.d("track device " + deviceId);
        DeviceVersions oldDeviceVersions = fromPref(deviceId);
        if (oldDeviceVersions == null) {
            Timber.d("Log device " + deviceId);
            DeviceVersions deviceVersions = new DeviceVersions(apiVersion, softwareVersion,
                    hardwareVersion, "");
            toPref(deviceId, deviceVersions);

            logDeviceVersions(deviceVersions);
        } else {
            Timber.d("Log update device " + deviceId);

            boolean updated = false;
            String history = oldDeviceVersions.history + " | ";
            if (!apiVersion.equals(oldDeviceVersions.apiVersion)) {
                updated = true;
                history += oldDeviceVersions.apiVersion + " -> " + apiVersion + " ";
            }
            if (!softwareVersion.equals(oldDeviceVersions.softwareVersion)) {
                updated = true;
                history += oldDeviceVersions.softwareVersion + " -> " + softwareVersion + " ";
            }
            if (updated) {
                DeviceVersions deviceVersions = new DeviceVersions(apiVersion, softwareVersion,
                        hardwareVersion, history);
                logDeviceVersions(deviceVersions);
            }
        }
    }

    private DeviceVersions fromPref(String deviceId) {
        String json = preferences.getString(deviceId, null);
        try {
            return gson.fromJson(json, DeviceVersions.class);
        } catch (Throwable t) {
            return null;
        }
    }

    private void toPref(String deviceId, DeviceVersions deviceVersions) {
        String json = gson.toJson(deviceVersions);
        preferences.edit().putString(deviceId, json).apply();
    }

    private void logDeviceVersions(DeviceVersions deviceVersions) {
        CustomEvent event = new CustomEvent("RM Device");
        event.putCustomAttribute("API version", deviceVersions.apiVersion);
        event.putCustomAttribute("Software version", deviceVersions.softwareVersion);
        event.putCustomAttribute("Hardware version", deviceVersions.hardwareVersion);
        event.putCustomAttribute("History", deviceVersions.history);
        Answers.getInstance().logCustom(event);
    }
}
