package com.rainmachine.data.remote.sprinkler.v3.response;

public class BaseResponse3 {
    public int statusCode;
    public int error;
    public String message;
    public String status;

    public static final int SC_SUCCESS = 0;
    public static final int SC_SESSION_EXPIRED = 2;
    public static final int SC_MODIFIED_SESSION = 3;
}
