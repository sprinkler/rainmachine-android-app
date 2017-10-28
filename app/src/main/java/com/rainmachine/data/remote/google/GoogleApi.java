package com.rainmachine.data.remote.google;

import com.rainmachine.data.remote.google.response.AutocompleteResponse;
import com.rainmachine.data.remote.google.response.ElevationResponse;
import com.rainmachine.data.remote.google.response.GeoCodingResponse;
import com.rainmachine.data.remote.google.response.PlaceDetailsResponse;
import com.rainmachine.data.remote.google.response.TimezoneResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleApi {

    @GET("geocode/json?sensor=true")
    Single<GeoCodingResponse> reverseGeocode(@Query("latlng") String location);

    @GET("elevation/json?sensor=true")
    Single<ElevationResponse> elevation(@Query("locations") String location);

    @GET("timezone/json?sensor=true")
    Single<TimezoneResponse> timezone(@Query("location") String location,
                                      @Query("timestamp") long timestamp);

    @GET("place/autocomplete/json?key=AIzaSyAdWNW4IlQqaSTFjVeN9Rv1WjyqQu1c_mI")
    Single<AutocompleteResponse> autocomplete(@Query("input") String input);

    @GET("place/details/json?key=AIzaSyAdWNW4IlQqaSTFjVeN9Rv1WjyqQu1c_mI")
    Single<PlaceDetailsResponse> placeDetails(@Query("placeid") String placeId);
}
