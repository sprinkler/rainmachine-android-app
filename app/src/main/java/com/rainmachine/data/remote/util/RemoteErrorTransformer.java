package com.rainmachine.data.remote.util;

import com.rainmachine.data.util.DataException;

import java.io.IOException;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import retrofit2.HttpException;

public class RemoteErrorTransformer implements SingleTransformer<Object, Object> {

    @Override
    public SingleSource<Object> apply(@NonNull Single<Object> upstream) {
        return upstream.onErrorReturn(wrapError);
    }

    private final Function<Throwable, Object> wrapError = throwable -> {
        if (throwable instanceof IOException) {
            throw new DataException(DataException.Status.NETWORK_ERROR, throwable);
        } else if (throwable instanceof HttpException) {
            throw new DataException(DataException.Status.HTTP_GENERIC_ERROR, throwable);
        } else if (throwable instanceof ApiMapperException) {
            throw new DataException(DataException.Status.API_MAPPER_ERROR, throwable);
        }
        throw new DataException(DataException.Status.UNKNOWN, throwable);
    };
}
