package com.rainmachine.presentation.screens.raindelay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RainDelayActivity extends SprinklerActivity implements RainDelayContract.Container {

    @Inject
    RainDelayContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static void start(Context context) {
        Intent intent = new Intent(context, RainDelayActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_rain_delay);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_snooze);
    }

    public Object getModule() {
        return new RainDelayModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void closeScreen() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
