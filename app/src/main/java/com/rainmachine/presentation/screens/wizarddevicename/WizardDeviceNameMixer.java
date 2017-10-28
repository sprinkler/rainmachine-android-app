package com.rainmachine.presentation.screens.wizarddevicename;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.SprinklerState;

import io.reactivex.Observable;

class WizardDeviceNameMixer {

    private Device device;
    private SprinklerRepositoryImpl sprinklerRepository;
    private DatabaseRepositoryImpl databaseRepository;
    private DeviceNameStore deviceNameStore;
    private SprinklerState sprinklerState;

    WizardDeviceNameMixer(Device device, SprinklerRepositoryImpl sprinklerRepository,
                          DatabaseRepositoryImpl databaseRepository,
                          DeviceNameStore deviceNameStore,
                          SprinklerState sprinklerState) {
        this.device = device;
        this.sprinklerRepository = sprinklerRepository;
        this.databaseRepository = databaseRepository;
        this.deviceNameStore = deviceNameStore;
        this.sprinklerState = sprinklerState;
    }

    Observable<WizardDeviceNameViewModel> refresh() {
        return Observable.fromCallable(() -> databaseRepository.getCloudInfoList())
                .filter(cloudInfoList -> cloudInfoList.size() > 0)
                .map(cloudInfoList -> {
                    WizardDeviceNameViewModel viewModel = new WizardDeviceNameViewModel();
                    viewModel.preFillPassword = cloudInfoList.get(0).password;
                    return viewModel;
                });
    }

    Observable<Irrelevant> saveDeviceAndPassword(final String deviceName, final
    String oldPass, final String newPass) {
        return sprinklerRepository
                .saveDeviceName(deviceName).toObservable()
                .doOnNext(irrelevant -> {
                    device.name = deviceName;
                    deviceNameStore.publish(device.name);
                })
                .flatMap(irrelevant -> sprinklerRepository.changePassword((oldPass == null ? "" :
                        oldPass), newPass).toObservable())
                .doOnNext(irrelevant -> sprinklerState.keepPasswordForLater(newPass))
                .compose(RunToCompletion.instance());
    }
}
