package com.rainmachine.presentation.screens.programdetailsfrequency;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.parcel.LocalDateTimeParcelConverter;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

@Parcel
public class ProgramDetailsFrequencyExtra {
    public Program program;
    @ParcelPropertyConverter(LocalDateTimeParcelConverter.class)
    public LocalDateTime sprinklerLocalDateTime;
}
