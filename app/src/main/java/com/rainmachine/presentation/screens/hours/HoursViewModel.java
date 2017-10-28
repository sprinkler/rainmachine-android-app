package com.rainmachine.presentation.screens.hours;

import com.rainmachine.domain.model.HourlyRestriction;

import org.parceler.Parcel;

import java.util.List;

@Parcel
class HoursViewModel {
    public List<HourlyRestriction> hourlyRestrictions;
    public boolean use24HourFormat;

    public HoursViewModel() {
    }
}
