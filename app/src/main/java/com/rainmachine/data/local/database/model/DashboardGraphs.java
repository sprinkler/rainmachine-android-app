package com.rainmachine.data.local.database.model;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class DashboardGraphs {
    public Long _id;
    public String deviceId;
    public List<DashboardGraph> graphs;

    @Parcel
    public static class DashboardGraph {
        public GraphType graphType;
        public boolean isEnabled;
        public long programId; // valid only if graphType is PROGRAM
        public String programName; // valid only if graphType is PROGRAM

        public DashboardGraph() {
        }

        public DashboardGraph(GraphType graphType, boolean isEnabled) {
            this.graphType = graphType;
            this.isEnabled = isEnabled;
        }

        public DashboardGraph(GraphType graphType, boolean isEnabled, long programId, String
                programName) {
            this(graphType, isEnabled);
            this.programId = programId;
            this.programName = programName;
        }
    }

    public enum GraphType {WEATHER, TEMPERATURE, RAIN_AMOUNT, DAILY_WATER_NEED, PROGRAM}
}
