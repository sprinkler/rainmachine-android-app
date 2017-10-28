package com.rainmachine.presentation.screens.wateringduration.event;

import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration;
import com.rainmachine.presentation.screens.wateringduration.ZoneViewModel;

public class SaveWateringDurationResult extends WateringDurationResult {
    public ZoneViewModel zone;
    public SaveWateringDuration.ResponseModel responseModel;

    public SaveWateringDurationResult(ZoneViewModel zone, SaveWateringDuration.ResponseModel
            responseModel) {
        this.zone = zone;
        this.responseModel = responseModel;
    }
}
