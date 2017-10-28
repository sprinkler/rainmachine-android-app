package com.rainmachine.data.remote.google.response;

public class TimezoneResponse {
    public double dstOffset;
    public double rawOffset;
    public String status;
    public String timeZoneId;
    public String timeZoneName;

    public boolean isValid() {
        return "OK".equals(status);
    }
}
