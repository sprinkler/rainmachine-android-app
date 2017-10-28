package com.rainmachine.infrastructure;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.util.ForegroundDetector;
import com.rainmachine.presentation.util.Toasts;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class UpdateHandler {

    private Features features;
    private Context context;
    private Device device;
    private SprinklerState sprinklerState;
    private SprinklerRepository sprinklerRepository;
    private ForegroundDetector foregroundDetector;

    private BehaviorSubject<Boolean> subject = BehaviorSubject.create();

    public UpdateHandler(Context context, Device device, Features features, SprinklerState
            sprinklerState, SprinklerRepository sprinklerRepository,
                         ForegroundDetector foregroundDetector) {
        this.context = context;
        this.device = device;
        this.features = features;
        this.sprinklerState = sprinklerState;
        this.sprinklerRepository = sprinklerRepository;
        this.foregroundDetector = foregroundDetector;
    }

    public Observable<Boolean> triggerUpdate() {
        Observable<Irrelevant> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository.triggerUpdate().toObservable();
        } else {
            observable = sprinklerRepository.triggerUpdate3().toObservable();
        }
        return observable
                .doOnSubscribe(disposable -> {
                    subject.onNext(true);
                    sprinklerState.setRefreshersBlocked(true);
                })
                .flatMap(irrelevant -> Observable
                        .interval(7, 7, TimeUnit.SECONDS)
                        .take(26)
                        .switchMap(step -> {
                            Observable<Update> observable1;
                            if (features.useNewApi()) {
                                observable1 = sprinklerRepository.update(false).toObservable();
                            } else {
                                observable1 = sprinklerRepository.update3(false).toObservable();
                            }
                            return observable1.onErrorResumeNext(Observable.empty());
                        }))
                .doOnNext(update -> {
                    if (features.useNewApi()) {
                        if (update.status == Update.Status.IDLE) {
                            Toasts.show(context.getString(R.string.all_success_update, device
                                    .name));
                        } else if (update.status == Update.Status.ERROR) {
                            Toasts.show(context.getString(R.string.all_error_update, device
                                    .name));
                        } else if (update.status == Update.Status.REBOOT) {
                            Toasts.show(R.string.all_rebooting, device.name);
                        }
                    } else {
                        if (update.status == Update.Status.IDLE) {
                            Toasts.show(context.getString(R.string.all_success_update, device
                                    .name));
                        }
                    }
                })
                .filter(update -> {
                    if (features.useNewApi()) {
                        return update.status == Update.Status.IDLE ||
                                update.status == Update.Status.ERROR;
                    } else {
                        return update.status == Update.Status.IDLE;
                    }
                })
                .map(update -> {
                    if (features.useNewApi()) {
                        return update.status == Update.Status.IDLE;
                    } else {
                        return update.status == Update.Status.IDLE;
                    }
                })
                .first(false)
                .toObservable()
                .doOnNext(success -> {
                    if (foregroundDetector.isBackground()) {
                        sendNotification(2000, context.getString(R.string.all_device_update,
                                device
                                        .name), context.getString(success ? R.string
                                .all_success_update : R
                                .string.all_error_update, device.name));
                    }
                })
                .doAfterTerminate(() -> {
                    sprinklerState.setRefreshersBlocked(false);
                    subject.onNext(false);
                });
    }

    private void sendNotification(int notificationId, String title, String message) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_rainmachine_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(devicesScreenIntent());
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private PendingIntent devicesScreenIntent() {
        Intent intent = new Intent(context, DevicesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent
                .FLAG_ONE_SHOT);
    }

    public boolean isUpdateInProgress() {
        if (subject.hasValue()) {
            return subject.getValue();
        }
        return false;
    }

    public Observable<Boolean> updateInProgress() {
        return subject;
    }
}
