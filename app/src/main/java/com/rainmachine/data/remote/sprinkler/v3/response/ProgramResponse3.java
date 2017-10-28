package com.rainmachine.data.remote.sprinkler.v3.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProgramResponse3 {

    private static final int FREQUENCY_DAILY = 0;
    private static final int FREQUENCY_WEEKDAYS = 2;
    private static final int FREQUENCY_ODD_DAYS = 4;
    private static final int FREQUENCY_EVEN_DAYS = 5;
    private static final int FREQUENCY_N_DAYS_OFFSET = 4;
    private static final int FREQUENCY_N_DAYS_START = 6;
    private static final int FREQUENCY_N_DAYS_END = 18;

    public long id;
    @SerializedName("time_format")
    public long timeFormat;
    public String name;
    public int delay; // in minutes
    @SerializedName("delay_on")
    public int delayOn;
    @SerializedName("cs_on")
    public int csOn;
    public int cycles;
    public int soak; // in minutes
    public String state;
    public int frequency;
    public int parameter;
    public String startTime;
    public String weekdays;
    public int active;
    public List<WateringTimesResponse3> wateringTimes;

    // This is received only on 3.57+
    public int ignoreWeatherData;

    public boolean isDaily() {
        return frequency == FREQUENCY_DAILY;
    }

    public boolean isOddDays() {
        return frequency == FREQUENCY_ODD_DAYS;
    }

    public boolean isEvenDays() {
        return frequency == FREQUENCY_EVEN_DAYS;
    }

    public boolean isWeekDays() {
        return frequency == FREQUENCY_WEEKDAYS;
    }

    public boolean isEveryNDays() {
        return (frequency >= FREQUENCY_N_DAYS_START && frequency <= FREQUENCY_N_DAYS_END);
    }

    public int frequencyNumDays() {
        if (frequency >= FREQUENCY_N_DAYS_START) {
            return frequency - FREQUENCY_N_DAYS_OFFSET;
        } else {
            return 2; // default
        }
    }
}
