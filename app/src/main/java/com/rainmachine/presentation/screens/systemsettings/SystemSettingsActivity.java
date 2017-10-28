package com.rainmachine.presentation.screens.systemsettings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SystemSettingsActivity extends SprinklerActivity {

    @Inject
    SystemSettingsPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SystemSettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_system_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_device_settings);
    }

    public Object getModule() {
        return new SystemSettingsModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }
}
