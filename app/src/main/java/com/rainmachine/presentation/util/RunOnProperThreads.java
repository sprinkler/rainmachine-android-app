package com.rainmachine.presentation.util;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunOnProperThreads<T> implements ObservableTransformer<T, T> {

    private static final RunOnProperThreads<Object> INSTANCE = new RunOnProperThreads<>();

    @SuppressWarnings("unchecked")
    public static <T> RunOnProperThreads<T> instance() {
        return (RunOnProperThreads<T>) INSTANCE;
    }

    private RunOnProperThreads() {
    }

    @Override
    public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
