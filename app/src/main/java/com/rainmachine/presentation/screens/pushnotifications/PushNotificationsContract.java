package com.rainmachine.presentation.screens.pushnotifications;


import com.rainmachine.domain.model.PushNotification;

import java.util.List;

public interface PushNotificationsContract {

    interface View {

        void showError();

        void updateContent(List<SectionViewModel> pushNotifications);

        void showContent();

        void showProgress();

        void showRemoteAccessNeeded();

        void goToRemoteAccessScreen();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {
        void onTogglePushNotification(PushNotification pushNotification, boolean enabled);

        void onClickRetry();

        void onClickSetUpRemoteAccess();
    }
}
