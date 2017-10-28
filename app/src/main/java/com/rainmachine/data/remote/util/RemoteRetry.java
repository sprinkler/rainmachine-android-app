package com.rainmachine.data.remote.util;

import java.io.IOException;

import io.reactivex.functions.BiPredicate;
import retrofit2.HttpException;

public class RemoteRetry implements BiPredicate<Integer, Throwable> {

    @Override
    public boolean test(Integer attempts, Throwable throwable) {
        if (throwable instanceof HttpException || throwable instanceof IOException) {
            return attempts <= 2;
        }
        return false;
    }
}
