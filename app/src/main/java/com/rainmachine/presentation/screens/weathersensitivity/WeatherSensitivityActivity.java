package com.rainmachine.presentation.screens.weathersensitivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSensitivityActivity extends SprinklerActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WeatherSensitivityActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_weather_sensitivity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_weather_sensitivity);
    }

    public Object getModule() {
        return new WeatherSensitivityModule(this);
    }
}
