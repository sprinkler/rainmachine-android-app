package com.rainmachine.presentation.screens.weathersensitivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherSensitivityView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WeatherSensitivityPresenter presenter;

    @BindView(R.id.check_use_correction)
    CheckBox checkUseCorrection;

    public WeatherSensitivityView(Context context, AttributeSet attrs) {
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_use_correction) {
            presenter.onCheckedChangedUseCorrection(isChecked);
        }
    }

    @OnClick({R.id.card_use_correction, R.id.card_rain_sensitivity, R.id.card_wind_sensitivity})
    void onClick(View view) {
        int id = view.getId();
        if (id == R.id.card_use_correction) {
            checkUseCorrection.toggle();
        } else if (id == R.id.card_rain_sensitivity) {
            presenter.onClickRainSensitivity();
        } else if (id == R.id.card_wind_sensitivity) {
            presenter.onClickWindSensitivity();
        }
    }

    void render(WeatherSensitivityViewModel viewModel) {
        checkUseCorrection.setOnCheckedChangeListener(null);
        checkUseCorrection.setChecked(viewModel.useCorrection);
        checkUseCorrection.setOnCheckedChangeListener(this);
    }

    void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
