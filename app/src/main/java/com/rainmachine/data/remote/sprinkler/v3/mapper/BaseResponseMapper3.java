package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.BaseResponse3;
import com.rainmachine.domain.util.Irrelevant;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class BaseResponseMapper3 implements Function<BaseResponse3, Irrelevant> {

    private static volatile BaseResponseMapper3 instance;

    public static BaseResponseMapper3 instance() {
        if (instance == null) {
            instance = new BaseResponseMapper3();
        }
        return instance;
    }

    @Override
    public Irrelevant apply(@NonNull BaseResponse3 baseResponse3) throws Exception {
        return Irrelevant.INSTANCE;
    }
}
