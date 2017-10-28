package com.rainmachine.data.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nl.qbusict.cupboard.Cupboard;
import timber.log.Timber;

public class RainSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rainmachine.db";
    private static final int VERSION_CLOUD_SERVERS = 23;
    private static final int VERSION_APP_PREFERENCES = 25;
    private static final int DATABASE_VERSION = VERSION_APP_PREFERENCES;

    protected Cupboard cupboard;

    public RainSQLiteOpenHelper(Context context, Cupboard cupboard) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.cupboard = cupboard;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard.withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("upgrade database from %d to %d", oldVersion, newVersion);
        cupboard.withDatabase(db).upgradeTables();
    }
}
