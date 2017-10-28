package com.rainmachine.presentation.screens.hiddendrawer;

public class LoggingSettingItem {
    public boolean enabled;

    public LoggingSettingItem(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return enabled ? "enabled" : "disabled";
    }
}
