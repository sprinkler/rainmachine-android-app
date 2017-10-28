package com.rainmachine.data.remote.sprinkler.v3.request;

import com.google.gson.annotations.SerializedName;
import com.rainmachine.data.remote.sprinkler.v3.response.WateringTimesResponse3;

import java.util.List;

public class ProgramRequest3 {
    public long id;
    @SerializedName("time_format")
    public long timeFormat;
    public int delay;
    @SerializedName("delay_on")
    public int delayOn;
    @SerializedName("cs_on")
    public int csOn;
    public int cycles;
    public int soak;
    public String state;
    public int frequency;
    public long startTime;
    public String weekdays;
    public int active;
    public String programStartTime;
    public List<WateringTimesResponse3> wateringTimes;
    public String name;
    // This is used only on 3.57+
    public int ignoreWeatherData;

    public ProgramRequest3() {
    }
}
