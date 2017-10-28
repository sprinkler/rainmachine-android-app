package com.rainmachine.data.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rainmachine.data.boundary.DeviceRepositoryImpl;
import com.rainmachine.data.boundary.HandPreferenceRepositoryImpl;
import com.rainmachine.data.boundary.PushNotificationRepositoryImpl;
import com.rainmachine.data.local.LocalModule;
import com.rainmachine.data.remote.cloud.PushNotificationsDataStoreRemote;
import com.rainmachine.data.remote.util.RemoteModule;
import com.rainmachine.data.util.gson.GsonIntegerTypeAdapter;
import com.rainmachine.data.util.gson.GsonLocalDateAdapter;
import com.rainmachine.data.util.gson.GsonLocalDateTimeAdapter;
import com.rainmachine.data.util.gson.GsonLocalTimeAdapter;
import com.rainmachine.data.util.gson.GsonLongTypeAdapter;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.HandPreferenceRepository;
import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.notifiers.HandPreferenceNotifier;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nl.nl2312.rxcupboard2.RxDatabase;
import nl.qbusict.cupboard.Cupboard;

@Module(
        includes = {
                LocalModule.class,
                RemoteModule.class
        },
        complete = false,
        library = true
)
public class DataModule {

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonLongTypeAdapter gsonLongTypeAdapter = new GsonLongTypeAdapter();
        GsonIntegerTypeAdapter gsonIntegerTypeAdapter = new GsonIntegerTypeAdapter();
        gsonBuilder.registerTypeAdapter(long.class, gsonLongTypeAdapter);
        gsonBuilder.registerTypeAdapter(Long.class, gsonLongTypeAdapter);
        gsonBuilder.registerTypeAdapter(int.class, gsonIntegerTypeAdapter);
        gsonBuilder.registerTypeAdapter(Integer.class, gsonIntegerTypeAdapter);
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new GsonLocalDateAdapter());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new GsonLocalTimeAdapter());
        gsonBuilder.enableComplexMapKeySerialization();
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    PushNotificationRepository providePushNotificationsRepository
            (PushNotificationsDataStoreRemote remoteDataStore, Context context) {
        return new PushNotificationRepositoryImpl(remoteDataStore);
    }

    @Provides
    @Singleton
    DeviceRepositoryImpl provideDevicesRepositoryImpl(RxDatabase rxDatabase, Cupboard cupboard,
                                                      SQLiteDatabase database, Bus bus) {
        return new DeviceRepositoryImpl(rxDatabase, cupboard, database, bus);
    }

    @Provides
    @Singleton
    DeviceRepository provideDevicesRepository(DeviceRepositoryImpl deviceRepositoryImpl) {
        return deviceRepositoryImpl;
    }

    @Provides
    @Singleton
    HandPreferenceRepository provideHandPreferenceRepository(RxDatabase rxDatabase,
                                                             HandPreferenceNotifier
                                                                     handPreferenceNotifier) {
        return new HandPreferenceRepositoryImpl(rxDatabase, handPreferenceNotifier);
    }
}
