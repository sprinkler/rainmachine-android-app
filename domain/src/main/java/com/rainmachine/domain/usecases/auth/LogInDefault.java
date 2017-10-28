package com.rainmachine.domain.usecases.auth;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class LogInDefault extends ObservableUseCase<LogInDefault.RequestModel, LogInDefault
        .ResponseModel> {

    private SprinklerRepository sprinklerRepository;

    public LogInDefault(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return sprinklerRepository
                .login("", false)
                .toObservable()
                .map(status -> new ResponseModel(status == LoginStatus.SUCCESS))
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
