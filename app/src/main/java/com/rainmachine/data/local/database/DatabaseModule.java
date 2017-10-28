package com.rainmachine.data.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.rainmachine.data.local.database.model.BackupDevice;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.CloudServers;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.database.model.DeviceSettings;
import com.rainmachine.data.local.database.model.SprinklerSettings;
import com.rainmachine.data.local.database.model.WateringLogs;
import com.rainmachine.data.local.database.model.WateringZone;
import com.rainmachine.data.local.database.util.GsonListFieldConverterFactory;
import com.rainmachine.data.local.database.util.GsonMapFieldConverterFactory;
import com.rainmachine.data.local.database.util.LocalDateFieldConverter;
import com.rainmachine.data.local.database.util.LocalDateTimeFieldConverter;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nl.nl2312.rxcupboard2.RxCupboard;
import nl.nl2312.rxcupboard2.RxDatabase;
import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

@Module(
        complete = false,
        library = true
)
public class DatabaseModule {

    @Provides
    @Singleton
    Cupboard provideCupboard(Gson gson) {
        Cupboard cupboard = new CupboardBuilder()
                .useAnnotations()
                .registerFieldConverter(LocalDateTime.class, new LocalDateTimeFieldConverter())
                .registerFieldConverter(LocalDate.class, new LocalDateFieldConverter())
                .registerFieldConverterFactory(new GsonListFieldConverterFactory(gson))
                .registerFieldConverterFactory(new GsonMapFieldConverterFactory(gson))
                .build();
        cupboard.register(WateringZone.class);
        cupboard.register(SprinklerSettings.class);
        cupboard.register(CloudInfo.class);
        cupboard.register(Device.class);
        cupboard.register(BackupDevice.class);
        cupboard.register(DashboardGraphs.class);
        cupboard.register(WateringLogs.class);
        cupboard.register(DeviceSettings.class);
        cupboard.register(CloudServers.class);
        cupboard.register(AppPreferencesDb.class);
        return cupboard;
    }

    @Provides
    @Singleton
    SQLiteDatabase provideDatabase(Context context, Cupboard cupboard) {
        return new RainSQLiteOpenHelper(context, cupboard).getWritableDatabase();
    }

    @Provides
    @Singleton
    RxDatabase provideRxDatabase(Cupboard cupboard, SQLiteDatabase database) {
        return RxCupboard.with(cupboard, database);
    }

    @Provides
    @Singleton
    DatabaseRepositoryImpl provideDatabaseRepository(Context context, Cupboard cupboard,
                                                     SQLiteDatabase database, RxDatabase rxDatabase,
                                                     Bus bus) {
        return new DatabaseRepositoryImpl(cupboard, database, rxDatabase, bus);
    }
}
