package com.rainmachine.presentation.screens.hiddendrawer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.rainmachine.BuildConfig;
import com.rainmachine.R;
import com.rainmachine.data.remote.cloud.PushNotificationsDataStoreRemote;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.palaima.debugdrawer.base.DebugModule;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class PushDrawerModule implements DebugModule {

    @Inject
    PushNotificationsDataStoreRemote pushNotificationsDataStoreRemote;

    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.btn_send_push)
    View btnSendPush;

    private Activity activity;

    public PushDrawerModule(Activity activity) {
        this.activity = activity;
        Injector.inject(this);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.hidden_drawer_module_push, parent, false);
        ButterKnife.bind(this, view);
        setup();
        return view;
    }

    private void setup() {
        List<String> list = new ArrayList<>();
        list.add("disconnected");
        list.add("back online");
        list.add("software update");
        list.add("cloud settings");
        list.add("zone");
        list.add("program");
        list.add("weather");
        list.add("rain sensor");
        list.add("rain delay");
        list.add("freeze temperature");
        list.add("reboot");
        list.add("short");
        final GenericSpinnerAdapter<String> adapter = new GenericSpinnerAdapter<>(activity, list);
        spinner.setAdapter(adapter);
        setSelection();

        btnSendPush.setEnabled(true);
    }

    private void setSelection() {
        int currentPosition = spinner.getSelectedItemPosition();
        if (currentPosition == Spinner.INVALID_POSITION) {
            currentPosition = 0;
        }
        spinner.setSelection(currentPosition);
    }

    @OnClick(R.id.btn_send_push)
    public void onClickSendPushNotification() {
        Observable
                .fromCallable(() -> {
                    try {
                        InstanceID instanceID = InstanceID.getInstance(activity);
                        String token = instanceID.getToken(BuildConfig.GCM_SENDER_ID,
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        int position = spinner.getSelectedItemPosition();
                        int notificationType = notificationType(position);
                        boolean isUnitsMetric = false;
                        boolean use24HourFormat = false;
                        pushNotificationsDataStoreRemote
                                .triggerNotification(token, notificationType, notificationEvent
                                        (position), isUnitsMetric, use24HourFormat)
                                .blockingGet();
                        Toasts.show("Successfully sent push notification request to Tremend " +
                                "server");
                    } catch (Exception e) {
                        Toasts.show("Problem sending the push notification request to Tremend " +
                                "server");
                    }
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    // 1 - disconnected, 2 - update available, 3 - back online
    private int notificationType(int position) {
        switch (position) {
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 2;
            case 3:
                return 11111;
            case 4:
                return 10001;
            case 5:
                return 10002;
            case 6:
                return 10003;
            case 7:
                return 10004;
            case 8:
                return 10005;
            case 9:
                return 10006;
            case 10:
                return 10007;
            case 11:
                return 10008;
        }
        return -1;
    }

    private String notificationEvent(int position) {
        switch (position) {
            case 0:
                return "1";
            case 1:
                return "3";
            case 2:
                return "2";
            case 3:
                return "11111";
            case 4:
                return "10001,1,2";
            case 5:
                return "10002,1,3";
            case 6:
                return "10003,1,4";
            case 7:
                return "10004,1";
            case 8:
                return "10005,1";
            case 9:
                return "10006,1,-5";
            case 10:
                return "10007";
            case 11:
                return "10008";
        }
        return "";
    }

    @Override
    public void onOpened() {
        setSelection();
    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
