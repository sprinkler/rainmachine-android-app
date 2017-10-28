package com.rainmachine.presentation.screens.zonedetails;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ScrollView;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ZoneDetailsWeatherView extends ScrollView implements CompoundButton
        .OnCheckedChangeListener {

    @Inject
    ZoneDetailsWeatherPresenter presenter;

    @BindView(R.id.toggle_weather_data)
    SwitchCompat toggleWeatherData;
    @BindView(R.id.toggle_historical_data)
    SwitchCompat toggleHistoricalData;
    @BindView(R.id.toggle_adjust)
    SwitchCompat toggleAdjust;

    public ZoneDetailsWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            presenter.init();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int id = compoundButton.getId();
        if (id == R.id.toggle_historical_data) {
            presenter.onToggleHistoricalData(checked);
        } else if (id == R.id.toggle_weather_data) {
            presenter.onToggleWeatherData(checked);
        } else if (id == R.id.toggle_adjust) {
            presenter.onToggleAdjust(checked);
        }
    }

    public void toggleHistoricalData(boolean checked) {
        toggleHistoricalData.setOnCheckedChangeListener(null);
        toggleHistoricalData.setChecked(checked);
        toggleHistoricalData.setOnCheckedChangeListener(this);
    }

    public void toggleWeatherData(boolean checked) {
        toggleWeatherData.setOnCheckedChangeListener(null);
        toggleWeatherData.setChecked(checked);
        toggleWeatherData.setOnCheckedChangeListener(this);
    }

    public void toggleAdjust(boolean checked) {
        toggleAdjust.setOnCheckedChangeListener(null);
        toggleAdjust.setChecked(checked);
        toggleAdjust.setOnCheckedChangeListener(this);
    }

    public void updateContent(ZoneDetailsViewModel viewModel) {
        toggleHistoricalData(viewModel.zoneProperties.historicalAverage);
        toggleWeatherData(viewModel.zoneProperties.forecastData);
        toggleAdjust(!viewModel.zoneProperties.historicalAverage && !viewModel.zoneProperties
                .forecastData);
    }
}
