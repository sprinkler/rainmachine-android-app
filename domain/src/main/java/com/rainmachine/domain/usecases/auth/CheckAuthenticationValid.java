package com.rainmachine.domain.usecases.auth;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class CheckAuthenticationValid extends ObservableUseCase<CheckAuthenticationValid
        .RequestModel,
        CheckAuthenticationValid.ResponseModel> {

    private SprinklerRepository sprinklerRepository;

    public CheckAuthenticationValid(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return sprinklerRepository
                .testApiAuthenticated()
                .toObservable()
                .map(success -> new ResponseModel(success))
                .onErrorReturn(throwable -> new ResponseModel(false));
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public boolean success;

        public ResponseModel(boolean success) {
            this.success = success;
        }
    }
}
