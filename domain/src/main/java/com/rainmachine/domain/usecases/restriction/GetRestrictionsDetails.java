package com.rainmachine.domain.usecases.restriction;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.CurrentActiveRestrictions;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.usecases.GetRainSensor;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;

import java.util.List;

import io.reactivex.Observable;

public class GetRestrictionsDetails extends ObservableUseCase<GetRestrictionsDetails.RequestModel,
        GetRestrictionsDetails.ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private GetRainSensor getRainSensor;

    public GetRestrictionsDetails(SprinklerRepository sprinklerRepository, GetRainSensor
            getRainSensor) {
        this.sprinklerRepository = sprinklerRepository;
        this.getRainSensor = getRainSensor;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        Observable<ResponseModel> stream = sprinklerRepository.currentRestrictions().toObservable()
                .map(restrictions -> ResponseModel.from(restrictions));
        if (!requestModel.fullDetails) {
            return stream;
        } else {
            return stream
                    .flatMap(responseModel -> {
                        responseModel.hasFullDetails = true;
                        if (responseModel.freeze || responseModel.month || responseModel.weekDay) {
                            return sprinklerRepository
                                    .globalRestrictions().toObservable()
                                    .map(globalRestrictions -> {
                                        responseModel.globalRestrictions = globalRestrictions;
                                        return responseModel;
                                    });
                        }
                        return Observable.just(responseModel);
                    })
                    .flatMap(responseModel -> {
                        if (responseModel.hourly) {
                            return sprinklerRepository
                                    .hourlyRestrictions().toObservable()
                                    .map(hourlyRestrictions -> {
                                        responseModel.hourlyRestrictions = hourlyRestrictions;
                                        return responseModel;
                                    });
                        }
                        return Observable.just(responseModel);
                    })
                    .flatMap(responseModel -> {
                        if (responseModel.month || responseModel.weekDay || responseModel.hourly) {
                            return sprinklerRepository
                                    .timeDate().toObservable()
                                    .map(sprinklerDateTime -> {
                                        responseModel.sprinklerLocalDateTime = sprinklerDateTime;
                                        return responseModel;
                                    });
                        }
                        return Observable.just(responseModel);
                    })
                    .flatMap(responseModel -> {
                        if (responseModel.hourly || responseModel.freeze) {
                            return sprinklerRepository
                                    .devicePreferences().toObservable()
                                    .map(devicePreferences -> {
                                        responseModel.use24HourFormat = devicePreferences
                                                .use24HourFormat;
                                        responseModel.isUnitsMetric = devicePreferences
                                                .isUnitsMetric;
                                        return responseModel;
                                    });
                        }
                        return Observable.just(responseModel);
                    })
                    .flatMap(responseModel -> {
                        if (responseModel.rainSensor) {
                            return getRainSensor
                                    .execute(new GetRainSensor.RequestModel())
                                    .map(rainSensorModel -> {
                                        responseModel.rainSensorSnoozeDuration = rainSensorModel
                                                .rainSensorSnoozeDuration;
                                        return responseModel;
                                    });
                        }
                        return Observable.just(responseModel);
                    });
        }
    }


    public static class RequestModel {
        boolean fullDetails;

        public RequestModel(boolean fullDetails) {
            this.fullDetails = fullDetails;
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

        public boolean hasFullDetails;
        public GlobalRestrictions globalRestrictions;
        public List<HourlyRestriction> hourlyRestrictions;
        public LocalDateTime sprinklerLocalDateTime;
        public boolean isUnitsMetric;
        public boolean use24HourFormat;
        public Provision.RainSensorSnoozeDuration rainSensorSnoozeDuration;

        static ResponseModel from(CurrentActiveRestrictions restrictions) {
            ResponseModel responseModel = new ResponseModel();
            responseModel.hasFullDetails = false;
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
    }
}
