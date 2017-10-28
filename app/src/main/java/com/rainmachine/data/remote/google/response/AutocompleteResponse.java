package com.rainmachine.data.remote.google.response;

import java.util.List;

public class AutocompleteResponse {
    public String status;
    public List<PredictionResponse> predictions;

    public boolean isValid() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }
}
