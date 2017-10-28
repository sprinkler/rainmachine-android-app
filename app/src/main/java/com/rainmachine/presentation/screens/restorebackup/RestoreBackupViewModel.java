package com.rainmachine.presentation.screens.restorebackup;

import com.rainmachine.domain.model.DeviceBackup;

import org.joda.time.LocalDateTime;
import org.parceler.Parcel;

import java.util.List;

class RestoreBackupViewModel {

    public List<BackupDeviceData> backupDevices;

    @Parcel
    public static class BackupDeviceData {
        public Long _databaseId;
        public String deviceId;
        public String name;
        public DeviceBackup.DeviceType deviceType;
        public List<Backup> backups;
        public LocalDateTime lastBackupLocalDateTime;
        public boolean use24HourFormat;

        @Override
        public String toString() {
            return name;
        }
    }

    @Parcel
    public static class Backup {
        public int position; // the corresponding position for the database backup item group
        public LocalDateTime localDateTime;
    }
}
