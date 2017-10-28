package com.rainmachine.presentation.screens.currentrestrictions;

import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Provision;

import org.joda.time.LocalDateTime;

class CurrentRestrictionsViewModel {
    boolean isRainDelay;
    int rainDelayCounterRemaining;

    boolean isRainSensor;
    Provision.RainSensorSnoozeDuration rainSensorSnoozeDuration;

    boolean isFreezeProtect;
    int freezeProtectTemp;
    boolean isUnitsMetric;

    boolean isMonth;
    LocalDateTime.Property monthOfYear;

    boolean isDay;
    LocalDateTime.Property dayOfWeek;

    boolean isHour;
    HourlyRestriction hourlyRestriction;
    boolean use24HourFormat;

    int numActiveRestrictions;
}
