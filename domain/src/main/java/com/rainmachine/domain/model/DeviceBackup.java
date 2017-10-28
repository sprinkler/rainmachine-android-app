package com.rainmachine.domain.model;

import org.joda.time.LocalDateTime;

import java.util.List;

public class DeviceBackup {
    public long _databaseId;
    public String deviceId;
    public String name;
    public List<BackupEntry> entries;
    public DeviceType deviceType;

    public static class BackupEntry {
        public LocalDateTime localDateTime;
    }

    public enum DeviceType {SPK1, SPK2, SPK3_12, SPK3_16, NA}
}
