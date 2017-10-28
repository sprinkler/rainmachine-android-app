package com.rainmachine.presentation.screens.stats;

import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.domain.model.DayStats;
import com.rainmachine.domain.model.Program;

import org.joda.time.LocalDateTime;

import java.util.List;

public class StatsViewModel {
    public List<DayStats> stats;
    public StatsDataCategory weekCategory;
    public StatsDataCategory monthCategory;
    public StatsDataCategory yearCategory;
    public List<Program> programs;
    public List<Long> simulationExpiredProgramIds;
    public boolean isUnitsMetric;
    public boolean use24HourFormat;
    public LocalDateTime sprinklerLocalDateTime;

    public boolean showVitals;
    public String nextWateringCycle;
    public String lastWeatherUpdate; // pretty format

    public DashboardGraphs dashboardGraphs;
    boolean showDailyWaterNeedChart;
}
