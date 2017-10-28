package com.rainmachine.presentation.screens.systemsettings;

import com.rainmachine.domain.model.CloudSettings;

import org.joda.time.LocalDateTime;

class SystemSettingsViewModel {
    public boolean isUnitsMetric;
    public LocalDateTime sprinklerLocalDateTime;
    public boolean use24HourFormat;
    public String deviceName;
    public String timezone;
    public CloudSettings cloudSettings;
    public String address;
    public boolean enabledWifiSettings;
}
