package com.rainmachine.data.remote.util;

import com.rainmachine.domain.util.Irrelevant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class SprinklerStatsRemoteRetry implements Function<Observable<Throwable>,
        ObservableSource<?>> {

    private static final int HTTP_STATUS_SERVICE_UNAVAILABLE = 503;

    @Override
    public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable) throws
            Exception {
        return throwableObservable
                .zipWith(Observable.range(1, 5), (throwable, integer) -> throwable)
                .flatMap(throwable -> {
                    if (throwable instanceof IOException || throwable instanceof
                            HttpException) {
                        if (throwable instanceof HttpException) {
                            HttpException httpException = (HttpException) throwable;
                            Response response = httpException.response();
                            // For status service unavailable, we retry
                            if (response != null && response.code() ==
                                    HTTP_STATUS_SERVICE_UNAVAILABLE) {
                                Timber.d("Retry because of status service unavailable");
                                return Observable
                                        .just(Irrelevant.INSTANCE)
                                        .delay(10, TimeUnit.SECONDS);
                            }
                        }
                        // If not status service unavailable, retry a bit faster
                        return Observable
                                .just(Irrelevant.INSTANCE)
                                .delay(5, TimeUnit.SECONDS);
                    }
                    // For anything else, don't retry
                    return Observable.error(throwable);
                });
    }
}
