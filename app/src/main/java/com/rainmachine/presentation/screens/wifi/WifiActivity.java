package com.rainmachine.presentation.screens.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.sprinklerdelegate.SprinklerDelegateActivity;
import com.rainmachine.presentation.screens.wizarddevicename.WizardDeviceNameActivity;
import com.rainmachine.presentation.screens.wizardpassword.WizardPasswordActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiActivity extends SprinklerActivity implements WifiContract.Container {

    public static final String EXTRA_IS_MINI_WIZARD = "extra_is_mini_wizard";

    @Inject
    WifiContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isInProgress;

    public static Intent getStartIntent(Context context, boolean showOldPassInput,
                                        boolean isWizard, boolean isMiniWizard) {
        Intent intent = new Intent(context, WifiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ExtraConstants.IS_WIZARD, isWizard);
        intent.putExtra(ExtraConstants.SHOW_OLD_PASS_INPUT, showOldPassInput);
        intent.putExtra(EXTRA_IS_MINI_WIZARD, isMiniWizard);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.wifi_connect_to_wifi));

        isInProgress = false;
        if (savedInstanceState != null) {
            isInProgress = savedInstanceState.getBoolean("isInProgress");
        }
    }

    public Object getModule() {
        return new WifiModule(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isInProgress", isInProgress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi, menu);
        MenuItem skipItem = menu.findItem(R.id.menu_skip);
        boolean isWizard = getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
        skipItem.setVisible(!isInProgress && isWizard);
        MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        refreshItem.setVisible(!isInProgress);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_skip) {
            presenter.onClickSkip();
            return true;
        } else if (id == R.id.menu_refresh) {
            presenter.onClickRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showProgress() {
        supportInvalidateOptionsMenu();
        isInProgress = true;
    }

    @Override
    public void showContent() {
        supportInvalidateOptionsMenu();
        isInProgress = false;
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void showSkipDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(0,
                getString(R.string.all_are_you_sure),
                getString(R.string.wifi_skip_wifi_description),
                getString(R.string.all_yes), getString(R.string.all_no));
        showDialogSafely(dialog);
    }

    @Override
    public void showWifiPasswordDialog(WifiItemViewModel wifiItemViewModel) {
        DialogFragment dialog = WifiSettingsDialogFragment.newInstance(wifiItemViewModel);
        showDialogSafely(dialog);
    }

    @Override
    public void showAddNetworkDialog(boolean isWizard) {
        DialogFragment dialog = WifiNetworkDialogFragment.newInstance(isWizard);
        showDialogSafely(dialog);
    }

    @Override
    public void showWifiAuthFailureDialog(int dialogId, String ssid) {
        DialogFragment dialog = InfoMessageDialogFragment.newInstance(dialogId,
                getString(R.string.wifi_error_authentication_title),
                getString(R.string.wifi_error_authentication, ssid),
                getString(R.string.all_ok));
        showDialogSafely(dialog);
    }

    @Override
    public void showNoUDPResponseDialog(int dialogId, String ssid) {
        DialogFragment dialog = InfoMessageDialogFragment.newInstance(dialogId, null, getString(R
                .string.wifi_error_udp, ssid), getString(R.string.all_ok));
        showDialogSafely(dialog);
    }

    @Override
    public void goToPasswordScreen() {
        startActivity(WizardPasswordActivity.getStartIntent(this));
    }

    @Override
    public void goToDeviceNameScreen(boolean shouldShowOldPassInput) {
        startActivity(WizardDeviceNameActivity.getStartIntent(this, shouldShowOldPassInput));
    }

    @Override
    public void goToSprinklerDelegateScreen() {
        startActivity(SprinklerDelegateActivity.getStartIntent(this));
    }

    @Override
    public boolean isWizard() {
        return getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
    }

    @Override
    public boolean isMiniWizard() {
        return getIntent().getBooleanExtra(WifiActivity.EXTRA_IS_MINI_WIZARD, false);
    }

    @Override
    public boolean shouldShowOldPassInput() {
        return getIntent().getBooleanExtra(ExtraConstants.SHOW_OLD_PASS_INPUT, false);
    }
}
