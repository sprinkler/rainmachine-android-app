package com.rainmachine.presentation.screens.mini8settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Mini8SettingsView extends ViewFlipper implements Mini8SettingsContract.View,
        CompoundButton.OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    Mini8SettingsContract.Presenter presenter;

    @BindView(R.id.tv_touch_program_subtitle)
    TextView tvTouchProgramSubtitle;
    @BindView(R.id.check_touch_advanced)
    CheckBox checkTouchAdvanced;
    @BindView(R.id.value_min_led_brightness)
    TextView tvMinLedBrightness;
    @BindView(R.id.value_max_led_brightness)
    TextView tvMaxLedBrightness;
    @BindView(R.id.tv_touch_sleep_timeout)
    TextView tvTouchSleepTimeout;
    @BindView(R.id.tv_touch_long_press_timeout)
    TextView tvTouchLongPressTimeout;
    /*@BindView(R.id.check_led_delay)
    CheckBox checkLedDelay;*/

    public Mini8SettingsView(Context context, AttributeSet attrs) {
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
        if (id == R.id.check_touch_advanced) {
            presenter.onCheckedChangedTouchAdvanced(isChecked);
        } /*else if (id == R.id.check_led_delay) {
            presenter.onCheckedChangedLedDelay(isChecked);
        }*/
    }

    @OnClick({R.id.view_touch_start_program, R.id.view_touch_advanced, /*R.id.view_led_delay,*/
            R.id.view_min_led_brightness, R.id.view_max_led_brightness,
            R.id.view_touch_sleep_timeout, R.id.view_touch_long_press_timeout, R.id.btn_retry,
            R.id.view_manual_watering_duration})
    void onClick(View view) {
        int id = view.getId();
        if (id == R.id.view_touch_start_program) {
            presenter.onClickTouchStartProgram();
        } else if (id == R.id.view_touch_advanced) {
            checkTouchAdvanced.toggle();
        } /*else if (id == R.id.view_led_delay) {
            checkLedDelay.toggle();
        }*/ else if (id == R.id.view_min_led_brightness) {
            presenter.onClickMinLedBrightness();
        } else if (id == R.id.view_max_led_brightness) {
            presenter.onClickMaxLedBrightness();
        } else if (id == R.id.view_touch_sleep_timeout) {
            presenter.onClickTouchSleepTimeout();
        } else if (id == R.id.view_touch_long_press_timeout) {
            presenter.onClickTouchLongPressTimeout();
        } else if (id == R.id.btn_retry) {
            presenter.onClickRetry();
        } else if (id == R.id.view_manual_watering_duration) {
            presenter.onClickManualWateringDuration();
        }
    }

    @Override
    public void render(Mini8SettingsViewModel viewModel) {
        tvTouchProgramSubtitle.setText(getContext().getString(R.string
                .mini8_settings_program_will_start, viewModel.touchProgramToRun.name));

        checkTouchAdvanced.setOnCheckedChangeListener(null);
        checkTouchAdvanced.setChecked(viewModel.touchAdvanced);
        checkTouchAdvanced.setOnCheckedChangeListener(this);

        /*checkLedDelay.setOnCheckedChangeListener(null);
        checkLedDelay.setChecked(viewModel.showRestrictionsOnLed);
        checkLedDelay.setOnCheckedChangeListener(this);*/

        tvMinLedBrightness.setText(String.valueOf(viewModel.minLedBrightness));
        tvMaxLedBrightness.setText(String.valueOf(viewModel.maxLedBrightness));

        tvTouchSleepTimeout.setText(getResources().getQuantityString(R.plurals
                .mini8_settings_x_seconds, viewModel.touchSleepTimeout, viewModel
                .touchSleepTimeout));
        tvTouchLongPressTimeout.setText(getResources().getQuantityString(R.plurals
                .mini8_settings_x_seconds, viewModel.touchLongPressTimeout, viewModel
                .touchLongPressTimeout));
    }

    @Override
    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
