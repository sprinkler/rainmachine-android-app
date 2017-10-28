package com.rainmachine.domain.usecases.zoneimage;

import com.pacoworks.rxtuples2.RxTuples;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.util.usecase.CompletableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class SyncZoneImages extends CompletableUseCase<SyncZoneImages.RequestModel> {

    private GetMacAddress getMacAddress;
    private ZoneImageRepository zoneImageRepository;
    private SprinklerRepository sprinklerRepository;

    public SyncZoneImages(GetMacAddress getMacAddress, ZoneImageRepository zoneImageRepository,
                          SprinklerRepository sprinklerRepository) {
        this.getMacAddress = getMacAddress;
        this.zoneImageRepository = zoneImageRepository;
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Completable execute(RequestModel requestModel) {
        return Observable.combineLatest(
                getMacAddress
                        .execute(new GetMacAddress.RequestModel(requestModel.deviceId,
                                requestModel.isDeviceManual))
                        .map(responseModel -> responseModel.macAddress),
                sprinklerRepository
                        .provision().toObservable()
                        .map(provision -> new LatLong(provision.location.latitude,
                                provision.location.longitude)),
                RxTuples.toPair())
                .concatMap(pair -> zoneImageRepository.getRemoteZoneImages
                        (pair.getValue0(), pair.getValue1()).toObservable())
                .flatMap(zoneImages -> Observable.fromIterable(zoneImages))
                .flatMapCompletable(zoneImage -> zoneImageRepository.updateLocalZoneImage
                        (requestModel
                                .deviceId, zoneImage));
    }

    public static class RequestModel {
        public String deviceId;
        public boolean isDeviceManual;

        public RequestModel(String deviceId, boolean isDeviceManual) {
            this.deviceId = deviceId;
            this.isDeviceManual = isDeviceManual;
        }
    }
}
