package com.rainmachine.presentation.screens.currentrestrictions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.hours.HoursActivity;
import com.rainmachine.presentation.screens.raindelay.RainDelayActivity;
import com.rainmachine.presentation.screens.rainsensor.RainSensorActivity;
import com.rainmachine.presentation.screens.restrictions.RestrictionsActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurrentRestrictionsActivity extends SprinklerActivity implements
        CurrentRestrictionsContract.Container {

    @Inject
    CurrentRestrictionsContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CurrentRestrictionsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_current_restrictions);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.current_restrictions_subtitle);
    }

    public Object getModule() {
        return new CurrentRestrictionsModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void goToRestrictionsScreen() {
        startActivity(RestrictionsActivity.getStartIntent(this));
    }

    @Override
    public void goToHoursScreen() {
        startActivity(HoursActivity.getStartIntent(this));
    }

    @Override
    public void goToRainSensorScreen() {
        RainSensorActivity.start(this);
    }

    @Override
    public void goToSnoozeScreen() {
        RainDelayActivity.start(this);
    }
}
