package com.rainmachine.presentation.screens.offline;

import org.parceler.Parcel;

@Parcel
class OfflineExtra {
    public long _deviceDatabaseId;
    public String deviceName;

    OfflineExtra() {
    }

    OfflineExtra(long _deviceDatabaseId, String deviceName) {
        this._deviceDatabaseId = _deviceDatabaseId;
        this.deviceName = deviceName;
    }
}
