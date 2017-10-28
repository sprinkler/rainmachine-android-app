package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.ComponentsResponse;
import com.rainmachine.data.remote.google.response.GeoCodingAddressResponse;
import com.rainmachine.data.remote.google.response.GeoCodingResponse;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.LocationDetails;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class ReverseGeoCodingResponseMapper implements Function<GeoCodingResponse,
        LocationDetails> {

    private static volatile ReverseGeoCodingResponseMapper instance;

    private ReverseGeoCodingResponseMapper() {
    }

    public static ReverseGeoCodingResponseMapper instance() {
        if (instance == null) {
            instance = new ReverseGeoCodingResponseMapper();
        }
        return instance;
    }

    @Override
    public LocationDetails apply(@NonNull GeoCodingResponse response) throws Exception {
        if (!"OK".equals(response.status) || response.results == null || response.results.size()
                == 0) {
            throw new ApiMapperException();
        }
        GeoCodingAddressResponse locationResponse = response.results.get(0);
        if (locationResponse.address_components == null) {
            throw new ApiMapperException();
        }
        String country = null;
        String administrativeArea = null;
        for (ComponentsResponse comp : locationResponse.address_components) {
            if (comp.types == null) {
                continue;
            }
            if (comp.types.contains("country")) {
                country = comp.short_name;
            }
            if (comp.types.contains("administrative_area_level_1")) {
                administrativeArea = comp.short_name;
            }
        }
        LocationDetails locationDetails = new LocationDetails();
        try {
            locationDetails.latitude = Double.parseDouble(locationResponse.geometry.location.lat);
            locationDetails.longitude = Double.parseDouble(locationResponse.geometry.location.lng);
            locationDetails.country = country;
            locationDetails.administrativeArea = administrativeArea;
            return locationDetails;
        } catch (NumberFormatException nfe) {
            Timber.w(nfe, nfe.getMessage());
            throw new ApiMapperException();
        }
    }
}
