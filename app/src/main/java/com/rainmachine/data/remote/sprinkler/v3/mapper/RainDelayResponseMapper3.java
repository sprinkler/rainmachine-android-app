package com.rainmachine.data.remote.sprinkler.v3.mapper;

import com.rainmachine.data.remote.sprinkler.v3.response.RainDelayResponse3;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class RainDelayResponseMapper3 implements Function<RainDelayResponse3, Long> {

    private static volatile RainDelayResponseMapper3 instance;

    public static RainDelayResponseMapper3 instance() {
        if (instance == null) {
            instance = new RainDelayResponseMapper3();
        }
        return instance;
    }

    @Override
    public Long apply(@NonNull RainDelayResponse3 response3) throws Exception {
        return response3.settings.delayCounter;
    }
}
