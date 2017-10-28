package com.rainmachine.data.remote.google.response;

import java.util.List;

public class ElevationResponse {
    public List<ElevationSubResponse> results;
    public String status;

    public boolean isValid() {
        return "OK".equals(status) && results != null && results.size() > 0;
    }

    public double getElevation() {
        if (isValid()) {
            return results.get(0).elevation;
        }
        return 0.0;
    }
}
