package com.rainmachine.presentation.screens.programdetailszones;

import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.util.parcel.LocalDateTimeParcelConverter;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

@Parcel
public class ProgramDetailsZonesExtra {
    public Program program;
    public int positionInList;
    @ParcelPropertyConverter(LocalDateTimeParcelConverter.class)
    public LocalDateTime sprinklerLocalDateTime;
}
