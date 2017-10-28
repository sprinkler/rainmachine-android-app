package com.rainmachine.presentation.screens.locationsettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.screens.location.LocationActivity;
import com.rainmachine.presentation.util.ExtraConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocationSettingsActivity extends SprinklerActivity {

    private static final int REQ_CODE_SET_LOCATION = 1;

    @BindView(R.id.tv_sprinkler_address)
    TextView tvSprinklerAddress;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context, String sprinklerAddress) {
        Intent intent = new Intent(context, LocationSettingsActivity.class);
        intent.putExtra(ExtraConstants.IS_WIZARD, false);
        intent.putExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS, sprinklerAddress);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_settings);
        ButterKnife.bind(this);

        if (!buildGraphAndInject()) {
            return;
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(getString(R.string.location_settings_subtitle));

        updateViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SET_LOCATION && resultCode == Activity.RESULT_OK) {
            String address = data.getStringExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS);
            getIntent().putExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS, address);
            updateViews();
        }
    }

    public Object getModule() {
        return new LocationSettingsModule(this);
    }

    @OnClick(R.id.btn_change)
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_change) {
            String address = getIntent().getStringExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS);
            startActivityForResult(LocationActivity.getStartIntent(this, false, address),
                    REQ_CODE_SET_LOCATION);
        }
    }

    public void updateViews() {
        String address = getIntent().getStringExtra(LocationActivity.EXTRA_SPRINKLER_ADDRESS);
        tvSprinklerAddress.setText(address);
    }
}
