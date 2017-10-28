package com.rainmachine.presentation.screens.weathersources;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSourcesActivity extends SprinklerActivity {

    @Inject
    WeatherSourcesPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WeatherSourcesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_weather_sources);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_add_weather_service);
    }

    public Object getModule() {
        return new WeatherSourcesModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }
}
