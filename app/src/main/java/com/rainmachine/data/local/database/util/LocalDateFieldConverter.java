package com.rainmachine.data.local.database.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.rainmachine.domain.util.Strings;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

public class LocalDateFieldConverter implements FieldConverter<LocalDate> {

    private static final String FORMAT_DATE = "yyyy-MM-dd";

    @Override
    public LocalDate fromCursorValue(Cursor cursor, int columnIndex) {
        String s = cursor.getString(columnIndex);
        if (!Strings.isBlank(s)) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(FORMAT_DATE);
            return formatter.parseLocalDate(s);
        } else {
            return null;
        }
    }

    @Override
    public void toContentValue(LocalDate value, String key, ContentValues values) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(FORMAT_DATE);
        if (value != null) {
            values.put(key, value.toString(formatter));
        } else {
            values.put(key, "");
        }
    }

    @Override
    public EntityConverter.ColumnType getColumnType() {
        return EntityConverter.ColumnType.TEXT;
    }
}
