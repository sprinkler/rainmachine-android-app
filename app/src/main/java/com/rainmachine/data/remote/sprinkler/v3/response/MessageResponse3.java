package com.rainmachine.data.remote.sprinkler.v3.response;

public class MessageResponse3 {
    public String message;
    public String status;
    public String state;

    public static final String MESSAGE_STATUS_OK = "ok";
    public static final String MESSAGE_STATUS_LOGGED_OUT = "OUT";
    public static final String MESSAGE_STATUS_ERROR = "err";
}
