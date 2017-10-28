package com.rainmachine.presentation.util;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RunOnProperThreadsCompletable implements CompletableTransformer {

    private static final RunOnProperThreadsCompletable INSTANCE = new
            RunOnProperThreadsCompletable();

    public static RunOnProperThreadsCompletable instance() {
        return INSTANCE;
    }

    private RunOnProperThreadsCompletable() {
    }

    @Override
    public CompletableSource apply(@NonNull Completable upstream) {
        return upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
