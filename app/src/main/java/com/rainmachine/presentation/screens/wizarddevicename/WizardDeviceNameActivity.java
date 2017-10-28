package com.rainmachine.presentation.screens.wizarddevicename;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WizardDeviceNameActivity extends SprinklerActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, boolean showOldPassInput) {
        Intent intent = new Intent(context, WizardDeviceNameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ExtraConstants.IS_WIZARD, true);
        intent.putExtra(ExtraConstants.SHOW_OLD_PASS_INPUT, showOldPassInput);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_wizard_device_name);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.all_rainmachine_authentication);
        getSupportActionBar().setSubtitle(R.string.all_wizard);
    }

    public Object getModule() {
        return new WizardDeviceNameModule(this);
    }
}
