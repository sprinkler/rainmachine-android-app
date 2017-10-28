package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.ComponentsResponse;
import com.rainmachine.data.remote.google.response.GeoCodingAddressResponse;
import com.rainmachine.data.remote.google.response.GeoCodingResponse;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.LocationInfo;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class GeoCodingResponseMapper implements Function<GeoCodingResponse, LocationInfo> {

    private static volatile GeoCodingResponseMapper instance;

    private GeoCodingResponseMapper() {
    }

    public static GeoCodingResponseMapper instance() {
        if (instance == null) {
            instance = new GeoCodingResponseMapper();
        }
        return instance;
    }

    @Override
    public LocationInfo apply(@NonNull GeoCodingResponse response) throws Exception {
        if (!"OK".equals(response.status) || response.results == null || response.results.size()
                == 0) {
            throw new ApiMapperException();
        }
        return getGeoCodingLocation(response.results.get(0));
    }

    private LocationInfo getGeoCodingLocation(GeoCodingAddressResponse locationResponse) {
        if (locationResponse.address_components == null) {
            throw new ApiMapperException();
        }
        String country = null;
        for (ComponentsResponse comp : locationResponse.address_components) {
            if (comp.types == null) {
                continue;
            }
            if (comp.types.contains("country")) {
                country = comp.short_name;
            }
        }
        LocationInfo geoLocation = new LocationInfo();
        try {
            geoLocation.latitude = Double.parseDouble(locationResponse.geometry.location.lat);
            geoLocation.longitude = Double.parseDouble(locationResponse.geometry.location.lng);
            geoLocation.fullAddress = locationResponse.formatted_address;
            geoLocation.country = country;
            geoLocation.isCompleteInfo = true;
            return geoLocation;
        } catch (NumberFormatException nfe) {
            Timber.w(nfe, nfe.getMessage());
            throw new ApiMapperException();
        }
    }
}
