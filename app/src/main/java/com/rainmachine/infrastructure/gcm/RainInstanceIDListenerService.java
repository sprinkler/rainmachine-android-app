package com.rainmachine.infrastructure.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RainInstanceIDListenerService extends InstanceIDListenerService {

    @Inject
    PrefRepository prefRepository;
    @Inject
    UpdatePushNotificationSettings updatePushNotificationSettings;

    public RainInstanceIDListenerService() {
        super();
        Injector.inject(this);
    }

    @Override
    public void onTokenRefresh() {
        Timber.d("Token was refreshed");
        updatePushNotificationSettings.execute(new UpdatePushNotificationSettings.RequestModel())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
