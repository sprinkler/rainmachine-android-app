package com.rainmachine.data.boundary;

import com.google.gson.Gson;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.BackupDevice;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ProgramsResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.mapper.ZonesPropertiesResponseMapper3;
import com.rainmachine.data.remote.sprinkler.v3.response.ProgramsResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesPropertiesResponse3;
import com.rainmachine.data.remote.sprinkler.v4.mapper.HourlyRestrictionsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ProgramsResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.mapper.ZonesPropertiesResponseMapper;
import com.rainmachine.data.remote.sprinkler.v4.response.GlobalRestrictionsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.HourlyRestrictionsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.ProgramsResponse;
import com.rainmachine.data.remote.sprinkler.v4.response.ZonesPropertiesResponse;
import com.rainmachine.domain.boundary.data.BackupRepository;
import com.rainmachine.domain.model.BackupInfo;
import com.rainmachine.domain.model.DeviceBackup;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.Strings;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public class BackupRepositoryImpl implements BackupRepository {

    private DatabaseRepositoryImpl databaseRepository;
    private Gson gson;
    private Device device;
    private SprinklerState sprinklerState;
    private Features features;

    private final Object programsBackupLock = new Object();
    private final Object zonesPropertiesBackupLock = new Object();
    private final Object globalRestrictionsBackupLock = new Object();
    private final Object hourlyRestrictionsBackupLock = new Object();

    public BackupRepositoryImpl(DatabaseRepositoryImpl databaseRepository, Gson gson, Device device,
                                SprinklerState sprinklerState, Features features) {
        this.databaseRepository = databaseRepository;
        this.gson = gson;
        this.device = device;
        this.sprinklerState = sprinklerState;
        this.features = features;
    }

    void updateBackupIfPossible(ZonesPropertiesResponse zonesPropertiesResponse) {
        if (canSaveBackup()) {
            String body = gson.toJson(zonesPropertiesResponse, ZonesPropertiesResponse.class);
            synchronized (zonesPropertiesBackupLock) {
                if (needsBackupUpdate(device, BackupDevice.KEY_ZONES_PROPERTIES, body)) {
                    databaseRepository.saveBackup(device, body, BackupDevice.KEY_ZONES_PROPERTIES);
                }
            }
            if (needsBackupUpdate(device, BackupDevice.KEY_DEVICE_TYPE, null)) {
                BackupDevice.DeviceType deviceType = BackupDevice.DeviceType.NA;
                if (features.isSpk1()) {
                    deviceType = BackupDevice.DeviceType.SPK1;
                } else if (features.isSpk2()) {
                    deviceType = BackupDevice.DeviceType.SPK2;
                } else if (features.isSpk3()) {
                    if (zonesPropertiesResponse.zones.size() == 12) {
                        deviceType = BackupDevice.DeviceType.SPK3_12;
                    } else if (zonesPropertiesResponse.zones.size() == 16) {
                        deviceType = BackupDevice.DeviceType.SPK3_16;
                    }
                }
                databaseRepository.saveBackupDeviceType(device, deviceType);
            }
        }
    }

    @Override
    public Single<List<ZoneProperties>> zonesProperties(String body) {
        return Single
                .fromCallable(() -> gson.fromJson(body, ZonesPropertiesResponse.class))
                .map(ZonesPropertiesResponseMapper.instance());
    }

    void updateBackupIfPossible(ZonesPropertiesResponse3 zonesPropertiesResponse3) {
        if (canSaveBackup()) {
            String body = gson.toJson(zonesPropertiesResponse3, ZonesPropertiesResponse3.class);
            if (needsBackupUpdate(device, BackupDevice.KEY_ZONES_PROPERTIES, body)) {
                databaseRepository.saveBackup(device, body, BackupDevice.KEY_ZONES_PROPERTIES,
                        true);
            }
            if (needsBackupUpdate(device, BackupDevice.KEY_DEVICE_TYPE, null)) {
                BackupDevice.DeviceType deviceType = BackupDevice.DeviceType.NA;
                if (features.isSpk1()) {
                    deviceType = BackupDevice.DeviceType.SPK1;
                } else if (features.isSpk2()) {
                    deviceType = BackupDevice.DeviceType.SPK2;
                } else if (features.isSpk3()) {
                    if (zonesPropertiesResponse3.zones.size() == 12) {
                        deviceType = BackupDevice.DeviceType.SPK3_12;
                    } else if (zonesPropertiesResponse3.zones.size() == 16) {
                        deviceType = BackupDevice.DeviceType.SPK3_16;
                    }
                }
                databaseRepository.saveBackupDeviceType(device, deviceType);
            }
        }
    }

    @Override
    public Single<List<ZoneProperties>> zonesProperties3(String body) {
        return Single
                .fromCallable(() -> gson.fromJson(body, ZonesPropertiesResponse3.class))
                .map(ZonesPropertiesResponseMapper3.instance());
    }

    void markZonesPropertiesNeedsUpdate() {
        if (canSaveBackup()) {
            databaseRepository.saveNeedsBackupUpdate(device, BackupDevice.KEY_ZONES_PROPERTIES);
        }
    }

    void updateBackupIfPossible(ProgramsResponse programsResponse) {
        if (canSaveBackup()) {
            String body = gson.toJson(programsResponse, ProgramsResponse.class);
            synchronized (programsBackupLock) {
                if (!Strings.isBlank(body) && needsBackupUpdate(device, BackupDevice
                        .KEY_PROGRAMS, body)) {
                    databaseRepository.saveBackup(device, body, BackupDevice.KEY_PROGRAMS);
                }
            }
        }
    }

    @Override
    public Single<List<Program>> programs(String body) {
        return Single
                .fromCallable(() -> gson.fromJson(body, ProgramsResponse.class))
                .map(ProgramsResponseMapper.instance());
    }

    void updateBackupIfPossible(ProgramsResponse3 programsResponse3) {
        if (canSaveBackup()) {
            String body = gson.toJson(programsResponse3, ProgramsResponse3.class);
            if (!Strings.isBlank(body) && needsBackupUpdate(device, BackupDevice.KEY_PROGRAMS,
                    body)) {
                databaseRepository.saveBackup(device, body, BackupDevice.KEY_PROGRAMS, true);
            }
        }
    }

    @Override
    public Single<List<Program>> programs3(String body) {
        return Single
                .fromCallable(() -> gson.fromJson(body, ProgramsResponse3.class))
                .map(ProgramsResponseMapper3.instance())
                .map(listBooleanPair -> listBooleanPair.getValue0());
    }

    void markProgramsNeedsUpdate() {
        if (canSaveBackup()) {
            databaseRepository.saveNeedsBackupUpdate(device, BackupDevice.KEY_PROGRAMS);
        }
    }

    void updateBackupIfPossible(GlobalRestrictionsResponse globalRestrictionsResponse) {
        if (canSaveBackup()) {
            String body = gson.toJson(globalRestrictionsResponse, GlobalRestrictionsResponse.class);
            synchronized (globalRestrictionsBackupLock) {
                if (needsBackupUpdate(device, BackupDevice.KEY_RESTRICTIONS_GLOBAL, body)) {
                    databaseRepository.saveBackup(device, body, BackupDevice
                            .KEY_RESTRICTIONS_GLOBAL);
                }
            }
        }
    }

    void markGlobalRestrictionsNeedsUpdate() {
        if (canSaveBackup()) {
            databaseRepository.saveNeedsBackupUpdate(device, BackupDevice.KEY_RESTRICTIONS_GLOBAL);
        }
    }

    void updateBackupIfPossible(HourlyRestrictionsResponse hourlyRestrictionsResponse) {
        if (canSaveBackup()) {
            String body = gson.toJson(hourlyRestrictionsResponse, HourlyRestrictionsResponse.class);
            synchronized (hourlyRestrictionsBackupLock) {
                if (needsBackupUpdate(device, BackupDevice.KEY_RESTRICTIONS_HOURLY, body)) {
                    databaseRepository.saveBackup(device, body, BackupDevice
                            .KEY_RESTRICTIONS_HOURLY);
                }
            }
        }
    }

    @Override
    public Single<List<HourlyRestriction>> hourlyRestrictions(String body) {
        return Single
                .fromCallable(() -> gson.fromJson(body, HourlyRestrictionsResponse.class))
                .map(HourlyRestrictionsResponseMapper.instance());
    }

    void markHourlyRestrictionsNeedsUpdate() {
        if (canSaveBackup()) {
            databaseRepository.saveNeedsBackupUpdate(device, BackupDevice.KEY_RESTRICTIONS_HOURLY);
        }
    }

    void updateBackupIfPossible(String deviceName) {
        if (canSaveBackup() && needsBackupUpdate(device, BackupDevice.KEY_DEVICE_NAME,
                deviceName)) {
            databaseRepository.saveBackupDeviceName(device, deviceName);
        }
    }

    private boolean needsBackupUpdate(Device device, int backupKey, String body) {
        BackupDevice backupDevice = databaseRepository.backupDeviceById(device.deviceId);
        if (backupDevice == null) {
            return true;
        }
        boolean needsUpdate = backupDevice.needsUpdate(backupKey, body);
        // For programs, some fields like status and nextRun change but the user hasn't really
        // changed anything so we should not update the backup
        if (needsUpdate && backupKey == BackupDevice.KEY_PROGRAMS && !backupDevice
                .needsUpdatePrograms) {
            BackupDevice.BackupItemGroup newestGroup = backupDevice.getNewestGroup();
            if (newestGroup != null) {
                BackupDevice.BackupItem backupItem = newestGroup.getBackupItem(backupKey);
                if (backupItem != null && !backupItem.isOldApiFormat && features.useNewApi()) {
                    ProgramsResponse backupResponse = gson.fromJson(backupItem.body,
                            ProgramsResponse.class);
                    ProgramsResponse newResponse = gson.fromJson(body, ProgramsResponse.class);
                    if (newResponse == null) {
                        return false;
                    }
                    needsUpdate = !ProgramsResponse.areSimilar(backupResponse, newResponse);
                }
            }
        }
        return needsUpdate;
    }

    private boolean canSaveBackup() {
        return features.hasBackup() && device.wizardHasRun && !sprinklerState
                .isBackupInProgress();
    }

    @Override
    public Single<List<DeviceBackup>> getAllBackups() {
        return Observable.fromCallable(() -> databaseRepository.allBackupDevices())
                .flatMap(backupDevices -> Observable.fromIterable(backupDevices))
                .map(backupDevice -> {
                    DeviceBackup deviceBackup = new DeviceBackup();
                    deviceBackup._databaseId = backupDevice._id;
                    deviceBackup.deviceId = backupDevice.deviceId;
                    deviceBackup.name = backupDevice.name;
                    deviceBackup.entries = new ArrayList<>(backupDevice.backupGroups.size());
                    for (BackupDevice.BackupItemGroup group : backupDevice.backupGroups) {
                        DeviceBackup.BackupEntry entry = new DeviceBackup.BackupEntry();
                        entry.localDateTime = group.getLocalDateTime();
                        deviceBackup.entries.add(entry);
                    }
                    deviceBackup.deviceType = deviceType(backupDevice.deviceType);
                    return deviceBackup;
                })
                .toList();
    }

    private DeviceBackup.DeviceType deviceType(BackupDevice.DeviceType deviceType) {
        switch (deviceType) {
            case SPK1:
                return DeviceBackup.DeviceType.SPK1;
            case SPK2:
                return DeviceBackup.DeviceType.SPK2;
            case SPK3_12:
                return DeviceBackup.DeviceType.SPK3_12;
            case SPK3_16:
                return DeviceBackup.DeviceType.SPK3_16;
            case NA:
                return DeviceBackup.DeviceType.NA;
        }
        throw new IllegalArgumentException("Unknown device type");
    }

    @Override
    public Single<BackupInfo> getBackupForPrograms(Long _backupDeviceDatabaseId,
                                                   int position) {
        return getBackup(_backupDeviceDatabaseId, position, BackupDevice.KEY_PROGRAMS);
    }

    @Override
    public Single<BackupInfo> getBackupForZonesProperties(Long _backupDeviceDatabaseId, int
            position) {
        return getBackup(_backupDeviceDatabaseId, position, BackupDevice.KEY_ZONES_PROPERTIES);
    }

    @Override
    public Single<BackupInfo> getBackupForGlobalRestrictions(Long _backupDeviceDatabaseId,
                                                             int position) {
        return getBackup(_backupDeviceDatabaseId, position, BackupDevice.KEY_RESTRICTIONS_GLOBAL);
    }

    @Override
    public Single<BackupInfo> getBackupForHourlyRestrictions(Long _backupDeviceDatabaseId,
                                                             int position) {
        return getBackup(_backupDeviceDatabaseId, position, BackupDevice.KEY_RESTRICTIONS_HOURLY);
    }

    private Single<BackupInfo> getBackup(Long _backupDeviceDatabaseId,
                                         int position, int backupKey) {
        return Single.fromCallable(() -> {
            BackupDevice backupDevice = databaseRepository.backupDeviceByInternalId
                    (_backupDeviceDatabaseId);
            BackupDevice.BackupItem item = backupDevice.backupGroups.get(position).getBackupItem
                    (backupKey);
            if (item == null) {
                return BackupInfo.NOT_FOUND;
            }
            return new BackupInfo(item.body, item.isOldApiFormat);
        });
    }
}
