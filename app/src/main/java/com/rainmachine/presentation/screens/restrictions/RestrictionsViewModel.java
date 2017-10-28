package com.rainmachine.presentation.screens.restrictions;

import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;

import java.util.List;

class RestrictionsViewModel {
    public GlobalRestrictions globalRestrictions;
    public List<HourlyRestriction> hourlyRestrictions;
    public boolean isUnitsMetric;
    public boolean use24HourFormat;
    public int minWateringDurationThreshold;
    public int maxWateringCoefficient; // percent
}
