package com.rainmachine.presentation.screens.programs;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.parcel.LocalDateTimeParcelConverter;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.ParcelPropertyConverter;

@Parcel
public class MoreProgramActionsExtra {
    Program program;
    @ParcelPropertyConverter(LocalDateTimeParcelConverter.class)
    LocalDateTime sprinklerLocalDateTime;
    boolean isUnitsMetric;
    boolean use24HourFormat;

    @ParcelConstructor
    MoreProgramActionsExtra(Program program, LocalDateTime sprinklerLocalDateTime, boolean
            isUnitsMetric, boolean use24HourFormat) {
        this.program = program;
        this.sprinklerLocalDateTime = sprinklerLocalDateTime;
        this.isUnitsMetric = isUnitsMetric;
        this.use24HourFormat = use24HourFormat;
    }
}
