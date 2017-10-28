package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.BackupInfo;
import com.rainmachine.domain.model.DeviceBackup;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ZoneProperties;

import java.util.List;

import io.reactivex.Single;

public interface BackupRepository {

    Single<List<ZoneProperties>> zonesProperties(String body);

    Single<List<ZoneProperties>> zonesProperties3(String body);

    Single<List<Program>> programs(String body);

    Single<List<Program>> programs3(String body);

    Single<List<HourlyRestriction>> hourlyRestrictions(String body);

    Single<List<DeviceBackup>> getAllBackups();

    Single<BackupInfo> getBackupForPrograms(Long _backupDeviceDatabaseId, int position);

    Single<BackupInfo> getBackupForZonesProperties(Long _backupDeviceDatabaseId, int position);

    Single<BackupInfo> getBackupForGlobalRestrictions(Long _backupDeviceDatabaseId, int position);

    Single<BackupInfo> getBackupForHourlyRestrictions(Long _backupDeviceDatabaseId, int position);
}
