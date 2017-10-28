package com.rainmachine.domain.usecases;

import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.util.usecase.CompletableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class TriggerUpdateCheck extends CompletableUseCase<TriggerUpdateCheck.RequestModel> {

    private SprinklerRepository sprinklerRepository;

    public TriggerUpdateCheck(SprinklerRepository sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    @NotNull
    @Override
    public Completable execute(final RequestModel requestModel) {
        return sprinklerRepository
                .triggerUpdateCheck()
                .andThen(Observable
                        .intervalRange(0, 20, 0, 1, TimeUnit.SECONDS)
                        .flatMapSingle(step -> sprinklerRepository.update(true))
                        .filter(update -> update.status != Update.Status.CHECKING)
                        .firstElement())
                .ignoreElement();
    }

    public static class RequestModel {
    }
}
