package com.rainmachine.presentation.screens.statsdetails;

import org.parceler.Parcel;

@Parcel
public class StatsDetailsExtra {
    public static final int TYPE_WEEK = 0;
    public static final int TYPE_MONTH = 1;
    public static final int TYPE_YEAR = 2;

    public static final int CHART_WATER_NEED = 0;
    public static final int CHART_TEMPERATURE = 1;
    public static final int CHART_RAIN_AMOUNT = 2;
    public static final int CHART_PROGRAM = 3;
    public static final int CHART_WEATHER = 4;

    public int type;
    public int chart;
    public int programId; // used only for CHART_PROGRAM
    public String programName; // used only for CHART_PROGRAM
}
