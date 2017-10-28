package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.data.local.database.model.ZoneSettings;
import com.rainmachine.domain.model.ZoneProperties;

class ZoneDetailsViewModel {
    public ZoneProperties zoneProperties;
    public ZoneSettings zoneSettings;
    public ZoneProperties zonePropertiesOriginal;
    public ZoneSettings zoneSettingsOriginal;
    public boolean isUnitsMetric;
    public String deviceMacAddress;
    public boolean showEditImageActions;
}
