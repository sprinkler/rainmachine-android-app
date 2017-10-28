package com.rainmachine.domain.util;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunToCompletionCompletable<T> implements CompletableTransformer {

    private static final RunToCompletionCompletable<Object> INSTANCE = new
            RunToCompletionCompletable<>();

    @SuppressWarnings("unchecked")
    public static <T> RunToCompletionCompletable<T> instance() {
        return (RunToCompletionCompletable<T>) INSTANCE;
    }

    private RunToCompletionCompletable() {
    }

    @Override
    public CompletableSource apply(@NonNull Completable upstream) {
        Observable<T> stream = upstream.<T>toObservable().share();
        stream.onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return stream.ignoreElements();
    }
}
