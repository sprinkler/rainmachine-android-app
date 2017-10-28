package com.rainmachine.data.util;

public class DataException extends RuntimeException {

    public Status status;
    public String sprinklerErrorMessage;

    public DataException(Status status) {
        this.status = status;
    }

    public DataException(Status status, Throwable throwable, String sprinklerErrorMessage) {
        super(throwable);
        this.status = status;
        this.sprinklerErrorMessage = sprinklerErrorMessage;
    }

    public DataException(Status status, Throwable throwable) {
        super(throwable);
        this.status = status;
    }

    public enum Status {
        UNKNOWN, AUTHENTICATION_ERROR, SPRINKLER_ERROR, API_MAPPER_ERROR,
        NETWORK_ERROR, CUSTOM_ERROR, HTTP_GENERIC_ERROR
    }
}
