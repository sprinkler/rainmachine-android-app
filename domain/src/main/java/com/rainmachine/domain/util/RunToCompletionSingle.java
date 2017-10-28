package com.rainmachine.domain.util;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunToCompletionSingle<T> implements SingleTransformer<T, T> {

    private static final RunToCompletionSingle<Object> INSTANCE = new RunToCompletionSingle<>();

    @SuppressWarnings("unchecked")
    public static <T> RunToCompletionSingle<T> instance() {
        return (RunToCompletionSingle<T>) INSTANCE;
    }

    private RunToCompletionSingle() {
    }

    @Override
    public SingleSource<T> apply(@NonNull Single<T> upstream) {
        Observable<T> stream = upstream.<T>toObservable().share();
        stream.onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return stream.singleOrError();
    }
}
