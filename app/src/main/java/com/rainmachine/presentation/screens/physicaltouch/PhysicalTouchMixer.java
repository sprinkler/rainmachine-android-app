package com.rainmachine.presentation.screens.physicaltouch;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.usecases.auth.LogInDefault;
import com.rainmachine.domain.util.Irrelevant;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class PhysicalTouchMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private LogInDefault logInDefault;

    PhysicalTouchMixer(SprinklerRepositoryImpl sprinklerRepository, LogInDefault logInDefault) {
        this.sprinklerRepository = sprinklerRepository;
        this.logInDefault = logInDefault;
    }

    Observable<Irrelevant> tryToConnect() {
        return Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .flatMap(step -> logInDefault
                        .execute(new LogInDefault.RequestModel())
                        .map(responseModel -> responseModel.success))
                .flatMap(success -> {
                    if (success) {
                        return Observable.just(Irrelevant.INSTANCE);
                    } else {
                        return Observable.empty();
                    }
                })
                .first(Irrelevant.INSTANCE)
                .toObservable()
                .doOnSubscribe(disposable -> {
                    Timber.d("enable leds");
                    enableLightLEDs(true);
                })
                .doOnDispose(() -> {
                    Timber.d("disable leds");
                    enableLightLEDs(false);
                })
                .unsubscribeOn(Schedulers.io());
    }

    private void enableLightLEDs(boolean enable) {
        sprinklerRepository
                .enableLightLEDs(enable)
                .toObservable()
                .map(irrelevant -> true)
                .onErrorResumeNext(Observable.empty())
                .blockingFirst(false);
    }
}
