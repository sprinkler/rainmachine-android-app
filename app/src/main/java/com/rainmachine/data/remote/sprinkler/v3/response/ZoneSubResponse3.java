package com.rainmachine.data.remote.sprinkler.v3.response;

public class ZoneSubResponse3 {

    public static final String STATE_WATERING = "Watering";
    public static final String STATE_PENDING = "Pending";
    public static final String STATE_IDLE = "";

    public long id;
    public String name;
    public String type;
    public String state;
    public int counter;
}
