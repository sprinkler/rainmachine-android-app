package com.rainmachine.presentation.screens.dashboardgraphs;

import com.rainmachine.data.local.database.model.DashboardGraphs;

import java.util.List;

class DashboardGraphsViewModel {
    public DashboardGraphs dashboardGraphs;
    public List<DashboardGraphs.DashboardGraph> generalGraphs;
    public List<DashboardGraphs.DashboardGraph> programGraphs;
}
