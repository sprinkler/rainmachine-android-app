package com.rainmachine.domain.usecases.wateringduration;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class SaveWateringDuration extends ObservableUseCase<SaveWateringDuration.RequestModel,
        SaveWateringDuration.ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private SchedulerProvider schedulerProvider;

    public SaveWateringDuration(SprinklerRepository sprinklerRepository, SchedulerProvider
            schedulerProvider) {
        this.sprinklerRepository = sprinklerRepository;
        this.schedulerProvider = schedulerProvider;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return sprinklerRepository.provision()
                .map(provision -> provision.system.zoneDuration)
                .flatMapCompletable(zoneDurations -> {
                    zoneDurations.set((int) requestModel.zoneId - 1, requestModel.duration);
                    return sprinklerRepository.saveZoneDurations(zoneDurations);
                })
                .subscribeOn(schedulerProvider.io())
                .andThen(Observable.just(ResponseModel.success()))
                .onErrorReturn(throwable -> SaveWateringDuration.ResponseModel.error(throwable))
                .observeOn(schedulerProvider.ui())
                .startWith(SaveWateringDuration.ResponseModel.inFlight());
    }

    public static class RequestModel {
        public long zoneId;
        public long duration;

        public RequestModel(long zoneId, long duration) {
            this.zoneId = zoneId;
            this.duration = duration;
        }
    }

    public static class ResponseModel {
        public boolean inFlight;

        public boolean isSuccess;

        public boolean isError;
        public Throwable error;

        private ResponseModel() {
        }

        static ResponseModel inFlight() {
            ResponseModel responseModel = new ResponseModel();
            responseModel.inFlight = true;
            return responseModel;
        }

        static ResponseModel success() {
            ResponseModel responseModel = new ResponseModel();
            responseModel.isSuccess = true;
            return responseModel;
        }

        static ResponseModel error(Throwable throwable) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.isError = true;
            responseModel.error = throwable;
            return responseModel;
        }
    }
}
