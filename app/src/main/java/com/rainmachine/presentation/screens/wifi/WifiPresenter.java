package com.rainmachine.presentation.screens.wifi;

import com.rainmachine.R;
import com.rainmachine.domain.model.WifiSettings;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.bus.BaseEvent;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

class WifiPresenter extends BasePresenter<WifiContract.View> implements WifiContract
        .Presenter, ActionMessageDialogFragment.Callback, InfoMessageDialogFragment.Callback {

    private static final int DIALOG_ID_WIFI_AUTH_FAILURE = 0;
    private static final int DIALOG_ID_WIFI_NO_UDP_RESPONSE = 1;

    private WifiContract.Container container;
    private Bus bus;
    private WifiMixer mixer;

    private final CompositeDisposable disposables;

    WifiPresenter(WifiContract.Container container, Bus bus, WifiMixer mixer) {
        this.container = container;
        this.bus = bus;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WifiContract.View view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
        bus.register(this);
    }

    @Override
    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        bus.unregister(this);
        disposables.clear();
    }

    @Subscribe
    public void onSetSprinklerWifiEvent(WifiMixer.SetSprinklerWifiEvent event) {
        Timber.d("Received set sprinkle wifi event %d", event.type);
        switch (event.type) {
            case BaseEvent.EVENT_TYPE_SUCCESS:
                if (container.isWizard()) {
                    if (container.isMiniWizard()) {
                        container.goToPasswordScreen();
                    } else {
                        container.goToSprinklerDelegateScreen();
                    }
                    container.closeScreen();
                } else {
                    Toasts.show(R.string.wifi_success_connecting_wifi);
                    InfrastructureUtils.finishAllSprinklerActivities();
                }
                break;
            case BaseEvent.EVENT_TYPE_PROGRESS:
                view.showProgress();
                container.showProgress();
                break;
            case BaseEvent.EVENT_TYPE_ERROR:
                if (event.isAuthenticationError) {
                    view.showContent();
                    container.showContent();
                    container.showWifiAuthFailureDialog(DIALOG_ID_WIFI_AUTH_FAILURE, event.ssid);
                } else if (event.noUdpResponse) {
                    if (container.canShowDialogs()) {
                        container.showNoUDPResponseDialog(DIALOG_ID_WIFI_NO_UDP_RESPONSE, event
                                .ssid);
                    } else {
                        InfrastructureUtils.finishAllSprinklerActivities();
                    }
                } else {
                    InfrastructureUtils.finishAllSprinklerActivities();
                }
                break;
        }
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        if (container.isMiniWizard()) {
            container.goToPasswordScreen();
        } else {
            container.goToDeviceNameScreen(container.shouldShowOldPassInput());
        }
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogInfoMessageClick(int dialogId) {
        if (dialogId == DIALOG_ID_WIFI_NO_UDP_RESPONSE) {
            InfrastructureUtils.finishAllSprinklerActivities();
        }
    }

    @Override
    public void onDialogInfoMessageCancel(int dialogId) {
        if (dialogId == DIALOG_ID_WIFI_NO_UDP_RESPONSE) {
            InfrastructureUtils.finishAllSprinklerActivities();
        }
    }

    @Override
    public void onClickWifi(WifiItemViewModel wifiItemViewModel) {
        if (wifiItemViewModel.isEncrypted) {
            container.showWifiPasswordDialog(wifiItemViewModel);
        } else {
            WifiSettings wifiSettings = new WifiSettings();
            wifiSettings.sSID = wifiItemViewModel.sSID;
            wifiSettings.password = null;
            wifiSettings.isWEP = wifiItemViewModel.isWEP;
            wifiSettings.isWPA = wifiItemViewModel.isWPA;
            wifiSettings.isWPA2 = wifiItemViewModel.isWPA2;
            wifiSettings.networkType = WifiSettings.NETWORK_TYPE_DHCP;
            mixer.setSprinklerWifi(wifiSettings);
        }
    }

    @Override
    public void onClickConnectWifi(String ssid, String password, int positionSecurity,
                                   int networkType, String ipAddress, String netmask,
                                   String gateway, String dns) {
        WifiSettings wifiSettings = new WifiSettings();
        wifiSettings.sSID = ssid;
        wifiSettings.password = password;
        if (positionSecurity == 1) {
            wifiSettings.isWEP = true;
        } else if (positionSecurity == 2) {
            wifiSettings.isWPA = true;
        } else if (positionSecurity == 3) {
            wifiSettings.isWPA2 = true;
        }
        wifiSettings.networkType = networkType;
        wifiSettings.ipAddress = ipAddress;
        wifiSettings.netmask = netmask;
        wifiSettings.gateway = gateway;
        wifiSettings.dns = dns;
        mixer.setSprinklerWifi(wifiSettings);
    }

    @Override
    public void onClickConnectWifi(WifiItemViewModel wifiItemViewModel, String password) {
        WifiSettings wifiSettings = new WifiSettings();
        wifiSettings.sSID = wifiItemViewModel.sSID;
        wifiSettings.password = password;
        wifiSettings.isWEP = wifiItemViewModel.isWEP;
        wifiSettings.isWPA = wifiItemViewModel.isWPA;
        wifiSettings.isWPA2 = wifiItemViewModel.isWPA2;
        wifiSettings.networkType = WifiSettings.NETWORK_TYPE_DHCP;
        mixer.setSprinklerWifi(wifiSettings);
    }

    @Override
    public void onClickRefresh() {
        refresh();
    }

    @Override
    public void onClickSkip() {
        container.showSkipDialog();
    }

    @Override
    public void onClickAddNetwork() {
        container.showAddNetworkDialog(container.isWizard());
    }

    private void refresh() {
        view.showProgress();
        container.showProgress();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WifiViewModel> {

        @Override
        public void onNext(WifiViewModel viewModel) {
            view.render(viewModel);
            view.showContent();
            container.showContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            container.closeScreen();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
