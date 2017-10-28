package com.rainmachine.data.remote.sprinkler.v4.mapper;

import com.rainmachine.data.remote.sprinkler.v4.response.RainDelayResponse;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class RainDelayResponseMapper implements Function<RainDelayResponse, Long> {

    private static volatile RainDelayResponseMapper instance;

    public static RainDelayResponseMapper instance() {
        if (instance == null) {
            instance = new RainDelayResponseMapper();
        }
        return instance;
    }

    @Override
    public Long apply(@NonNull RainDelayResponse response) throws Exception {
        return response.delayCounter;
    }
}