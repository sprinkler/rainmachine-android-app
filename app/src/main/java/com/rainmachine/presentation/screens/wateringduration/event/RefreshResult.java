package com.rainmachine.presentation.screens.wateringduration.event;

import com.rainmachine.domain.usecases.wateringduration.GetWateringDurationForZones;

public class RefreshResult extends WateringDurationResult {
    public GetWateringDurationForZones.ResponseModel responseModel;

    public RefreshResult(GetWateringDurationForZones.ResponseModel responseModel) {
        this.responseModel = responseModel;
    }
}
