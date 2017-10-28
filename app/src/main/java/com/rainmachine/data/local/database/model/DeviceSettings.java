package com.rainmachine.data.local.database.model;

import java.util.HashMap;
import java.util.Map;

public class DeviceSettings {
    public Long _id;
    public String deviceId;
    public Map<Long, ZoneSettings> zones;

    public DeviceSettings() {
    }

    public DeviceSettings(String deviceId) {
        this.deviceId = deviceId;
        final int INITIAL_CAPACITY = 16;
        zones = new HashMap<>(INITIAL_CAPACITY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceSettings that = (DeviceSettings) o;

        if (_id != null ? !_id.equals(that._id) : that._id != null) {
            return false;
        }
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) {
            return false;
        }
        return zones != null ? zones.equals(that.zones) : that.zones == null;

    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (zones != null ? zones.hashCode() : 0);
        return result;
    }
}
