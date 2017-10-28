package com.rainmachine.domain.util;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunToCompletion<T> implements ObservableTransformer<T, T> {

    private static final RunToCompletion<Object> INSTANCE = new RunToCompletion<>();

    @SuppressWarnings("unchecked")
    public static <T> RunToCompletion<T> instance() {
        return (RunToCompletion<T>) INSTANCE;
    }

    private RunToCompletion() {
    }

    @Override
    public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        Observable<T> stream = upstream.share();
        stream.onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return stream;
    }
}
