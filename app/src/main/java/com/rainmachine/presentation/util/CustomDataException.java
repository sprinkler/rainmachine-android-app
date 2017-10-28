package com.rainmachine.presentation.util;

import com.rainmachine.data.util.DataException;

public class CustomDataException extends DataException {

    public CustomStatus customStatus;

    public CustomDataException(CustomStatus customStatus) {
        super(Status.CUSTOM_ERROR);
        this.customStatus = customStatus;
    }

    public enum CustomStatus {
        INVALID_WUNDERGROUND_API_KEY, INVALID_NETATMO_CREDENTIALS,
        EMAIL_CONFIRMATION_ERROR, RESET_CLOUD_CERTIFICATES_ERROR, SEND_DIAGNOSTICS_ERROR,
        DISABLE_CLOUD_EMAIL_ERROR, ENABLE_CLOUD_EMAIL_ERROR, REBOOT_ERROR, PARSER_ERROR,
        REFRESH_NETATMO_ERROR, RUN_PARSER_ERROR, ADD_WEATHER_SOURCE_ERROR
    }
}
