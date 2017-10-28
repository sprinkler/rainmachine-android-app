package com.rainmachine.presentation.screens.networksettings;

import com.rainmachine.presentation.screens.directaccess.DirectAccessActivity;
import com.rainmachine.presentation.util.BasePresenter;

class NetworkSettingsPresenter extends BasePresenter<NetworkSettingsView> {

    private NetworkSettingsActivity activity;
    private NetworkSettingsMixer mixer;

    private NetworkSettingsViewModel viewModel;

    NetworkSettingsPresenter(NetworkSettingsActivity activity, NetworkSettingsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
    }

    @Override
    public void attachView(NetworkSettingsView view) {
        super.attachView(view);

        viewModel = mixer.refresh();
        view.render(viewModel.localDiscoveryEnabled);
    }

    public void onToggleLocalDiscovery(boolean isChecked) {
        viewModel.localDiscoveryEnabled = isChecked;
        mixer.saveLocalDiscovery(isChecked);
    }

    public void onClickLocalDiscovery() {
        viewModel.localDiscoveryEnabled = !viewModel.localDiscoveryEnabled;
        view.updateLocalDiscovery(viewModel.localDiscoveryEnabled);
        mixer.saveLocalDiscovery(viewModel.localDiscoveryEnabled);
    }

    public void onClickDirectAccess() {
        activity.startActivity(DirectAccessActivity.getStartIntent(activity));
    }
}
