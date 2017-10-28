package com.rainmachine.infrastructure.tasks;


import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.injection.Injector;

import javax.inject.Inject;

import timber.log.Timber;

import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_SUCCESS;

public class UpdatePushNotificationSettingsService extends GcmTaskService {

    @Inject
    UpdatePushNotificationSettings updatePushNotificationSettings;

    @Override
    public int onRunTask(TaskParams taskParams) {
        Timber.i("Run the gcm task to update the push notification settings");
        Injector.inject(this);

        updatePushNotificationSettings.execute(new UpdatePushNotificationSettings.RequestModel())
                .blockingAwait();

        // If the call fails, the use case itself will automatically reschedule so we should
        // always return success here
        return RESULT_SUCCESS;
    }
}