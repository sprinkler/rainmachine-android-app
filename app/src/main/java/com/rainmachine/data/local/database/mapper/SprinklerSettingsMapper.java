package com.rainmachine.data.local.database.mapper;

import com.rainmachine.data.local.database.model.SprinklerSettings;
import com.rainmachine.domain.model.DevicePreferences;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class SprinklerSettingsMapper implements Function<SprinklerSettings, DevicePreferences> {

    private static volatile SprinklerSettingsMapper instance;

    public static SprinklerSettingsMapper instance() {
        if (instance == null) {
            instance = new SprinklerSettingsMapper();
        }
        return instance;
    }

    @Override
    public DevicePreferences apply(@NonNull SprinklerSettings sprinklerSettings) throws Exception {
        DevicePreferences devicePreferences = new DevicePreferences();
        devicePreferences.isUnitsMetric = sprinklerSettings.isUnitsMetric();
        devicePreferences.use24HourFormat = sprinklerSettings.use24HourFormat;
        return devicePreferences;
    }
}
