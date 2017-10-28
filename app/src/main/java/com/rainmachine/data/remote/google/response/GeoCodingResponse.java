package com.rainmachine.data.remote.google.response;

import java.util.List;

public class GeoCodingResponse {
    public List<GeoCodingAddressResponse> results;
    public String status;

    public boolean isValid() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }
}
