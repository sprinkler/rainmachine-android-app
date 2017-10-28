package com.rainmachine.domain.model;

public class LocationDetails {
    public double latitude;
    public double longitude;
    public String country;
    public String administrativeArea;

    public boolean isInNorthAmerica() {
        return "US".equalsIgnoreCase(country) || "MX".equalsIgnoreCase(country) || "CA"
                .equalsIgnoreCase(country);
    }

    public boolean isInCalifornia() {
        return "CA".equalsIgnoreCase(administrativeArea);
    }

    public boolean isInFlorida() {
        return "FL".equalsIgnoreCase(administrativeArea);
    }
}
