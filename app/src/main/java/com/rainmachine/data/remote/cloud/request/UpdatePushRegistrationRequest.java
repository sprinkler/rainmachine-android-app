package com.rainmachine.data.remote.cloud.request;

public class UpdatePushRegistrationRequest {
    public String token;
    public String phoneId;
    public int timezone;
    public String os; // ios/android
    public int send_notifications;
    public int isUnitsMetric;
    public int use24HourFormat;
}