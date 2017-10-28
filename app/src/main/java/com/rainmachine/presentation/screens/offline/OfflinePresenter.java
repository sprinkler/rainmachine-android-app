package com.rainmachine.presentation.screens.offline;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.presentation.util.RunOnProperThreads;

class OfflinePresenter implements OfflineContract.Presenter {

    private DeviceRepository deviceRepository;
    private OfflineContract.View view;
    private OfflineExtra extra;

    OfflinePresenter(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void attachView(OfflineContract.View view) {
        this.view = view;
        extra = this.view.getExtra();
    }

    @Override
    public void init() {
        view.setTitle(extra.deviceName);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onClickForget() {
        deviceRepository.deleteDevice(extra._deviceDatabaseId)
                .toObservable()
                .compose(RunOnProperThreads.instance())
                .subscribe(ignored -> view.closeScreen());
    }
}
