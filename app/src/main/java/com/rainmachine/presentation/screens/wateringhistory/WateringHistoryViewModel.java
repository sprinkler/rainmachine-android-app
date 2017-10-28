package com.rainmachine.presentation.screens.wateringhistory;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;

class WateringHistoryViewModel {
    // The days are ordered from today backwards in time. This is important because this is being
    // used when filtering the content in the UI
    List<Day> days;
    boolean use24HourFormat;
    boolean isUnitsMetric;

    static class Day {
        LocalDate date;
        List<Program> programs;
    }

    static class Program {
        long id;
        String name;
        List<Zone> zones;
    }

    static class Zone {
        long id;
        String name;
        int totalScheduled;
        int totalWatered;
        float waterSavedAmount;
        SpecialCondition specialCondition;
        List<Cycle> cycles;
    }

    static class Cycle {
        int scheduled;
        int watered;
        float waterUsedAmount;
        float waterScheduledAmount;
        LocalTime startTime;

        LocalTime getEndTime() {
            return startTime.plusSeconds(watered);
        }
    }

    enum SpecialCondition {
        NONE, STOPPED_BY_USER, MINIMUM_WATERING_TIME, FREEZE_PROTECT,
        DAY_RESTRICTION, WATERING_REACHES_NEXT_DAY, WATER_SURPLUS, RAIN_DETECTED,
        RAIN_SENSOR_RESTRICTION, MONTH_RESTRICTION, RAIN_DELAY, PROGRAM_RAIN_RESTRICTION
    }
}
