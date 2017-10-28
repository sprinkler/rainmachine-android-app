package com.rainmachine.data.boundary;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.rainmachine.data.local.database.mapper.SprinklerSettingsMapper;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.database.model.SprinklerSettings;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.infrastructure.bus.DeviceEvent;
import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import nl.nl2312.rxcupboard2.RxDatabase;
import nl.qbusict.cupboard.Cupboard;

public class DeviceRepositoryImpl implements DeviceRepository {

    private final RxDatabase rxDatabase;
    private final Cupboard cupboard;
    private final SQLiteDatabase database;
    private Bus bus;

    public DeviceRepositoryImpl(RxDatabase rxDatabase, Cupboard cupboard, SQLiteDatabase database,
                                Bus bus) {
        this.rxDatabase = rxDatabase;
        this.cupboard = cupboard;
        this.database = database;
        this.bus = bus;
    }

    @Override
    public Single<DevicePreferences> getDevicePreferencesForMostRecentDevice
            (DevicePreferences defaultPreferences) {
        return rxDatabase.query(rxDatabase.buildQuery(Device.class))
                .toList()
                .flatMap(devices -> {
                    if (devices.size() > 0) {
                        // Look for the most recent device to use its preferences
                        Collections.sort(devices, (lhs, rhs) -> (int) (rhs.timestamp - lhs
                                .timestamp));
                        return getDevicePreferences(devices.get(0).deviceId)
                                .onErrorReturn(throwable -> defaultPreferences);
                    } else {
                        return Single.just(defaultPreferences);
                    }
                });
    }

    public synchronized Single<List<Device>> getManualDevices() {
        String selection = "type = " + Device.SPRINKLER_TYPE_MANUAL;
        return rxDatabase.query(
                rxDatabase.buildQuery(Device.class)
                        .withSelection(selection)
                        .orderBy("timestamp ASC"))
                .toList();
    }

    @Override
    public synchronized Single<DevicePreferences> getDevicePreferences(String deviceId) {
        return rxDatabase.query(
                rxDatabase.buildQuery(SprinklerSettings.class)
                        .withSelection("deviceId = ? ", deviceId))
                .map(SprinklerSettingsMapper.instance())
                .firstOrError();
    }

    @Override
    public synchronized void markStaleCloudDevicesAsOffline(int seconds) {
        // devices older than x seconds
        long timestamp = new DateTime().minusSeconds(seconds).getMillis();
        String selection = "timestamp < " + timestamp + " AND type = " + Device
                .SPRINKLER_TYPE_CLOUD;
        ContentValues values = new ContentValues();
        values.put("isOffline", true);
        int numUpdated = cupboard.withDatabase(database).update(Device.class, values, selection);
        if (numUpdated > 0) {
            bus.post(new DeviceEvent());
        }
    }

    @Override
    public synchronized Completable deleteDevice(final long _id) {
        return rxDatabase.delete(Device.class, _id).toCompletable();
    }

    @Override
    public synchronized void deleteStaleLocalDiscoveredDevices(int seconds) {
        // devices older than x seconds
        long timestamp = new DateTime().minusSeconds(seconds).getMillis();
        String selection = "timestamp < " + timestamp + " AND (type = " + Device
                .SPRINKLER_TYPE_AP + " OR type = " + Device.SPRINKLER_TYPE_UDP + ")";
        int numDeleted = cupboard.withDatabase(database).delete(Device.class, selection);
        if (numDeleted > 0) {
            bus.post(new DeviceEvent());
        }
    }

    @Override
    public synchronized void deleteAllLocalDiscoveredDevices() {
        String selection = "type = " + Device.SPRINKLER_TYPE_UDP + " OR type = " + Device
                .SPRINKLER_TYPE_AP;
        int numDeleted = cupboard.withDatabase(database).delete(Device.class, selection);
        if (numDeleted > 0) {
            bus.post(new DeviceEvent());
        }
    }

    @Override
    public synchronized void deleteAllCloudDevices() {
        String selection = "type = " + Device.SPRINKLER_TYPE_CLOUD;
        int numDeleted = cupboard.withDatabase(database).delete(Device.class, selection);
        if (numDeleted > 0) {
            bus.post(new DeviceEvent());
        }
    }

    @Override
    public synchronized void deleteCloudDevices(String email) {
        String selection = "type = " + Device.SPRINKLER_TYPE_CLOUD + " AND cloudEmail = ?";
        int numDeleted = cupboard.withDatabase(database).delete(Device.class, selection, email);
        if (numDeleted > 0) {
            bus.post(new DeviceEvent());
        }
    }
}
