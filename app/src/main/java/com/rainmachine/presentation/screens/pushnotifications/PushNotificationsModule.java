package com.rainmachine.presentation.screens.pushnotifications;

import com.rainmachine.domain.usecases.pushnotification.CheckPushNotificationsPossible;
import com.rainmachine.domain.usecases.pushnotification.GetAllPushNotifications;
import com.rainmachine.domain.usecases.pushnotification.TogglePushNotification;
import com.rainmachine.injection.AppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                PushNotificationsActivity.class
        }
)
class PushNotificationsModule {

    @Provides
    @Singleton
    PushNotificationsContract.Presenter providePresenter(GetAllPushNotifications
                                                                 getAllPushNotifications,
                                                         TogglePushNotification
                                                                 togglePushNotification,
                                                         CheckPushNotificationsPossible
                                                                 checkPushNotificationsPossible) {
        return new PushNotificationsPresenter(getAllPushNotifications, togglePushNotification,
                checkPushNotificationsPossible);
    }
}
