package com.rainmachine.presentation.screens.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.infrastructure.WifiUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.infrastructure.receivers.OnConnectivityChangeReceiver;
import com.rainmachine.infrastructure.scanner.DeviceScanner;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class DevicesMixer {

    private static final int REFRESH_TIMEOUT_MILLIS = 700; // millis

    private Context context;
    private Bus bus;
    private DatabaseRepositoryImpl databaseRepository;
    private DeviceScanner deviceScanner;
    private DeviceRepository deviceRepository;

    private DuplicateDeviceComparator duplicateDeviceComparator;
    private DeviceOrderComparator deviceOrderComparator;
    private boolean isRefreshingManual;

    DevicesMixer(Context context, Bus bus, DatabaseRepositoryImpl databaseRepository,
                 DeviceScanner deviceScanner, DeviceRepository deviceRepository) {
        this.context = context;
        this.bus = bus;
        this.databaseRepository = databaseRepository;
        this.deviceScanner = deviceScanner;
        this.deviceRepository = deviceRepository;
        duplicateDeviceComparator = new DuplicateDeviceComparator();
        deviceOrderComparator = new DeviceOrderComparator();
        this.bus.register(this);
    }

    public void refresh() {
        Observable
                .fromCallable(() -> {
                    doRefresh();
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void doRefresh() {
        if (isRefreshingManual) {
            return;
        }
        List<Device> allDevices = databaseRepository.getAllDevices();
        List<Device> devices = convertAllDevices(allDevices);
        String currentWifiMac = WifiUtils.getCurrentWifiMac();
        bus.post(new RefreshEvent(devices, currentWifiMac));
    }

    static class RefreshEvent extends BaseEvent {
        List<Device> data;
        String currentWifiMac;

        private RefreshEvent(List<Device> data, String currentWifiMac) {
            this.data = data;
            this.currentWifiMac = currentWifiMac;
        }
    }

    private List<Device> convertAllDevices(List<Device> allDevices) {
        List<Device> devices = new ArrayList<>();
        // We sort the devices based on deviceId and those identical in a specific order: ap ->
        // local -> cloud
        Collections.sort(allDevices, duplicateDeviceComparator);
        Device lastDevice = null;
        for (Device device : allDevices) {
            if (lastDevice == null) {
                devices.add(device);
                lastDevice = device;
            } else if (!device.deviceId.equals(lastDevice.deviceId)) {
                devices.add(device);
                lastDevice = device;
            } else {
                Timber.d("Duplicate device %s vs %s", lastDevice.name, device.name);
                if (lastDevice.isUdp() && device.isCloud()) {
                    Timber.d("UDP device is also cloud device. Add alternate url");
                    lastDevice.alternateCloudUrl = device.getUrl();
                }
            }
        }
        Collections.sort(devices, deviceOrderComparator);
        return devices;
    }

    void refreshDevicesManually() {
        isRefreshingManual = true;
        deviceScanner.stopScanning();
        Observable
                .fromCallable(() -> {
                    deviceRepository.deleteAllLocalDiscoveredDevices();
                    Sleeper.sleep(REFRESH_TIMEOUT_MILLIS);
                    isRefreshingManual = false;
                    doRefresh();
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
        deviceScanner.startScanning();
    }

    private static class DuplicateDeviceComparator implements Comparator<Device> {
        @Override
        public int compare(Device lDevice, Device rDevice) {
            if (!lDevice.deviceId.equals(rDevice.deviceId)) {
                return lDevice.deviceId.compareTo(rDevice.deviceId);
            }
            if (lDevice.isAp()) {
                return -1;
            } else if (rDevice.isAp()) {
                return 1;
            } else if (lDevice.isUdp()) {
                return -1;
            } else if (rDevice.isUdp()) {
                return 1;
            }
            return 0;
        }
    }

    private static class DeviceOrderComparator implements Comparator<Device> {
        @Override
        public int compare(Device lDevice, Device rDevice) {
            if (lDevice.isOffline) {
                return 1;
            } else if (rDevice.isOffline) {
                return -1;
            }
            if (lDevice.type == rDevice.type) {
                return lDevice.name.compareToIgnoreCase(rDevice.name);
            }
            if (lDevice.isAp()) {
                return -1;
            } else if (rDevice.isAp()) {
                return 1;
            } else if (lDevice.isUdp()) {
                return -1;
            } else if (rDevice.isUdp()) {
                return 1;
            } else if (lDevice.isCloud()) {
                return -1;
            } else if (rDevice.isCloud()) {
                return 1;
            }
            return 0;
        }
    }

    void startScanning() {
        deviceScanner.startScanning();
        enableConnectivityChangeReceiver();
    }

    void stopScanning() {
        deviceScanner.stopScanning();
        disableConnectivityChangeReceiver();
    }

    private void enableConnectivityChangeReceiver() {
        ComponentName receiver = new ComponentName(context, OnConnectivityChangeReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableConnectivityChangeReceiver() {
        ComponentName receiver = new ComponentName(context, OnConnectivityChangeReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
