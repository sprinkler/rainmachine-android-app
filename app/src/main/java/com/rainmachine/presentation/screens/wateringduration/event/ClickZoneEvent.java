package com.rainmachine.presentation.screens.wateringduration.event;

import com.rainmachine.presentation.screens.wateringduration.ZoneViewModel;

public class ClickZoneEvent extends WateringDurationViewEvent {
    public ZoneViewModel zone;

    public ClickZoneEvent(ZoneViewModel zone) {
        this.zone = zone;
    }
}
