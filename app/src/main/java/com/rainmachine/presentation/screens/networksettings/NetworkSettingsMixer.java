package com.rainmachine.presentation.screens.networksettings;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.util.Irrelevant;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

class NetworkSettingsMixer {

    private DeviceRepository deviceRepository;
    private PrefRepository prefRepository;

    NetworkSettingsMixer(DeviceRepository deviceRepository, PrefRepository
            prefRepository) {
        this.deviceRepository = deviceRepository;
        this.prefRepository = prefRepository;
    }

    NetworkSettingsViewModel refresh() {
        NetworkSettingsViewModel viewModel = new NetworkSettingsViewModel();
        viewModel.localDiscoveryEnabled = prefRepository.localDiscovery();
        return viewModel;
    }

    void saveLocalDiscovery(boolean isEnabled) {
        prefRepository.saveLocalDiscovery(isEnabled);
        if (!isEnabled) {
            Observable
                    .fromCallable(() -> {
                        deviceRepository.deleteAllLocalDiscoveredDevices();
                        return Irrelevant.INSTANCE;
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }
}
