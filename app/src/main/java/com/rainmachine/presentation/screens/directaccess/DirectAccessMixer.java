package com.rainmachine.presentation.screens.directaccess;

import com.rainmachine.data.boundary.DeviceRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.util.Irrelevant;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

class DirectAccessMixer {

    private DatabaseRepositoryImpl databaseRepository;
    private PrefRepository prefRepository;
    private DeviceRepositoryImpl devicesRepository;

    DirectAccessMixer(DatabaseRepositoryImpl databaseRepository, PrefRepository
            prefRepository, DeviceRepositoryImpl devicesRepository) {
        this.databaseRepository = databaseRepository;
        this.prefRepository = prefRepository;
        this.devicesRepository = devicesRepository;
    }

    Observable<List<Device>> refresh() {
        return devicesRepository.getManualDevices().toObservable();
    }

    Observable<String> dataChanges() {
        return databaseRepository
                .manualDevicesChanges();
    }

    void saveDevice(final Device device) {
        Observable
                .fromCallable(() -> {
                    databaseRepository.saveDevice(device);
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    void updateDevice(final Long _id, final String name, final String url) {
        Observable
                .fromCallable(() -> {
                    databaseRepository.updateDevice(_id, name, url);
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    void removeDevice(final String deviceId, final long _deviceId) {
        Observable
                .fromCallable(() -> {
                    databaseRepository.removeDevice(_deviceId);
                    prefRepository.cleanupDevicePrefs(deviceId);
                    return Irrelevant.INSTANCE;
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
