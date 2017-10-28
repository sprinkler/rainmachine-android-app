package com.rainmachine.data.remote.sprinkler.v3.response;

public class ZonePropertiesResponse3 {
    public long id;
    public int masterValve;
    public int before; // in minutes
    public int after; // in minutes
    public int active;
    public String name;
    public int vegetation;
    public int forecastData;
    public int historicalAverage;
}
