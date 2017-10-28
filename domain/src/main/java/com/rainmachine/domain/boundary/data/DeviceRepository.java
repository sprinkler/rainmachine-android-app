package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.DevicePreferences;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface DeviceRepository {

    Single<DevicePreferences> getDevicePreferencesForMostRecentDevice(DevicePreferences
                                                                              defaultPreferences);

    Single<DevicePreferences> getDevicePreferences(String deviceId);

    void markStaleCloudDevicesAsOffline(int seconds);

    Completable deleteDevice(long _id);

    void deleteStaleLocalDiscoveredDevices(int seconds);

    void deleteAllLocalDiscoveredDevices();

    void deleteAllCloudDevices();

    void deleteCloudDevices(String email);
}
