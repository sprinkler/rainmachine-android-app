package com.rainmachine.infrastructure.tasks;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.usecases.zoneimage.UploadZoneImage;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;

import timber.log.Timber;

import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_SUCCESS;

public class UploadZoneImageService extends GcmTaskService {

    public static String EXTRA_MAC_ADDRESS = "extra_mac_address";
    public static String EXTRA_ZONE_ID = "extra_zone_id";
    public static String EXTRA_LATITUDE = "extra_latitude";
    public static String EXTRA_LONGITUDE = "extra_longitude";

    @Inject
    UploadZoneImage uploadZoneImage;

    @Override
    public int onRunTask(TaskParams taskParams) {
        Timber.i("Run the gcm task to upload the zone image");

        Injector.inject(this);

        Bundle extras = taskParams.getExtras();
        final String macAddress = extras.getString(EXTRA_MAC_ADDRESS);
        final long zoneId = extras.getLong(EXTRA_ZONE_ID);
        final double latitude = extras.getDouble(EXTRA_LATITUDE);
        final double longitude = extras.getDouble(EXTRA_LONGITUDE);

        uploadZoneImage
                .execute(new UploadZoneImage.RequestModel(macAddress, zoneId,
                        new LatLong(latitude, longitude)))
                .blockingSubscribe();

        // If the call fails, the use case itself will automatically reschedule so we should
        // always return success here
        return RESULT_SUCCESS;
    }
}
