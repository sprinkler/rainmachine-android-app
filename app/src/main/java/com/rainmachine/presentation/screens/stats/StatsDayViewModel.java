package com.rainmachine.presentation.screens.stats;

import android.util.SparseArray;

import org.joda.time.LocalDate;

public class StatsDayViewModel {
    public LocalDate date;
    public float dailyWaterNeed;
    public int maxTemperature;
    public int minTemperature;
    public boolean lessOrEqualToFreezeProtect;
    public float rainAmount;
    public SparseArray<Float> programDailyWaterNeed;
    public int iconId;
}
