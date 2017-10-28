package com.rainmachine.data.local.database.model;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.parceler.Parcel;

import nl.qbusict.cupboard.annotation.Ignore;

@Parcel
public class Device {

    public static final int SPRINKLER_TYPE_UDP = 0;
    public static final int SPRINKLER_TYPE_MANUAL = 1;
    public static final int SPRINKLER_TYPE_CLOUD = 2;
    public static final int SPRINKLER_TYPE_AP = 3;

    private static final String DEMO_DEVICE_ID = "18:c8:e7:81:a2:6e";

    public Long _id;
    public String name;
    String url;
    @Ignore
    public String alternateCloudUrl; // if UDP device and also cloud device, keep also cloud url
    public String deviceId; // It is either MAC in lower case hexadecimal OR url for manual devices
    public long timestamp;
    public int type;
    public boolean wizardHasRun;
    public String cloudEmail;
    public boolean isOffline;

    public boolean isAp() {
        return type == SPRINKLER_TYPE_AP;
    }

    public boolean isUdp() {
        return type == SPRINKLER_TYPE_UDP;
    }

    public boolean isManual() {
        return type == SPRINKLER_TYPE_MANUAL;
    }

    public boolean isCloud() {
        return type == SPRINKLER_TYPE_CLOUD;
    }

    // Device can be reached via local WiFi
    public boolean isLocal() {
        return isUdp() || isAp();
    }

    // Device can be reached via Internet
    public boolean isRemote() {
        return isCloud() || isManual();
    }

    public boolean isDemo() {
        return DEMO_DEVICE_ID.equals(deviceId);
    }

    public void setUrl(@NonNull String url) {
        this.url = url.trim();
    }

    public String getUrl() {
        return url;
    }

    public static Device demo() {
        Device demoDevice = new Device();
        demoDevice.deviceId = DEMO_DEVICE_ID;
        demoDevice.name = "RainMachine Demo";
        demoDevice.setUrl("https://demo.labs.rainmachine.com:18080/");
        demoDevice.type = Device.SPRINKLER_TYPE_MANUAL;
        demoDevice.timestamp = new DateTime().getMillis();
        demoDevice.wizardHasRun = true;
        demoDevice.cloudEmail = null;
        return demoDevice;
    }

    /* Database fields no longer used but still present in the database:
        int state;
        boolean isManual;
     */
}
