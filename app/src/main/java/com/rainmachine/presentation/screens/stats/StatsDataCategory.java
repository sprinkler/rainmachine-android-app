package com.rainmachine.presentation.screens.stats;

import org.joda.time.LocalDate;

import java.util.Map;

public class StatsDataCategory {
    public Map<LocalDate, StatsDayViewModel> days;
    public ScaleViewModel scaleViewModel;
    public LocalDate startDate;
    public int numDays;
    public float waterSavedPercentage; // [0, 1]
    public float waterSavedAmount; // gallons or liters
}
