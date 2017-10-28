package com.rainmachine.data.remote.sprinkler.v3.response;

import com.google.gson.annotations.SerializedName;

public class DayResponse3 {
    public long id;
    public int day;
    public int maxt;
    public int mint;
    public String units;
    public String icon;
    public double percentage;
    @SerializedName("lastupdate")
    public String lastUpdate;
//    public String location;

    // 3.59+
//    public int wd;
    @SerializedName("waterflag")
    public int waterFlag;
}
