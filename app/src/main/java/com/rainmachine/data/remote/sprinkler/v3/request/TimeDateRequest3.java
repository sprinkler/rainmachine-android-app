package com.rainmachine.data.remote.sprinkler.v3.request;

import com.google.gson.annotations.SerializedName;

public class TimeDateRequest3 {
    public String appDate;
    @SerializedName("time_format")
    public int timeFormat;
}
