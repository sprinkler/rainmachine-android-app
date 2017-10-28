package com.rainmachine.presentation.screens.programdetails;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.parcel.LocalDateTimeParcelConverter;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

@Parcel
public class ProgramDetailsExtra {
    public Program program;
    public Program originalProgram;
    @ParcelPropertyConverter(LocalDateTimeParcelConverter.class)
    public LocalDateTime sprinklerLocalDateTime;
    public boolean use24HourFormat;
    public boolean isUnitsMetric;
}
