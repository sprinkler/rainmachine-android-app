package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.ComponentsResponse;
import com.rainmachine.data.remote.google.response.PlaceDetailsResponse;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.LocationInfo;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class PlaceDetailsResponseMapper implements Function<PlaceDetailsResponse, LocationInfo> {

    private static volatile PlaceDetailsResponseMapper instance;

    private PlaceDetailsResponseMapper() {
    }

    public static PlaceDetailsResponseMapper instance() {
        if (instance == null) {
            instance = new PlaceDetailsResponseMapper();
        }
        return instance;
    }

    @Override
    public LocationInfo apply(@NonNull PlaceDetailsResponse response) throws Exception {
        if (!"OK".equals(response.status) || response.result == null) {
            throw new ApiMapperException();
        }
        return convertPlaceDetails(response);
    }

    private LocationInfo convertPlaceDetails(PlaceDetailsResponse response) {
        if (response.result.address_components == null) {
            throw new ApiMapperException();
        }
        String country = null;
        for (ComponentsResponse comp : response.result.address_components) {
            if (comp.types == null) {
                continue;
            }
            if (comp.types.contains("country")) {
                country = comp.short_name;
            }
        }
        LocationInfo geoLocation = new LocationInfo();
        try {
            geoLocation.latitude = Double.parseDouble(response.result.geometry.location.lat);
            geoLocation.longitude = Double.parseDouble(response.result.geometry.location.lng);
            geoLocation.fullAddress = response.result.formatted_address;
            geoLocation.country = country;
            geoLocation.isCompleteInfo = true;
            return geoLocation;
        } catch (NumberFormatException nfe) {
            Timber.w(nfe, nfe.getMessage());
            throw new ApiMapperException();
        }
    }
}
