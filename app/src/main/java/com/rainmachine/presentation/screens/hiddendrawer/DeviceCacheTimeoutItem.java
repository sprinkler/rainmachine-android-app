package com.rainmachine.presentation.screens.hiddendrawer;

public class DeviceCacheTimeoutItem {
    public String name;
    public int seconds;

    public DeviceCacheTimeoutItem(String name, int seconds) {
        this.name = name;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return name;
    }
}
