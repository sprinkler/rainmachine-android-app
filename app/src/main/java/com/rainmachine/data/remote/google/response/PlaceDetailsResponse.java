package com.rainmachine.data.remote.google.response;

public class PlaceDetailsResponse {
    public String status;
    public PlaceDetailsResult result;

    public boolean isValid() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }
}
