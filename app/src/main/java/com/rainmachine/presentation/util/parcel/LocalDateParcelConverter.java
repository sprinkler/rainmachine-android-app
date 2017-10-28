package com.rainmachine.presentation.util.parcel;

import android.os.Parcel;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.converter.NullableParcelConverter;

public class LocalDateParcelConverter extends NullableParcelConverter<LocalDate> {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Override
    public void nullSafeToParcel(LocalDate input, Parcel parcel) {
        parcel.writeString(input.toString(DATE_PATTERN));
    }

    @Override
    public LocalDate nullSafeFromParcel(Parcel parcel) {
        DateTimeFormatter formatterDate = DateTimeFormat.forPattern(DATE_PATTERN);
        return formatterDate.parseLocalDate(parcel.readString());
    }
}
