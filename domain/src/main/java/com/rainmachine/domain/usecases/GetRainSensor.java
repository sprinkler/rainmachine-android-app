package com.rainmachine.domain.usecases;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class GetRainSensor extends ObservableUseCase<GetRainSensor.RequestModel, GetRainSensor
        .ResponseModel> {

    private SprinklerRepository sprinklerRepository;

    public GetRainSensor(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return sprinklerRepository
                .provision().toObservable()
                .map(provision -> {
                    ResponseModel responseModel = new ResponseModel();
                    responseModel.useRainSensor = provision.system.useRainSensor;
                    responseModel.rainSensorNormallyClosed = provision.system
                            .rainSensorNormallyClosed;
                    responseModel.rainSensorLastEvent = provision.system.rainSensorLastEvent;
                    responseModel.rainSensorSnoozeDuration = provision.system
                            .rainSensorSnoozeDuration;
                    return responseModel;
                });
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public boolean useRainSensor;
        public boolean rainSensorNormallyClosed;
        public Provision.RainSensorLastEvent rainSensorLastEvent;
        public Provision.RainSensorSnoozeDuration rainSensorSnoozeDuration;
    }
}
