package com.rainmachine.presentation.screens.devices;

import android.os.Handler;
import android.os.Looper;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.NetworkUtils;
import com.rainmachine.infrastructure.bus.DeviceEvent;
import com.rainmachine.infrastructure.receivers.WifiBroadcastReceiver;
import com.rainmachine.injection.Injector;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.List;

class DevicesPresenter implements DevicesContract.Presenter {

    private final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private DevicesContract.Container container;
    private DevicesContract.View view;
    private Bus bus;
    private AppManager appManager;
    private DevicesMixer mixer;
    private PrefRepository prefRepository;

    DevicesPresenter(DevicesContract.Container container, Bus bus, AppManager
            appManager, DevicesMixer mixer, PrefRepository prefRepository) {
        this.container = container;
        this.bus = bus;
        this.appManager = appManager;
        this.mixer = mixer;
        this.prefRepository = prefRepository;
    }

    @Override
    public void attachView(DevicesContract.View view) {
        this.view = view;
        this.view.setup();
    }

    @Override
    public void init() {
        bus.register(this);
        refresh();
    }

    @Override
    public void start() {
        refresh();
        // If we come from a device, we need to clear the data routing set up for that device
        NetworkUtils.clearNetworkTrafficRouting();
        mixer.startScanning();
        // If we come from a device, remove the object graph so we don't use extra memory
        Injector.removeSprinklerGraph();
    }

    @Override
    public void stop() {
        mixer.stopScanning();
        MAIN_HANDLER.removeCallbacks(runShowEmpty);
    }

    @Override
    public void destroy() {
        bus.unregister(this);
    }

    @Override
    public void onClickDevice(Device device) {
        if (!device.isOffline) {
            Injector.buildSprinklerGraph(device);
            container.goToDeviceScreen();
        } else {
            container.goToOfflineScreen(device._id, device.name);
        }
    }

    @Override
    public void onClickRefreshManual() {
        mixer.refreshDevicesManually();
        view.render(new ArrayList<>());
    }

    @Subscribe
    public void onRefresh(DevicesMixer.RefreshEvent event) {
        view.render(event.data);
        view.updateCurrentWifiMac(event.currentWifiMac);
        // If x seconds pass and we have no devices, show empty message
        if (event.data.size() == 0) {
            MAIN_HANDLER.postDelayed(runShowEmpty, 2 * DateTimeConstants.MILLIS_PER_SECOND);
        } else {
            MAIN_HANDLER.removeCallbacks(runShowEmpty);
            prefRepository.saveShownAtLeastOneDeviceInThePast(true);
        }
    }

    @Subscribe
    public void onDeviceEvent(DeviceEvent event) {
        refresh();
    }

    @Subscribe
    public void onWifiEvent(WifiBroadcastReceiver.WifiNetworkEvent event) {
        view.updateCurrentWifiMac(event.mac);
    }

    private void refresh() {
        mixer.refresh();
    }

    private Runnable runShowEmpty = new Runnable() {

        @Override
        public void run() {
            if (!prefRepository.shownAtLeastOneDeviceInThePast()) {
                List<Device> devices = new ArrayList<>(1);
                devices.add(Device.demo());
                view.render(devices);
            } else {
                view.showEmptyScreen();
            }
        }
    };
}
