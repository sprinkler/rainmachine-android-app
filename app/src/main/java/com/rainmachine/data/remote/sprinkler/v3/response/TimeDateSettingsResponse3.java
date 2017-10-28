package com.rainmachine.data.remote.sprinkler.v3.response;

import com.google.gson.annotations.SerializedName;

public class TimeDateSettingsResponse3 {
    public String appDate;
    @SerializedName("am_pm")
    public int amPm;
    @SerializedName("time_format")
    public int timeFormat;
}
