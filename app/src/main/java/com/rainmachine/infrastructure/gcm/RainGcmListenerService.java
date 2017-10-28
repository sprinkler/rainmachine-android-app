package com.rainmachine.infrastructure.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.rainmachine.R;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.util.RainApplication;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import io.reactivex.Observable;
import timber.log.Timber;

public class RainGcmListenerService extends GcmListenerService {

    @Inject
    Context context;
    @Inject
    Gson gson;
    @Inject
    DatabaseRepositoryImpl databaseRepository;
    @Inject
    PrefRepository prefRepository;

    public RainGcmListenerService() {
        super();
        Injector.inject(this);
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, final Bundle data) {
        Timber.d("Received push notification");
        if (RainApplication.isDebugLogging()) {
            for (String key : data.keySet()) {
                Timber.d("%s : %s", key, data.get(key));
            }
        }

        try {
//            final int notificationType = Integer.parseInt(data.getString("notification_type"));
            final String title = data.getString("title");
            final String body = data.getString("body");
            getDevice(data)
                    .compose(RunOnProperThreads.instance())
                    .onErrorReturn(throwable -> new Device())
                    .subscribe(device -> sendNotification(title, body, goToDevicesScreenIntent()));
        } catch (Throwable t) {
            Timber.w("Error parsing push notification %s", t);
        }
    }

    private Observable<Device> getDevice(final Bundle data) {
        return Observable.fromCallable(() -> {
            String sprinklerJson = data.getString("sprinkler");
            SprinklerPush sprinkler = gson.fromJson(sprinklerJson, SprinklerPush.class);
            String deviceId = Strings.valueOrDefault(sprinkler.sprinkler_mac, "")
                    .toLowerCase(Locale.ENGLISH);
            Timber.d("device mac: %s", deviceId);
            Device device = databaseRepository.getDevice(deviceId, Device
                    .SPRINKLER_TYPE_UDP);
            if (device == null) {
                device = databaseRepository.getDevice(deviceId, Device
                        .SPRINKLER_TYPE_CLOUD);
            }
            return device;
        });
    }

    private PendingIntent goToDevicesScreenIntent() {
        Intent intent = new Intent(context, DevicesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent
                .FLAG_ONE_SHOT);
    }

    private void sendNotification(String title, String message, PendingIntent pendingIntent) {
        final int NOTIFICATION_ID_BASE = 100;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_rainmachine_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int randomNotificationId = NOTIFICATION_ID_BASE + new Random().nextInt(5000);
        notificationManager.notify(randomNotificationId, notificationBuilder.build());
    }

    private static class SprinklerPush {
        String sprinkler_mac;
    }
}
