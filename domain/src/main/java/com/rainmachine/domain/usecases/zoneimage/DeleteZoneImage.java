package com.rainmachine.domain.usecases.zoneimage;


import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.util.Timberific;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class DeleteZoneImage extends ObservableUseCase<DeleteZoneImage.RequestModel, DeleteZoneImage
        .ResponseModel> {

    private ZoneImageRepository zoneImageRepository;
    private InfrastructureService infrastructureService;

    public DeleteZoneImage(InfrastructureService infrastructureService,
                           ZoneImageRepository zoneImageRepository) {
        this.infrastructureService = infrastructureService;
        this.zoneImageRepository = zoneImageRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return zoneImageRepository.deleteZoneImage(requestModel.macAddress, requestModel.zoneId,
                requestModel.coordinates)
                .doOnError(throwable -> {
                    Timberific.i("Keep trying to delete the zone image in the background");
                    infrastructureService.scheduleDeleteZoneImageRetry(requestModel.macAddress,
                            requestModel.zoneId, requestModel.coordinates);
                })
                .onErrorComplete()
                .andThen(Observable.just(new ResponseModel()));
    }

    public static class RequestModel {
        public String macAddress;
        public long zoneId;
        public LatLong coordinates;

        public RequestModel(String macAddress, long zoneId, LatLong coordinates) {
            this.macAddress = macAddress;
            this.zoneId = zoneId;
            this.coordinates = coordinates;
        }
    }

    public static class ResponseModel {
    }
}
