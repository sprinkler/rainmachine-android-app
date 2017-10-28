package com.rainmachine.presentation.screens.pushnotifications;


import java.util.List;

class SectionViewModel {
    public Type type;
    public List<PushNotificationViewModel> pushNotifications;

    public enum Type {GLOBAL, AVAILABLE}
}
