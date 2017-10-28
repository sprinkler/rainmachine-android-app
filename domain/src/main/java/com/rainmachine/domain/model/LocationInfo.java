package com.rainmachine.domain.model;

public class LocationInfo {
    public double latitude;
    public double longitude;
    public String fullAddress;
    public String country;
    public boolean isCompleteInfo;

    public boolean isFromNorthAmerica() {
        return "US".equalsIgnoreCase(country) || "MX".equalsIgnoreCase(country) || "CA"
                .equalsIgnoreCase(country);
    }

    @Override
    public String toString() {
        return fullAddress;
    }
}
