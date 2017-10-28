package com.rainmachine.presentation.util.parcel;

import android.os.Parcel;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.converter.NullableParcelConverter;

public class LocalDateTimeParcelConverter extends NullableParcelConverter<LocalDateTime> {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void nullSafeToParcel(LocalDateTime input, Parcel parcel) {
        parcel.writeString(input.toString(DATE_TIME_PATTERN));
    }

    @Override
    public LocalDateTime nullSafeFromParcel(Parcel parcel) {
        DateTimeFormatter formatterDate = DateTimeFormat.forPattern(DATE_TIME_PATTERN);
        return formatterDate.parseLocalDateTime(parcel.readString());
    }
}
