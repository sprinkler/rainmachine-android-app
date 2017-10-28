package com.rainmachine.presentation.screens.rainsensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RainSensorActivity extends SprinklerActivity implements RainSensorContract.Container {

    @Inject
    RainSensorContract.Presenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static void start(Context context) {
        Intent intent = new Intent(context, RainSensorActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_rain_sensor);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_rain_sensor);
    }

    public Object getModule() {
        return new RainSensorModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showRainDetectedOptions(String[] items, int checkedItemPosition) {
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(0,
                getString(R.string.rain_sensor_while_detected), getString(R.string.all_ok),
                items, checkedItemPosition);
        showDialogSafely(dialog);
    }
}
