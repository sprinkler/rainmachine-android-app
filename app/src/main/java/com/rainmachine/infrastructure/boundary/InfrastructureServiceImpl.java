package com.rainmachine.infrastructure.boundary;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.iid.InstanceID;
import com.rainmachine.BuildConfig;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.StreamUtils;
import com.rainmachine.infrastructure.tasks.DeleteZoneImageService;
import com.rainmachine.infrastructure.tasks.UpdatePushNotificationSettingsService;
import com.rainmachine.infrastructure.tasks.UploadZoneImageService;
import com.rainmachine.infrastructure.util.BaseApplication;

import java.io.IOException;

import timber.log.Timber;

public class InfrastructureServiceImpl implements InfrastructureService {

    private Context context;

    public InfrastructureServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public String getSystemId() {
        String uniqueId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (!Strings.isBlank(uniqueId)) {
            return uniqueId;
        }
        return "THISISASTATICID";
    }

    @Override
    public String getPhoneId() {
        String phoneID = getSystemId();
        if ("THISISASTATICID".equals(phoneID)) {
            String errorMessage = "The device does not have a valid ANDROID_ID";
            Timber.e(new Throwable(errorMessage), errorMessage);
        }
        return phoneID;
    }

    @Override
    public String getInstaller() {
        String installer = context.getPackageManager().getInstallerPackageName(context
                .getPackageName());
        if ("com.android.vending".equals(installer)
                || "com.google.android.feedback".equals(installer)) {
            return "Google Play Store";
        }
        if ("com.amazon.venezia".equals(installer)) {
            return "Amazon App Store";
        }
        if (installer == null) {
            return "No store info";
        }
        return installer;
    }

    @Override
    public int getCurrentVersionCode() {
        try {
            Context ctx = BaseApplication.getContext();
            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            Timber.w(e, e.getMessage());
            return 0;
        }
    }

    @Override
    public String getUpdatedPushToken() throws IOException {
        return InstanceID.getInstance(context).getToken(BuildConfig
                .GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }

    @Override
    public void scheduleUpdatePushNotificationsSettingsRetry() {
        OneoffTask oneOff = new OneoffTask.Builder()
                .setService(UpdatePushNotificationSettingsService.class)
                .setExecutionWindow(0L, 30L)
                .setTag("update_push_notifications_settings")
                .setUpdateCurrent(true)
//              .setPersisted(true) Requires RECEIVE_BOOT_COMPLETED permission
                .build();
        GcmNetworkManager.getInstance(context).schedule(oneOff);
    }

    @Override
    public void scheduleDeleteZoneImageRetry(String macAddress, long zoneId, LatLong coordinates) {
        Bundle extras = new Bundle();
        extras.putString(DeleteZoneImageService.EXTRA_MAC_ADDRESS, macAddress);
        extras.putLong(DeleteZoneImageService.EXTRA_ZONE_ID, zoneId);
        extras.putDouble(DeleteZoneImageService.EXTRA_LATITUDE, coordinates.getLatitude());
        extras.putDouble(DeleteZoneImageService.EXTRA_LONGITUDE, coordinates.getLongitude());
        OneoffTask oneOff = new OneoffTask.Builder()
                .setService(DeleteZoneImageService.class)
                .setExecutionWindow(0L, 30L)
                .setTag("delete_zone_image_" + macAddress + "_" + zoneId)
                .setUpdateCurrent(false)
                .setExtras(extras)
//              .setPersisted(true) Requires RECEIVE_BOOT_COMPLETED permission
                .build();
        GcmNetworkManager.getInstance(context).schedule(oneOff);
    }

    @Override
    public void scheduleUploadZoneImageRetry(String macAddress, long zoneId, LatLong coordinates) {
        Bundle extras = new Bundle();
        extras.putString(UploadZoneImageService.EXTRA_MAC_ADDRESS, macAddress);
        extras.putLong(UploadZoneImageService.EXTRA_ZONE_ID, zoneId);
        extras.putDouble(UploadZoneImageService.EXTRA_LATITUDE, coordinates.getLatitude());
        extras.putDouble(UploadZoneImageService.EXTRA_LONGITUDE, coordinates.getLongitude());
        OneoffTask oneOff = new OneoffTask.Builder()
                .setService(UploadZoneImageService.class)
                .setExecutionWindow(0L, 30L)
                .setTag("upload_zone_image_" + macAddress + "_" + zoneId)
                .setUpdateCurrent(false)
                .setExtras(extras)
//              .setPersisted(true) Requires RECEIVE_BOOT_COMPLETED permission
                .build();
        GcmNetworkManager.getInstance(context).schedule(oneOff);
    }

    @Override
    public byte[] getResizedImageWithLargestSide(String imagePath, int side) {
        Bitmap scaledDownBitmap = StreamUtils.getScaledDownBitmap(imagePath, side);
        int width;
        int height;
        if (scaledDownBitmap.getWidth() > scaledDownBitmap.getHeight()) {
            width = side;
            float widthRatio = (1.0f * width) / scaledDownBitmap.getWidth();
            height = Math.round(scaledDownBitmap.getHeight() * widthRatio);
        } else {
            height = side;
            float heightRatio = (1.0f * height) / scaledDownBitmap.getHeight();
            width = Math.round(scaledDownBitmap.getWidth() * heightRatio);
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(scaledDownBitmap, width, height, false);
        return StreamUtils.bytes(bitmap);
    }

    @Override
    public byte[] getImageAsBytes(String imagePath) {
        return StreamUtils.readFile(imagePath);
    }
}
