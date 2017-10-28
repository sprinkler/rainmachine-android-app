package com.rainmachine.presentation.screens.wateringduration.event;

import com.rainmachine.presentation.screens.wateringduration.ZoneViewModel;

public class ClickZoneResult extends WateringDurationResult {
    public ZoneViewModel zone;

    public ClickZoneResult(ZoneViewModel zone) {
        this.zone = zone;
    }
}
