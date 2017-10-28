package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.TimezoneResponse;
import com.rainmachine.data.remote.util.ApiMapperException;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;


public class TimezoneResponseMapper implements Function<TimezoneResponse, String> {

    private static volatile TimezoneResponseMapper instance;

    public static TimezoneResponseMapper instance() {
        if (instance == null) {
            instance = new TimezoneResponseMapper();
        }
        return instance;
    }

    @Override
    public String apply(@NonNull TimezoneResponse response) throws Exception {
        if (!"OK".equals(response.status)) {
            throw new ApiMapperException();
        }
        return response.timeZoneId;
    }
}