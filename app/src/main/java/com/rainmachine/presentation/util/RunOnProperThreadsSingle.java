package com.rainmachine.presentation.util;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunOnProperThreadsSingle<T> implements SingleTransformer<T, T> {

    private static final RunOnProperThreadsSingle<Object> INSTANCE = new
            RunOnProperThreadsSingle<>();

    @SuppressWarnings("unchecked")
    public static <T> RunOnProperThreadsSingle<T> instance() {
        return (RunOnProperThreadsSingle<T>) INSTANCE;
    }

    private RunOnProperThreadsSingle() {
    }

    @Override
    public SingleSource<T> apply(@NonNull Single<T> upstream) {
        return upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
