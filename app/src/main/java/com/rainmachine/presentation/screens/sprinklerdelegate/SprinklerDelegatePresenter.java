package com.rainmachine.presentation.screens.sprinklerdelegate;

import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

class SprinklerDelegatePresenter extends BasePresenter<SprinklerDelegateContract.View> implements
        SprinklerDelegateContract.Presenter, ActionMessageDialogFragment.Callback {

    private Bus bus;
    private SprinklerDelegateMixer mixer;

    private static boolean skipPoorWifiDetection;

    SprinklerDelegatePresenter(Bus bus, SprinklerDelegateMixer mixer) {
        this.bus = bus;
        this.mixer = mixer;
    }

    @Override
    public void start() {
        bus.register(this);
        mixer.makeDelegateDecision(skipPoorWifiDetection);
    }

    @Override
    public void stop() {
        bus.unregister(this);
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        view.goToSystemWifiSettingsScreen();
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        skipPoorWifiDetection = true;
        mixer.makeDelegateDecision(true);
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        view.closeScreen();
    }

    @Subscribe
    public void onDelegateDecision(SprinklerDelegateMixer.DelegateDecisionEvent event) {
        boolean finishScreen = true;
        switch (event.goToScreen) {
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_MAIN: {
                view.goToMainScreen();
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_WIFI: {
                view.goToWifiScreen(false, false);
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_WIFI_OLD_PASS: {
                view.goToWifiScreen(true, false);
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_WIFI_AND_PASSWORD: {
                view.goToWifiScreen(false, true);
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_LOGIN: {
                view.goToLoginScreen();
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_WIZARD: {
                view.goToDeviceNameScreen(false);
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_WIZARD_OLD_PASS: {
                view.goToDeviceNameScreen(true);
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_TO_PHYSICAL_TOUCH: {
                view.goToPhysicalTouchScreen();
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent
                    .GO_TO_SETTINGS_AND_DISABLE_POOR_NETWORK_AVOIDANCE: {
                view.showWifiWarningDialog();
                finishScreen = false;
                break;
            }
            case SprinklerDelegateMixer.DelegateDecisionEvent.GO_BACK: {
                // Do nothing because the activity will finish
                break;
            }
        }
        if (finishScreen) {
            view.closeScreenWithoutTrace();
        }
    }
}
