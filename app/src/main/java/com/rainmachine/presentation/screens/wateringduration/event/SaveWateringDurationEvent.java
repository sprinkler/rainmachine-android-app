package com.rainmachine.presentation.screens.wateringduration.event;

import com.rainmachine.presentation.screens.wateringduration.ZoneViewModel;

public class SaveWateringDurationEvent extends WateringDurationViewEvent {
    public final ZoneViewModel zone;

    public SaveWateringDurationEvent(ZoneViewModel zone) {
        this.zone = zone;
    }
}