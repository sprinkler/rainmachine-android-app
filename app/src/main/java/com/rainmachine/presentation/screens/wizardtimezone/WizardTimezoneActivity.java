package com.rainmachine.presentation.screens.wizardtimezone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WizardTimezoneActivity extends SprinklerActivity {

    @Inject
    WizardTimezonePresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, WizardTimezoneActivity.class);
        intent.putExtra(ExtraConstants.IS_WIZARD, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_wizard_timezone);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.wizard_timezone_set_date_time));
    }

    public Object getModule() {
        return new WizardTimezoneModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }
}
