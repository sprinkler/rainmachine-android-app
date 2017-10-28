package com.rainmachine.data.local.database.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.rainmachine.domain.util.Strings;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

public class LocalDateTimeFieldConverter implements FieldConverter<LocalDateTime> {

    private static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";

    @Override
    public LocalDateTime fromCursorValue(Cursor cursor, int columnIndex) {
        String s = cursor.getString(columnIndex);
        if (!Strings.isBlank(s)) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(FORMAT_DATE_TIME);
            return formatter.parseLocalDateTime(s);
        } else {
            return null;
        }
    }

    @Override
    public void toContentValue(LocalDateTime value, String key, ContentValues values) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(FORMAT_DATE_TIME);
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
