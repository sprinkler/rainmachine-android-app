package com.rainmachine.data.remote.google.mapper;

import com.rainmachine.data.remote.google.response.AutocompleteResponse;
import com.rainmachine.data.remote.google.response.PredictionResponse;
import com.rainmachine.data.remote.util.ApiMapperException;
import com.rainmachine.domain.model.Autocomplete;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class AutocompleteResponseMapper implements Function<AutocompleteResponse,
        List<Autocomplete>> {

    private static volatile AutocompleteResponseMapper instance;

    private AutocompleteResponseMapper() {
    }

    public static AutocompleteResponseMapper instance() {
        if (instance == null) {
            instance = new AutocompleteResponseMapper();
        }
        return instance;
    }

    @Override
    public List<Autocomplete> apply(@NonNull AutocompleteResponse response) throws Exception {
        if (!"OK".equals(response.status) && !"ZERO_RESULTS".equals(response.status)) {
            throw new ApiMapperException();
        }
        return convertAutocomplete(response);
    }

    private List<Autocomplete> convertAutocomplete(AutocompleteResponse autocompleteResponse) {
        List<Autocomplete> list = new ArrayList<>(autocompleteResponse.predictions.size());
        Autocomplete autocomplete;
        for (PredictionResponse predictionResponse : autocompleteResponse.predictions) {
            autocomplete = new Autocomplete();
            autocomplete.description = predictionResponse.description;
            autocomplete.placeId = predictionResponse.place_id;
            list.add(autocomplete);
        }
        return list;
    }
}