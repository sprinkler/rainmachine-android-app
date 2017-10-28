package com.rainmachine.data.remote.google;

import com.rainmachine.data.remote.google.mapper.AutocompleteResponseMapper;
import com.rainmachine.data.remote.google.mapper.ElevationResponseMapper;
import com.rainmachine.data.remote.google.mapper.GeoCodingResponseMapper;
import com.rainmachine.data.remote.google.mapper.PlaceDetailsResponseMapper;
import com.rainmachine.data.remote.google.mapper.ReverseGeoCodingResponseMapper;
import com.rainmachine.data.remote.google.mapper.TimezoneResponseMapper;
import com.rainmachine.data.remote.util.RemoteErrorTransformer;
import com.rainmachine.data.remote.util.RemoteRetry;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.domain.model.LocationDetails;
import com.rainmachine.domain.model.LocationInfo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;

public class GoogleApiDelegate {

    private GoogleApi googleApi;
    private RemoteRetry remoteRetry;
    private RemoteErrorTransformer remoteErrorTransformer;

    public GoogleApiDelegate(GoogleApi googleApi) {
        this.googleApi = googleApi;
        remoteRetry = new RemoteRetry();
        remoteErrorTransformer = new RemoteErrorTransformer();
    }

    public Single<LocationDetails> detailsBasedOnLocation(double latitude, double longitude) {
        return googleApi
                .reverseGeocode(latitude + "," + longitude)
                .retry(remoteRetry)
                .map(ReverseGeoCodingResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<List<Autocomplete>> autocomplete(final CharSequence text) {
        return googleApi
                .autocomplete(text.toString())
                .retry(remoteRetry)
                .map(AutocompleteResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<LocationInfo> placeDetails(String placeId) {
        return googleApi
                .placeDetails(placeId)
                .retry(remoteRetry)
                .map(PlaceDetailsResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<LocationInfo> reverseGeocode(final double latitude, final double
            longitude) {
        return googleApi
                .reverseGeocode(latitude + "," + longitude)
                .retry(remoteRetry)
                .map(GeoCodingResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<String> timezone(double latitude, double longitude) {
        String sLocation = latitude + "," + longitude;
        long timestamp = DateTime.now().getMillis() / DateTimeConstants.MILLIS_PER_SECOND;
        return googleApi
                .timezone(sLocation, timestamp)
                .retry(remoteRetry)
                .map(TimezoneResponseMapper.instance())
                .compose(dealWithError());
    }

    public Single<Double> elevation(double latitude, double longitude) {
        String sLocation = latitude + "," + longitude;
        return googleApi
                .elevation(sLocation)
                .retry(remoteRetry)
                .map(ElevationResponseMapper.instance())
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) remoteErrorTransformer;
    }
}
