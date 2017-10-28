package com.rainmachine.presentation.screens.programdetailsstarttime;

import com.rainmachine.domain.model.Program;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;

@Parcel
public class ProgramDetailsStartTimeExtra {
    public Program program;
    boolean use24HourFormat;
    public LocalDateTime sprinklerLocalDateTime;
}
