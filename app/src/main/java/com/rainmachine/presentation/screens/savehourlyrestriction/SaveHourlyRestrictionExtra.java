package com.rainmachine.presentation.screens.savehourlyrestriction;

import com.rainmachine.domain.model.HourlyRestriction;

import org.parceler.Parcel;

@Parcel
public class SaveHourlyRestrictionExtra {
    public HourlyRestriction restriction;
    public boolean use24HourFormat;

    public SaveHourlyRestrictionExtra() {
    }
}
