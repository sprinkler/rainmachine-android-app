package com.rainmachine.domain.usecases.remoteaccess;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.usecase.CompletableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;

public class ToggleRemoteAccess extends CompletableUseCase<ToggleRemoteAccess.RequestModel> {

    private SprinklerRepository sprinklerRepository;

    public ToggleRemoteAccess(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Completable execute(final RequestModel requestModel) {
        return sprinklerRepository.enableCloud(requestModel.enable);
    }

    public static class RequestModel {
        boolean enable;

        public RequestModel(boolean enable) {
            this.enable = enable;
        }
    }
}
