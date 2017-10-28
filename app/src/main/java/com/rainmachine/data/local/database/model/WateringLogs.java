package com.rainmachine.data.local.database.model;

import com.rainmachine.domain.model.WateringLogDetailsDay;

import org.joda.time.LocalDate;

import java.util.Map;

public class WateringLogs {

    public Long _id;
    public String deviceId;
    public LocalDate startDate;
    public LocalDate endDate;
    public Map<LocalDate, WateringLogDetailsDay> days;

    public WateringLogs() {
    }
}
