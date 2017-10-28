package com.rainmachine.data.local.database.model;

import android.text.format.DateFormat;

import com.rainmachine.infrastructure.util.RainApplication;

public class SprinklerSettings {
    private static final String UNITS_CELSIUS = "C";
    private static final String UNITS_FAHRENHEIT = "F";

    public Long _id;
    public String deviceId;
    public String units;
    public boolean use24HourFormat;

    public boolean isUnitsMetric() {
        return UNITS_CELSIUS.equalsIgnoreCase(units);
    }

    public void setUnitsUS() {
        units = UNITS_FAHRENHEIT;
    }

    public static String getUnitsInternalValue(boolean isUnitsMetric) {
        return isUnitsMetric ? UNITS_CELSIUS : UNITS_FAHRENHEIT;
    }

    private static String getDefaultUnitsInternalValue() {
        return UNITS_FAHRENHEIT;
    }

    public static SprinklerSettings createDefault(Device device) {
        SprinklerSettings sprinklerSettings = new SprinklerSettings();
        sprinklerSettings.deviceId = device.deviceId;
        sprinklerSettings.units = getDefaultUnitsInternalValue();
        sprinklerSettings.use24HourFormat = DateFormat.is24HourFormat(RainApplication.getContext());
        return sprinklerSettings;
    }
}
