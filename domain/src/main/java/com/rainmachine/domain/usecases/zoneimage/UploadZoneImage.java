package com.rainmachine.domain.usecases.zoneimage;

import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.util.Timberific;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class UploadZoneImage extends ObservableUseCase<UploadZoneImage.RequestModel, UploadZoneImage
        .ResponseModel> {

    private final InfrastructureService infrastructureService;
    private final ZoneImageRepository zoneImageRepository;

    public UploadZoneImage(InfrastructureService infrastructureService,
                           ZoneImageRepository zoneImageRepository) {
        this.infrastructureService = infrastructureService;
        this.zoneImageRepository = zoneImageRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return uploadZoneImage(requestModel.macAddress, requestModel.zoneId, requestModel
                .coordinates)
                .doOnError(throwable -> {
                    Timberific.i("Keep trying to upload the zone image in the background");
                    infrastructureService.scheduleUploadZoneImageRetry(requestModel.macAddress,
                            requestModel.zoneId, requestModel.coordinates);
                })
                .onErrorComplete()
                .andThen(Observable.just(new UploadZoneImage.ResponseModel()));
    }

    private Completable uploadZoneImage(final String macAddress, final long zoneId,
                                        final LatLong coordinates) {
        return zoneImageRepository.getLocalZoneImage(macAddress, zoneId)
                .flatMapCompletable(zoneImage ->
                        zoneImageRepository.uploadZoneImage(macAddress, zoneId, coordinates,
                                infrastructureService.getImageAsBytes(zoneImage.imageLocalPath))
                                .flatMapCompletable(imageUrl -> {
                                    zoneImage.imageUrl = imageUrl;
                                    Timberific.i("new firebase zone image url + " + zoneImage
                                            .imageUrl);
                                    return zoneImageRepository.updateLocalZoneImage(macAddress,
                                            zoneImage);
                                }));
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
