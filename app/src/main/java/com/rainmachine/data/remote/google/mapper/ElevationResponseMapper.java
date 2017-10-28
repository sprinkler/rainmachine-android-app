package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.ElevationResponse;
import com.rainmachine.data.remote.util.ApiMapperException;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class ElevationResponseMapper implements Function<ElevationResponse, Double> {

    private static volatile ElevationResponseMapper instance;

    public static ElevationResponseMapper instance() {
        if (instance == null) {
            instance = new ElevationResponseMapper();
        }
        return instance;
    }

    @Override
    public Double apply(@NonNull ElevationResponse response) throws Exception {
        if (!"OK".equals(response.status) || response.results == null || response.results.size()
                == 0) {
            throw new ApiMapperException();
        }
        return response.results.get(0).elevation;
    }
}