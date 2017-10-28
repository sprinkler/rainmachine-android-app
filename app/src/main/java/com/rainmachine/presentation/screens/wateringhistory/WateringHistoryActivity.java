package com.rainmachine.presentation.screens.wateringhistory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WateringHistoryActivity extends SprinklerActivity {

    @Inject
    WateringHistoryPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.spinner)
    Spinner spinner;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WateringHistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_watering_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        getSupportActionBar().setSubtitle(R.string.all_watering_history);

        setup();
    }

    public Object getModule() {
        return new WateringHistoryModule(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @OnClick(R.id.btn_export)
    void onClickExport() {
        presenter.onClickExport();
    }

    private void setup() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.watering_history_interval, R.layout.item_spinner_toolbar);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_toolbar);
        spinner.setAdapter(adapter);

        SpinnerInteractionListener listener = new SpinnerInteractionListener();
        spinner.setOnTouchListener(listener);
        spinner.setOnItemSelectedListener(listener);
    }

    public void showContent() {
        supportInvalidateOptionsMenu();
        spinner.setVisibility(View.VISIBLE);
        spinner.setSelection(0);
    }

    public void showProgress() {
        supportInvalidateOptionsMenu();
        spinner.setVisibility(View.INVISIBLE);
    }

    public void showError() {
        supportInvalidateOptionsMenu();
        spinner.setVisibility(View.INVISIBLE);
    }

    private class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View
            .OnTouchListener {

        private boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userSelect) {
                presenter.onSelectedInterval(WateringHistoryInterval.values()[pos]);
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // do nothing
        }
    }
}
