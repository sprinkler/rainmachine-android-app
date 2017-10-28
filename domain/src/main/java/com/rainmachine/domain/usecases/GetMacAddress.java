package com.rainmachine.domain.usecases;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class GetMacAddress extends ObservableUseCase<GetMacAddress.RequestModel, GetMacAddress
        .ResponseModel> {

    private SprinklerRepository sprinklerRepository;

    public GetMacAddress(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        Observable<String> macStream;
        if (requestModel.isDeviceManual) {
            macStream = sprinklerRepository.wifiSettings().toObservable()
                    .concatMap(wifiSettings -> Observable.just(wifiSettings.macAddress));
        } else {
            macStream = Observable.just(requestModel.deviceId);
        }
        return macStream.map(macAddress -> new ResponseModel(macAddress));
    }

    public static class RequestModel {
        public String deviceId;
        public boolean isDeviceManual;

        public RequestModel(String deviceId, boolean isDeviceManual) {
            this.deviceId = deviceId;
            this.isDeviceManual = isDeviceManual;
        }
    }

    public static class ResponseModel {
        public String macAddress;

        public ResponseModel(String macAddress) {
            this.macAddress = macAddress;
        }
    }
}
