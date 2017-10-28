package com.rainmachine.domain.usecases.restriction;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.CurrentActiveRestrictions;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class GetRestrictionsLive extends ObservableUseCase<GetRestrictionsLive.RequestModel,
        GetRestrictionsLive.ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private SprinklerState sprinklerState;
    private Features features;

    private Observable<ResponseModel> stream;
    private Relay<Long> forceRefresh;

    public GetRestrictionsLive(SprinklerRepository sprinklerRepository, SprinklerState
            sprinklerState, Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.sprinklerState = sprinklerState;
        this.features = features;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        if (stream == null) {
            forceRefresh = PublishRelay.<Long>create().toSerialized();
            Observable<Long> interval = Observable.interval(0, 60, TimeUnit.SECONDS);

            Observable<ResponseModel> network = Observable.merge(forceRefresh, interval)
                    .filter(step -> !sprinklerState.isRefreshersBlocked())
                    .switchMap(step -> {
                        if (features.isApiAtLeast41()) {
                            return api();
                        } else {
                            return apiOld();
                        }
                    });

            stream = network.replay(1).refCount();
        } else {
            if (requestModel.forceRefresh) {
                forceRefresh();
            }
        }
        return stream;
    }

    public void forceRefresh() {
        if (forceRefresh != null) {
            forceRefresh.accept(-1L);
        }
    }

    private Observable<ResponseModel> api() {
        return sprinklerRepository
                .currentRestrictions().toObservable()
                .map(restrictions -> ResponseModel.from(restrictions))
                .onErrorResumeNext(Observable.empty());
    }

    private Observable<ResponseModel> apiOld() {
        Observable<Long> observable1;
        if (features.useNewApi()) {
            observable1 = sprinklerRepository.rainDelay().toObservable();
        } else {
            observable1 = sprinklerRepository.rainDelay3().toObservable();
        }
        return observable1
                .map(rainDelay -> ResponseModel.from(rainDelay))
                .onErrorResumeNext(Observable.empty());
    }

    public static class RequestModel {
        boolean forceRefresh;

        public RequestModel(boolean forceRefresh) {
            this.forceRefresh = forceRefresh;
        }
    }

    public static class ResponseModel {
        public boolean hourly;
        public boolean freeze;
        public boolean month;
        public boolean weekDay;
        public boolean rainDelay;
        public int rainDelayCounter;
        public boolean rainSensor;
        public int numActiveRestrictions;

        static ResponseModel from(CurrentActiveRestrictions restrictions) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.freeze = restrictions.freeze;
            responseModel.hourly = restrictions.hourly;
            responseModel.month = restrictions.month;
            responseModel.rainDelay = restrictions.rainDelay;
            responseModel.rainDelayCounter = restrictions.rainDelayCounter;
            responseModel.rainSensor = restrictions.rainSensor;
            responseModel.weekDay = restrictions.weekDay;
            responseModel.numActiveRestrictions = numActive(restrictions.freeze)
                    + numActive(restrictions.hourly) + numActive(restrictions.month)
                    + numActive(restrictions.rainDelay)
                    + numActive(restrictions.rainSensor) + numActive(restrictions
                    .weekDay);
            return responseModel;
        }

        private static int numActive(boolean restriction) {
            return restriction ? 1 : 0;
        }

        static ResponseModel from(long counterRemaining) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.rainDelay = counterRemaining > 0;
            responseModel.rainDelayCounter = (int) counterRemaining;
            responseModel.numActiveRestrictions = responseModel.rainDelay ? 1 : 0;
            return responseModel;
        }
    }
}
