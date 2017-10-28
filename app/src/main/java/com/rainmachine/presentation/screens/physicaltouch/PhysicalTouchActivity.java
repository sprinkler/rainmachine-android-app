package com.rainmachine.presentation.screens.physicaltouch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhysicalTouchActivity extends SprinklerActivity {

    @Inject
    PhysicalTouchPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, PhysicalTouchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_physical_touch);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.physical_touch_tap_device);
    }

    public Object getModule() {
        return new PhysicalTouchModule(this);
    }

    @Override
    public void onResume() {
        sprinklerState.setInitialSetup(true);
        super.onResume();
        presenter.start();
    }

    @Override
    public void onPause() {
        presenter.stop();
        super.onPause();
        sprinklerState.setInitialSetup(false);
    }
}
