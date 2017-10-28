package com.rainmachine.data.local.database.model;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class BackupDevice {
    public static final int KEY_DEVICE_TYPE = 0;
    public static final int KEY_DEVICE_NAME = 1;
    public static final int KEY_PROGRAMS = 2;
    public static final int KEY_ZONES_PROPERTIES = 3;
    public static final int KEY_RESTRICTIONS_GLOBAL = 4;
    public static final int KEY_RESTRICTIONS_HOURLY = 5;

    public Long _id;
    public String deviceId;
    public String name;
    public DeviceType deviceType;
    public List<BackupItemGroup> backupGroups;
    public boolean needsUpdateDeviceType;
    public boolean needsUpdateDeviceName;
    public boolean needsUpdatePrograms;
    public boolean needsUpdateZonesProperties;
    public boolean needsUpdateRestrictionsGlobal;
    public boolean needsUpdateRestrictionsHourly;

    public BackupDevice() {
    }

    public BackupDevice(String deviceId, String name) {
        this.deviceId = deviceId;
        this.name = name;
        this.deviceType = DeviceType.NA;
        backupGroups = new ArrayList<>(10);
        needsUpdateDeviceType = true;
        needsUpdatePrograms = true;
        needsUpdateZonesProperties = true;
        needsUpdateRestrictionsGlobal = true;
        needsUpdateRestrictionsHourly = true;
    }

    public static class BackupItemGroup {
        public BackupItem programs;
        public BackupItem zonesProperties;
        public BackupItem restrictionsGlobal;
        public BackupItem restrictionsHourly;

        public LocalDateTime getLocalDateTime() {
            LocalDateTime lastDateTime = null;
            if (programs != null) {
                lastDateTime = programs.localDateTime;
            }
            if (zonesProperties != null && (lastDateTime == null || lastDateTime.isBefore
                    (zonesProperties.localDateTime))) {
                lastDateTime = zonesProperties.localDateTime;
            }
            if (restrictionsGlobal != null && (lastDateTime == null || lastDateTime.isBefore
                    (restrictionsGlobal.localDateTime))) {
                lastDateTime = restrictionsGlobal.localDateTime;
            }
            if (restrictionsHourly != null && (lastDateTime == null || lastDateTime.isBefore
                    (restrictionsHourly.localDateTime))) {
                lastDateTime = restrictionsHourly.localDateTime;
            }
            return lastDateTime;
        }

        public BackupItem getBackupItem(int backupKey) {
            if (backupKey == KEY_PROGRAMS) {
                return programs;
            }
            if (backupKey == KEY_ZONES_PROPERTIES) {
                return zonesProperties;
            }
            if (backupKey == KEY_RESTRICTIONS_GLOBAL) {
                return restrictionsGlobal;
            }
            if (backupKey == KEY_RESTRICTIONS_HOURLY) {
                return restrictionsHourly;
            }
            return null;
        }

        public void saveBackupItem(int backupKey, BackupItem item) {
            if (backupKey == KEY_PROGRAMS) {
                programs = item;
            }
            if (backupKey == KEY_ZONES_PROPERTIES) {
                zonesProperties = item;
            }
            if (backupKey == KEY_RESTRICTIONS_GLOBAL) {
                restrictionsGlobal = item;
            }
            if (backupKey == KEY_RESTRICTIONS_HOURLY) {
                restrictionsHourly = item;
            }
        }

        public static BackupItemGroup cloneIt(BackupItemGroup srcGroup, BackupItemGroup destGroup) {
            destGroup.programs = srcGroup.programs != null ? new BackupItem(srcGroup.programs) :
                    null;
            destGroup.zonesProperties = srcGroup.zonesProperties != null ? new BackupItem
                    (srcGroup.zonesProperties) : null;
            destGroup.restrictionsGlobal = srcGroup.restrictionsGlobal != null ? new BackupItem
                    (srcGroup.restrictionsGlobal) : null;
            destGroup.restrictionsHourly = srcGroup.restrictionsHourly != null ? new BackupItem
                    (srcGroup.restrictionsHourly) : null;
            return destGroup;
        }
    }

    public static class BackupItem {

        public String body;
        public LocalDateTime localDateTime; // the date it was saved
        public boolean isOldApiFormat; // the body is a json in the old API format

        public BackupItem() {
        }

        public BackupItem(BackupItem clone) {
            body = clone.body;
            localDateTime = clone.localDateTime;
            isOldApiFormat = clone.isOldApiFormat;
        }
    }

    public enum DeviceType {SPK1, SPK2, SPK3_12, SPK3_16, NA}

    public BackupItem prepareNextBackupItem(int backupKey) {
        BackupItemGroup group = getAppropriateItemGroup(backupKey);
        BackupItem item = group.getBackupItem(backupKey);
        if (item == null) {
            item = new BackupItem();
            group.saveBackupItem(backupKey, item);
        }
        return item;
    }

    private BackupItemGroup getAppropriateItemGroup(int backupKey) {
        LocalDateTime newestDateTime = null;
        BackupItemGroup newestGroup = null;
        LocalDateTime oldestDateTime = null;
        BackupItemGroup oldestGroup = null;
        for (BackupItemGroup group : backupGroups) {
            if (newestDateTime == null || newestDateTime.isBefore(group.getLocalDateTime())) {
                newestDateTime = group.getLocalDateTime();
                newestGroup = group;
            }
            if (oldestDateTime == null || oldestDateTime.isAfter(group.getLocalDateTime())) {
                oldestDateTime = group.getLocalDateTime();
                oldestGroup = group;
            }
        }
        BackupItemGroup newGroup;
        if (backupGroups.size() == 0) {
            newGroup = new BackupItemGroup();
            backupGroups.add(newGroup);
        } else {
            if (newestGroup != null && newestGroup.getBackupItem(backupKey) == null) {
                newGroup = newestGroup;
            } else {
                if (backupGroups.size() < 10) {
                    newGroup = BackupItemGroup.cloneIt(newestGroup, new BackupItemGroup());
                    backupGroups.add(newGroup);
                } else {
                    newGroup = BackupItemGroup.cloneIt(newestGroup, oldestGroup);
                }
            }
        }
        return newGroup;
    }

    public BackupItemGroup getNewestGroup() {
        LocalDateTime newestDateTime = null;
        BackupItemGroup newestGroup = null;
        for (BackupItemGroup group : backupGroups) {
            if (newestDateTime == null || newestDateTime.isBefore(group.getLocalDateTime())) {
                newestDateTime = group.getLocalDateTime();
                newestGroup = group;
            }
        }
        return newestGroup;
    }

    public void saveNeedsUpdate(int backupKey) {
        if (backupKey == KEY_DEVICE_TYPE) {
            needsUpdateDeviceType = true;
        } else if (backupKey == KEY_DEVICE_NAME) {
            needsUpdateDeviceName = true;
        } else if (backupKey == KEY_PROGRAMS) {
            needsUpdatePrograms = true;
        } else if (backupKey == KEY_ZONES_PROPERTIES) {
            needsUpdateZonesProperties = true;
        } else if (backupKey == KEY_RESTRICTIONS_GLOBAL) {
            needsUpdateRestrictionsGlobal = true;
        } else if (backupKey == KEY_RESTRICTIONS_HOURLY) {
            needsUpdateRestrictionsHourly = true;
        }
    }

    public boolean needsUpdate(int backupKey, String body) {
        if (backupKey == KEY_DEVICE_TYPE) {
            return needsUpdateDeviceType;
        }

        boolean needsUpdate = false;
        if (backupKey == KEY_DEVICE_NAME) {
            needsUpdate = needsUpdateDeviceName;
        } else if (backupKey == KEY_PROGRAMS) {
            needsUpdate = needsUpdatePrograms;
        } else if (backupKey == KEY_ZONES_PROPERTIES) {
            needsUpdate = needsUpdateZonesProperties;
        } else if (backupKey == KEY_RESTRICTIONS_GLOBAL) {
            needsUpdate = needsUpdateRestrictionsGlobal;
        } else if (backupKey == KEY_RESTRICTIONS_HOURLY) {
            needsUpdate = needsUpdateRestrictionsHourly;
        }
        if (needsUpdate) {
            return true;
        }
        if (backupKey == KEY_DEVICE_NAME) {
            return !name.equals(body);
        }
        BackupItemGroup newestGroup = getNewestGroup();
        if (newestGroup == null) {
            return true;
        }
        BackupItem backupItem = newestGroup.getBackupItem(backupKey);
        if (backupItem == null) {
            return true;
        }
        return !backupItem.body.equalsIgnoreCase(body);
    }

    public void clearNeedsUpdate(int backupKey) {
        if (backupKey == KEY_DEVICE_TYPE) {
            needsUpdateDeviceType = false;
        } else if (backupKey == KEY_DEVICE_NAME) {
            needsUpdateDeviceName = false;
        } else if (backupKey == KEY_PROGRAMS) {
            needsUpdatePrograms = false;
        } else if (backupKey == KEY_ZONES_PROPERTIES) {
            needsUpdateZonesProperties = false;
        } else if (backupKey == KEY_RESTRICTIONS_GLOBAL) {
            needsUpdateRestrictionsGlobal = false;
        } else if (backupKey == KEY_RESTRICTIONS_HOURLY) {
            needsUpdateRestrictionsHourly = false;
        }
    }
}
