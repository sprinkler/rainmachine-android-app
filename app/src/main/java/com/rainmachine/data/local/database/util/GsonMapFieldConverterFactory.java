package com.rainmachine.data.local.database.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

/**
 * FieldConverter that uses Gson to serialize and deserialize {@link Map}s
 */
public class GsonMapFieldConverterFactory implements FieldConverterFactory {

    private final Gson gson;

    public GsonMapFieldConverterFactory(Gson gson) {
        this.gson = gson;
    }

    @Override
    public FieldConverter<?> create(Cupboard cupboard, final Type type) {
        if (type == Map.class || (type instanceof ParameterizedType
                && ((Class<?>) (((ParameterizedType) type).getRawType())).isAssignableFrom(Map
                .class))) {
            return new FieldConverter<Map<?, ?>>() {
                @Override
                public Map<?, ?> fromCursorValue(Cursor cursor, int columnIndex) {
                    String json = cursor.getString(columnIndex);
                    return gson.fromJson(json, type);
                }

                @Override
                public void toContentValue(Map<?, ?> value, String key, ContentValues values) {
                    values.put(key, gson.toJson(value, type));
                }

                @Override
                public EntityConverter.ColumnType getColumnType() {
                    return EntityConverter.ColumnType.TEXT;
                }
            };
        }
        return null;
    }
}