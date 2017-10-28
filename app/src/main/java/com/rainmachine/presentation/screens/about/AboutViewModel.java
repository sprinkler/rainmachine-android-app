package com.rainmachine.presentation.screens.about;

import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.Versions;
import com.rainmachine.domain.model.WifiSettingsSimple;

class AboutViewModel {
    public Update update;
    public WifiSettingsSimple wifiSettings;
    public Versions versions;
    public int cpuUsage;
    public String uptime;
    public long memUsage;
    public String gatewayAddress;

    // Added in API 4.1
    public String remoteAccessStatus;
    public boolean showRemoteAccessStatus;
}
