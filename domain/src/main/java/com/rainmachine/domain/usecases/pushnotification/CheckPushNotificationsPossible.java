package com.rainmachine.domain.usecases.pushnotification;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class CheckPushNotificationsPossible extends ObservableUseCase<CheckPushNotificationsPossible
        .RequestModel, CheckPushNotificationsPossible.ResponseModel> {

    private final SprinklerRepository sprinklerRepository;

    public CheckPushNotificationsPossible(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return sprinklerRepository.cloudSettings()
                .map(cloudSettings -> {
                    boolean isPossible = cloudSettings.enabled && Strings.isBlank(cloudSettings
                            .pendingEmail);
                    return new ResponseModel(isPossible);
                })
                .toObservable();
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public boolean isPossible;

        ResponseModel(boolean isPossible) {
            this.isPossible = isPossible;
        }
    }
}
