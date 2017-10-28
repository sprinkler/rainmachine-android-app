package com.rainmachine.domain.model;

import org.joda.time.LocalDate;

public class DayStats {
    public long id;
    public LocalDate date;
    public int maxTemp;
    public int minTemp;
    // 1 - watering restrictions, 2 - rain delaySeconds, 3 - freeze control, 4 - water surplus
    public int wateringFlag;
    public int weatherImageId;

    // Only for old API 3
    public double percentage;
    public String lastUpdate;
    public String iconName;
}
