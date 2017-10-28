package com.rainmachine.presentation.screens.softwareupdate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SoftwareUpdateActivity extends SprinklerActivity {

    @Inject
    SoftwareUpdatePresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SoftwareUpdateActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_software_update);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_software_update);
    }

    public Object getModule() {
        return new SoftwareUpdateModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }
}
