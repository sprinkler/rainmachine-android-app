package com.rainmachine.infrastructure.util;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class RxGlobalErrorHandler implements Consumer<Throwable> {

    @Override
    public void accept(@NonNull Throwable e) throws Exception {
        if (e instanceof UndeliverableException) {
            e = e.getCause();
        }
        if (e instanceof IOException) {
            // fine, irrelevant network problem or API that throws on cancellation
            return;
        }
        if (e instanceof InterruptedException) {
            // fine, some blocking code was interrupted by a dispose call
            return;
        }
        if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
            // that's likely a bug in the application
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread
                    .currentThread(), e);
            return;
        }
        if (e instanceof IllegalStateException) {
            // that's a bug in RxJava or in a custom operator
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread
                    .currentThread(), e);
            return;
        }
        Timber.w(e, "Undeliverable exception received, not sure what to do");
    }
}
