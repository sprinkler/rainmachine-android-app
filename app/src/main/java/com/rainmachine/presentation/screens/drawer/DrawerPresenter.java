package com.rainmachine.presentation.screens.drawer;

import com.rainmachine.BuildConfig;
import com.rainmachine.presentation.activities.DrawerActivity;
import com.rainmachine.presentation.screens.cloudaccounts.CloudAccountsActivity;
import com.rainmachine.presentation.screens.devices.DevicesActivity;
import com.rainmachine.presentation.screens.networksettings.NetworkSettingsActivity;
import com.rainmachine.presentation.util.BasePresenter;

public class DrawerPresenter extends BasePresenter<DrawerView> {

    private DrawerActivity activity;
    private int currentScreen;

    public DrawerPresenter(DrawerActivity activity, int currentScreen) {
        this.activity = activity;
        this.currentScreen = currentScreen;
    }

    @Override
    public void attachView(DrawerView view) {
        super.attachView(view);

        view.render("v" + BuildConfig.VERSION_NAME);
    }

    public void onClickDevices() {
        if (currentScreen != 0) {
            activity.onNewActivity();
            activity.startActivity(DevicesActivity.getStartIntent(activity, false));
            activity.finish();
        } else {
            activity.onCloseDrawer();
        }
    }

    public void onClickCloudAccounts() {
        if (currentScreen != 1) {
            activity.onNewActivity();
            activity.startActivity(CloudAccountsActivity.getStartIntent(activity));
            if (currentScreen != 0) {
                activity.finish();
            }
        } else {
            activity.onCloseDrawer();
        }
    }

    public void onClickNetworkSettings() {
        if (currentScreen != 2) {
            activity.onNewActivity();
            activity.startActivity(NetworkSettingsActivity.getStartIntent(activity));
            if (currentScreen != 0) {
                activity.finish();
            }
        } else {
            activity.onCloseDrawer();
        }
    }
}
