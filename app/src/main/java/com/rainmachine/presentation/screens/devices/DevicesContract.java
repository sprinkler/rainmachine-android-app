package com.rainmachine.presentation.screens.devices;

import com.rainmachine.data.local.database.model.Device;

import java.util.List;

interface DevicesContract {

    interface View {

        void setup();

        void render(List<Device> data);

        void updateCurrentWifiMac(String currentWifiMac);

        void showEmptyScreen();
    }

    interface Container {

        void goToDeviceScreen();

        void goToOfflineScreen(long _deviceDatabaseId, String deviceName);
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View> {

        void start();

        void stop();

        void onClickRefreshManual();

        void onClickDevice(Device device);
    }
}
