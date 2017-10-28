package com.rainmachine.presentation.screens.wifi;

import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;

interface WifiContract {

    interface View {

        void setup();

        void showProgress();

        void showContent();

        void render(WifiViewModel viewModel);
    }

    interface Container {

        boolean isWizard();

        boolean isMiniWizard();

        boolean shouldShowOldPassInput();

        void showProgress();

        void showContent();

        void closeScreen();

        void showSkipDialog();

        void showWifiPasswordDialog(WifiItemViewModel wifiItemViewModel);

        void showAddNetworkDialog(boolean isWizard);

        void showWifiAuthFailureDialog(int dialogId, String ssid);

        void showNoUDPResponseDialog(int dialogId, String ssid);

        void goToPasswordScreen();

        void goToDeviceNameScreen(boolean shouldShowOldPassInput);

        void goToSprinklerDelegateScreen();

        boolean canShowDialogs();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback, InfoMessageDialogFragment.Callback {

        void start();

        void onClickWifi(WifiItemViewModel wifiItemViewModel);

        void onClickAddNetwork();

        void onClickSkip();

        void onClickRefresh();

        void onClickConnectWifi(String ssid, String password, int positionSecurity, int
                networkType, String ipAddress, String netmask, String gateway, String dns);

        void onClickConnectWifi(WifiItemViewModel wifiItemViewModel, String password);
    }
}
