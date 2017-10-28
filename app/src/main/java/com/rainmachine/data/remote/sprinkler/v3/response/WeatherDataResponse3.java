package com.rainmachine.data.remote.sprinkler.v3.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherDataResponse3 {
    @SerializedName("HomeScreen")
    public List<DayResponse3> homeScreen;

    /* Error response */
    public String message;
    public String status;
}
