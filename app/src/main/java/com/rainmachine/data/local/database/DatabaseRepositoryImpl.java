package com.rainmachine.data.local.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.rainmachine.data.local.database.model.BackupDevice;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.CloudServers;
import com.rainmachine.data.local.database.model.DashboardGraphs;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.database.model.DeviceSettings;
import com.rainmachine.data.local.database.model.SprinklerSettings;
import com.rainmachine.data.local.database.model.WateringLogs;
import com.rainmachine.data.local.database.model.WateringZone;
import com.rainmachine.infrastructure.bus.DeviceEvent;
import com.squareup.otto.Bus;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import nl.nl2312.rxcupboard2.DatabaseChange;
import nl.nl2312.rxcupboard2.RxDatabase;
import nl.qbusict.cupboard.Cupboard;

public class DatabaseRepositoryImpl {

    private Cupboard cupboard;
    private SQLiteDatabase database;
    private RxDatabase rxDatabase;
    private Bus bus; // we should not use bus here. Remove.

    public DatabaseRepositoryImpl(Cupboard cupboard, SQLiteDatabase database, RxDatabase
            rxDatabase, Bus bus) {
        this.cupboard = cupboard;
        this.database = database;
        this.rxDatabase = rxDatabase;
        this.bus = bus;
    }

    public synchronized void saveDevice(Device device) {
        Device existentDevice;
        if (device.isAp() || device.isUdp()) {
            existentDevice = getApOrUdpDevice(device.deviceId);
        } else {
            existentDevice = getDevice(device.deviceId, device.type);
        }
        if (existentDevice != null)
        // we override the device entry if it already exists
        {
            device._id = existentDevice._id;
        }
        device = rxDatabase.put(device).blockingGet();

        if (existentDevice == null || hasChanges(device, existentDevice)) {
            bus.post(new DeviceEvent());
        }
    }

    private boolean hasChanges(Device device, Device existentDevice) {
        return !device.name.equals(existentDevice.name) || device.type != existentDevice.type ||
                !device.getUrl().equals(existentDevice.getUrl()) || device.wizardHasRun !=
                existentDevice
                        .wizardHasRun || device.isOffline != existentDevice.isOffline;
    }

    public Device getDevice(String deviceId, int type) {
        String selection = "deviceId = ? AND type = " + type;
        return cupboard.withDatabase(database).query(Device.class).withSelection(selection,
                deviceId).get();
    }

    private Device getApOrUdpDevice(String deviceId) {
        String selection = "deviceId = ? AND (type = " + Device.SPRINKLER_TYPE_AP + " OR type = "
                + Device.SPRINKLER_TYPE_UDP + ")";
        return cupboard.withDatabase(database).query(Device.class).withSelection(selection,
                deviceId).get();
    }

    public List<Device> getUdpAndCloudDevice(String deviceId) {
        String selection = "deviceId = ? AND (type = " + Device.SPRINKLER_TYPE_UDP + " OR type = "
                + Device.SPRINKLER_TYPE_CLOUD + ")";
        return cupboard.withDatabase(database).query(Device.class).withSelection(selection,
                deviceId).list();
    }

    public synchronized void updateDevice(Long _id, final String name, final String url) {
        rxDatabase
                .query(Device.class, "_id = " + _id)
                .subscribe(device -> {
                    device.name = name;
                    device.setUrl(url);
                    rxDatabase.put(device).blockingGet();
                });
    }

    public synchronized void removeDevice(long _id) {
        boolean success = rxDatabase.delete(Device.class, _id).blockingGet();
        if (success) {
            bus.post(new DeviceEvent());
        }
    }

    public Observable<String> manualDevicesChanges() {
        return rxDatabase
                .changes(Device.class)
                .map(deviceDatabaseChange -> "ignored")
                .toObservable();
    }

    public synchronized List<Device> getAllDevices() {
        return cupboard.withDatabase(database).query(Device.class).list();
    }

    /* Backup logic */
    public void saveBackup(Device device, String body, int backupKey, boolean isOldApiFormat) {
        database.beginTransaction();
        try {
            BackupDevice backupDevice = backupDeviceById(device.deviceId);
            if (backupDevice == null) {
                backupDevice = new BackupDevice(device.deviceId, device.name);
            }

            BackupDevice.BackupItem item = backupDevice.prepareNextBackupItem(backupKey);
            item.body = body;
            item.isOldApiFormat = isOldApiFormat;
            item.localDateTime = new LocalDateTime();
            backupDevice.clearNeedsUpdate(backupKey);
            cupboard.withDatabase(database).put(backupDevice);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void saveBackup(Device device, String body, int backupKey) {
        saveBackup(device, body, backupKey, false);
    }

    public void saveBackupDeviceName(Device device, String name) {
        database.beginTransaction();
        try {
            BackupDevice backupDevice = backupDeviceById(device.deviceId);
            if (backupDevice == null) {
                backupDevice = new BackupDevice(device.deviceId, device.name);
            } else {
                backupDevice.name = name;
            }
            backupDevice.clearNeedsUpdate(BackupDevice.KEY_DEVICE_NAME);
            cupboard.withDatabase(database).put(backupDevice);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void saveBackupDeviceType(Device device, BackupDevice.DeviceType deviceType) {
        database.beginTransaction();
        try {
            BackupDevice backupDevice = backupDeviceById(device.deviceId);
            if (backupDevice == null) {
                backupDevice = new BackupDevice(device.deviceId, device.name);
            }
            backupDevice.deviceType = deviceType;
            backupDevice.clearNeedsUpdate(BackupDevice.KEY_DEVICE_TYPE);
            cupboard.withDatabase(database).put(backupDevice);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void saveNeedsBackupUpdate(Device device, int backupKey) {
        BackupDevice backupDevice = backupDeviceById(device.deviceId);
        if (backupDevice == null) {
            return;
        }
        backupDevice.saveNeedsUpdate(backupKey);
        cupboard.withDatabase(database).put(backupDevice);
    }

    public BackupDevice backupDeviceById(String deviceId) {
        return cupboard.withDatabase(database).query(BackupDevice.class)
                .withSelection("deviceId = ?", deviceId).get();
    }

    public BackupDevice backupDeviceByInternalId(Long _deviceId) {
        return cupboard.withDatabase(database).get(BackupDevice.class, _deviceId);
    }

    public List<BackupDevice> allBackupDevices() {
        return cupboard.withDatabase(database).query(BackupDevice.class).list();
    }

    /* Dashboard graphs */
    public DashboardGraphs dashboardGraphsById(String deviceId) {
        return cupboard.withDatabase(database).query(DashboardGraphs.class)
                .withSelection("deviceId = ?", deviceId).get();
    }

    public void saveDashboardGraphs(DashboardGraphs dashboardGraphs) {
        rxDatabase.put(dashboardGraphs).blockingGet();
    }

    public Observable<DashboardGraphs> dashboardGraphsByIdChanges() {
        return rxDatabase
                .changes(DashboardGraphs.class)
                .map(deviceDatabaseChange -> deviceDatabaseChange.entity())
                .toObservable();
    }

    public SprinklerSettings sprinklerSettings(Device device) {
        SprinklerSettings sprinklerSettings = cupboard.withDatabase(database)
                .query(SprinklerSettings.class)
                .withSelection("deviceId = ? ", device.deviceId)
                .get();
        if (sprinklerSettings == null) {
            sprinklerSettings = SprinklerSettings.createDefault(device);
            cupboard.withDatabase(database).put(sprinklerSettings);
        }
        return sprinklerSettings;
    }

    public void updateSprinklerSettingsUnits(Device device, boolean isUnitsMetric) {
        ContentValues values = new ContentValues();
        values.put("units", SprinklerSettings.getUnitsInternalValue(isUnitsMetric));
        cupboard.withDatabase(database).update(SprinklerSettings.class, values, "deviceId = ?",
                device.deviceId);
    }

    public void updateSprinklerSettings24HourFormat(Device device, boolean use24HourFormat) {
        ContentValues values = new ContentValues();
        values.put("use24HourFormat", use24HourFormat);
        cupboard.withDatabase(database).update(SprinklerSettings.class, values, "deviceId = ?",
                device.deviceId);
    }

    public WateringZone getWateringZone(Device device, long zoneId) {
        return cupboard.withDatabase(database).query(WateringZone.class).withSelection
                ("deviceId = ? AND zoneId = " + zoneId, device.deviceId).get();
    }

    public void saveWateringZone(WateringZone wateringZone) {
        cupboard.withDatabase(database).put(wateringZone);
    }

    public void updateWateringZone(Long _id, int startCounter) {
        ContentValues values = new ContentValues();
        values.put("seconds", startCounter);
        cupboard.withDatabase(database).update(WateringZone.class, values, "_id = " + _id);
    }

    public CloudInfo getCloudInfo(String email) {
        return cupboard.withDatabase(database).query(CloudInfo.class).withSelection("email = " +
                "?", email).get();
    }

    public List<CloudInfo> getCloudInfoList() {
        return cupboard.withDatabase(database).query(CloudInfo.class).list();
    }

    public long saveCloudInfo(CloudInfo cloudInfo) {
        return cupboard.withDatabase(database).put(cloudInfo);
    }

    public void updateCloudInfo(String email, int activeCount, int knownCount, int authCount) {
        ContentValues values = new ContentValues();
        values.put("activeCount", activeCount);
        values.put("knownCount", knownCount);
        values.put("authCount", authCount);
        cupboard.withDatabase(database).update(CloudInfo.class, values, "email = ?", email);
    }

    public void removeCloudInfo(long _id) {
        cupboard.withDatabase(database).delete(CloudInfo.class, _id);
    }

    public Single<WateringLogs> wateringLogs(String deviceId) {
        String selection = "deviceId = ?";
        return rxDatabase
                .query(WateringLogs.class, selection, deviceId)
                // fix for earlier version which used different format
                .onErrorReturn(throwable -> emptyWateringLogs())
                .first(emptyWateringLogs());
    }

    private WateringLogs emptyWateringLogs() {
        WateringLogs wateringLogs = new WateringLogs();
        wateringLogs.startDate = new LocalDate(1984, 1, 1);
        wateringLogs.endDate = new LocalDate(1984, 1, 2);
        return wateringLogs;
    }

    public void saveWateringLogs(WateringLogs wateringLogs) {
        String selection = "deviceId = ?";
        cupboard.withDatabase(database).delete(WateringLogs.class, selection, wateringLogs
                .deviceId);
        cupboard.withDatabase(database).put(wateringLogs);
    }

    public Single<DeviceSettings> deviceSettings(final String deviceId) {
        String selection = "deviceId = ?";
        return rxDatabase
                .query(DeviceSettings.class, selection, deviceId)
                .firstOrError()
                .onErrorReturn(throwable -> {
                    // Save a default entry if none exists yet
                    DeviceSettings deviceSettings = new DeviceSettings(deviceId);
                    deviceSettings._id = saveDeviceSettings(deviceSettings);
                    return deviceSettings;
                });
    }

    public Observable<DeviceSettings> deviceSettingsChanges(final String deviceId) {
        return Observable
                .merge(Observable.just(new DatabaseChange.DatabaseUpdate<DeviceSettings>()),
                        rxDatabase.changes(DeviceSettings.class).toObservable())
                .switchMapSingle(databaseChange -> deviceSettings(deviceId));
    }

    public Long saveDeviceSettings(final DeviceSettings deviceSettings) {
        return rxDatabase.put(deviceSettings).blockingGet()._id;
    }

    public Single<List<CloudServers>> allCloudServers() {
        return rxDatabase.query(CloudServers.class).toList();
    }

    public Single<CloudServers> saveCloudServers(final CloudServers cloudServers) {
        return rxDatabase.put(cloudServers);
    }
}
