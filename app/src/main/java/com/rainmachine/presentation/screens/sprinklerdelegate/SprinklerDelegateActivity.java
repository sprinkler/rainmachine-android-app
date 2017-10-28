package com.rainmachine.presentation.screens.sprinklerdelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.screens.login.LoginActivity;
import com.rainmachine.presentation.screens.main.MainActivity;
import com.rainmachine.presentation.screens.physicaltouch.PhysicalTouchActivity;
import com.rainmachine.presentation.screens.wifi.WifiActivity;
import com.rainmachine.presentation.screens.wizarddevicename.WizardDeviceNameActivity;
import com.rainmachine.presentation.util.IntentUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SprinklerDelegateActivity extends SprinklerActivity implements
        SprinklerDelegateContract.View {

    @Inject
    SprinklerDelegateContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, SprinklerDelegateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprinkler_delegate);
        ButterKnife.bind(this);

        overridePendingTransition(0, 0);
        if (!buildGraphAndInject()) {
            return;
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);

        presenter.attachView(this);
    }

    @Override
    public void onResume() {
        sprinklerState.setInitialSetup(true);
        super.onResume();
        presenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        sprinklerState.setInitialSetup(false);
        presenter.stop();
    }

    public Object getModule() {
        return new SprinklerDelegateModule();
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void closeScreenWithoutTrace() {
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void goToSystemWifiSettingsScreen() {
        Intent intent = IntentUtils.newAdvancedWifiIntent();
        if (IntentUtils.activityExists(intent)) {
            startActivity(intent);
        }
    }

    @Override
    public void goToMainScreen() {
        startActivity(MainActivity.getStartIntent(this, false));
    }

    @Override
    public void goToLoginScreen() {
        startActivity(LoginActivity.getStartIntent(this));
    }

    @Override
    public void goToPhysicalTouchScreen() {
        startActivity(PhysicalTouchActivity.getStartIntent(this));
    }

    @Override
    public void goToDeviceNameScreen(boolean showOldPassInput) {
        startActivity(WizardDeviceNameActivity.getStartIntent(this, showOldPassInput));
    }

    @Override
    public void goToWifiScreen(boolean showOldPassInput, boolean isMiniWizard) {
        startActivity(WifiActivity.getStartIntent(this, showOldPassInput, true, isMiniWizard));
    }

    @Override
    public void showWifiWarningDialog() {
        DialogFragment dialog = ActionMessageDialogFragment.newInstance(0,
                getString(R.string.sprinkler_delegate_poor_network_title),
                getString(R.string.sprinkler_delegate_poor_network_description),
                getString(R.string.sprinkler_delegate_go_to_settings),
                getString(R.string.sprinkler_delegate_lucky));
        showDialogSafely(dialog);
    }
}
