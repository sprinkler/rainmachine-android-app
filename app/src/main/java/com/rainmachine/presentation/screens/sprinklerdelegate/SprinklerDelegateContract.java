package com.rainmachine.presentation.screens.sprinklerdelegate;

import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;

interface SprinklerDelegateContract {

    interface View {
        void showWifiWarningDialog();

        void goToSystemWifiSettingsScreen();

        void goToMainScreen();

        void goToLoginScreen();

        void goToPhysicalTouchScreen();

        void goToDeviceNameScreen(boolean showOldPassInput);

        void goToWifiScreen(boolean showOldPassInput, boolean isMiniWizard);

        void closeScreen();

        void closeScreenWithoutTrace();
    }

    interface Presenter extends com.rainmachine.presentation.util.Presenter<View>,
            ActionMessageDialogFragment.Callback {

        void start();

        void stop();
    }
}
