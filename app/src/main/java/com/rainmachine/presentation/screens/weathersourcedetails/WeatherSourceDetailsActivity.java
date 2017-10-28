package com.rainmachine.presentation.screens.weathersourcedetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherSourceDetailsActivity extends SprinklerActivity {

    static final int REQ_CODE_NEARBY_STATIONS = 0;

    public static final String EXTRA_PARSER_ID = "extra_parser_id";
    public static final String EXTRA_STATION_NAME = "extra_station_name";

    @Inject
    WeatherSourceDetailsPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View customActionBarView;

    public static Intent getStartIntent(Context context, long parserId) {
        Intent intent = new Intent(context, WeatherSourceDetailsActivity.class);
        intent.putExtra(EXTRA_PARSER_ID, parserId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_weather_source_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
        buildCustomActionBarView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_NEARBY_STATIONS && resultCode == Activity.RESULT_OK) {
            presenter.onComingBackFromNearbyStations(data.getStringExtra(EXTRA_STATION_NAME));
        }
    }

    public Object getModule() {
        return new WeatherSourceDetailsModule(this);
    }

    private void buildCustomActionBarView() {
        customActionBarView = View.inflate(getSupportActionBar().getThemedContext(), R.layout
                .include_actionbar_discard_save_refresh, null);
        updateCustomActionBarButtons(true);
        customActionBarView.findViewById(R.id.actionbar_save).setOnClickListener(
                v -> presenter.onClickSave());
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                v -> presenter.onClickDiscard());
        ButterKnife.findById(customActionBarView, R.id.actionbar_refresh)
                .setOnClickListener(v -> presenter.onClickRefreshNow());
        // Show the custom action bar view and hide the normal Home icon and title.
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void updateCustomActionBarButtons(boolean canBeRefreshed) {
        TextView tvRefresh = ButterKnife.findById(customActionBarView, R.id.tv_refresh);
        tvRefresh.setEnabled(canBeRefreshed);
        tvRefresh.setCompoundDrawablesWithIntrinsicBounds(canBeRefreshed ? R.drawable
                .ic_autorenew_24dp : R.drawable.ic_autorenew_24dp_disabled, 0, 0, 0);
        View viewRefresh = ButterKnife.findById(customActionBarView, R.id.actionbar_refresh);
        viewRefresh.setEnabled(canBeRefreshed);
    }

    public void showActionBarView() {
        if (customActionBarView != null) {
            customActionBarView.setVisibility(View.VISIBLE);
        }
    }

    public void hideActionBarView() {
        if (customActionBarView != null) {
            customActionBarView.setVisibility(View.INVISIBLE);
        }
    }
}
