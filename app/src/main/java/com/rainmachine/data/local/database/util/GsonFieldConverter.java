package com.rainmachine.data.local.database.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

/**
 * FieldConverter that uses Gson to serialize and deserialize fields
 *
 * @param <T> the field type
 */
public class GsonFieldConverter<T> implements FieldConverter<T> {

    private final Class<T> type;
    private final Gson gson;

    public GsonFieldConverter(Gson gson, Class<T> type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T fromCursorValue(Cursor cursor, int columnIndex) {
        return gson.fromJson(cursor.getString(columnIndex), type);
    }

    @Override
    public void toContentValue(T value, String key, ContentValues values) {
        values.put(key, gson.toJson(value));
    }

    @Override
    public EntityConverter.ColumnType getColumnType() {
        return EntityConverter.ColumnType.TEXT;
    }
}