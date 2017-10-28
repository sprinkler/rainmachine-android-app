package com.rainmachine.domain.usecases.wateringduration;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.model.Zone;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class GetWateringDurationForZones extends ObservableUseCase<GetWateringDurationForZones
        .RequestModel, GetWateringDurationForZones.ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private SchedulerProvider schedulerProvider;

    public GetWateringDurationForZones(SprinklerRepository sprinklerRepository,
                                       SchedulerProvider schedulerProvider) {
        this.sprinklerRepository = sprinklerRepository;
        this.schedulerProvider = schedulerProvider;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return Observable.combineLatest(sprinklerRepository.provision().toObservable(),
                sprinklerRepository.zones().toObservable(),
                ((provision, zones) -> {
                    List<ZoneDto> zoneDtos = new ArrayList<>(zones.size());
                    for (Zone zone : zones) {
                        zoneDtos.add(map(zone, provision));
                    }
                    return ResponseModel.success(zoneDtos);
                }))
                .subscribeOn(schedulerProvider.io())
                .onErrorReturn(throwable -> ResponseModel.error(throwable))
                .observeOn(schedulerProvider.ui())
                .startWith(ResponseModel.inFlight());
    }

    private ZoneDto map(Zone zone, Provision provision) {
        ZoneDto zoneDto = new ZoneDto();
        zoneDto.id = zone.id;
        zoneDto.name = zone.name;
        zoneDto.isEnabled = zone.isEnabled;
        zoneDto.durationSeconds = provision.system.zoneDuration.get((int) zone.id - 1);
        return zoneDto;
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public boolean inFlight;

        public boolean isSuccess;
        public List<ZoneDto> zones;

        public boolean isError;
        public Throwable error;

        private ResponseModel() {
        }

        static ResponseModel inFlight() {
            ResponseModel responseModel = new ResponseModel();
            responseModel.inFlight = true;
            return responseModel;
        }

        static ResponseModel success(List<ZoneDto> zoneDtos) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.isSuccess = true;
            responseModel.zones = zoneDtos;
            return responseModel;
        }

        static ResponseModel error(Throwable throwable) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.isError = true;
            responseModel.error = throwable;
            return responseModel;
        }
    }

    public static class ZoneDto {
        public long id;
        public String name;
        public boolean isEnabled;
        public long durationSeconds;
    }
}
