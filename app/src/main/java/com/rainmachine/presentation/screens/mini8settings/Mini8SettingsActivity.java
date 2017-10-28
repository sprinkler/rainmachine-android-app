package com.rainmachine.presentation.screens.mini8settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.dialogs.InputNumberDialogFragment;
import com.rainmachine.presentation.screens.wateringduration.WateringDurationActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Mini8SettingsActivity extends SprinklerActivity implements Mini8SettingsContract
        .Container {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, Mini8SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_mini8_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(device.name);
    }

    public Object getModule() {
        return new Mini8SettingsModule(this);
    }

    @Override
    public void showMinLedBrightnessDialog(int minLedBrightness) {
        DialogFragment dialog = InputNumberDialogFragment.newInstance(Mini8SettingsContract
                        .DIALOG_ID_MIN_LED_BRIGHTNESS,
                getString(R.string.mini8_settings_min_led_brightness),
                getString(R.string.all_save),
                minLedBrightness,
                getString(R.string.mini8_settings_brightness_interval),
                0, 255);
        showDialogSafely(dialog);
    }

    @Override
    public void showMaxLedBrightnessDialog(int maxLedBrightness) {
        DialogFragment dialog = InputNumberDialogFragment.newInstance(Mini8SettingsContract
                        .DIALOG_ID_MAX_LED_BRIGHTNESS,
                getString(R.string.mini8_settings_max_led_brightness),
                getString(R.string.all_save),
                maxLedBrightness,
                getString(R.string.mini8_settings_brightness_interval),
                0, 255);
        showDialogSafely(dialog);
    }

    @Override
    public void showTouchSleepTimeoutDialog(int touchSleepTimeout) {
        DialogFragment dialog = InputNumberDialogFragment.newInstance(Mini8SettingsContract
                        .DIALOG_ID_TOUCH_SLEEP_TIMEOUT,
                getString(R.string.mini8_settings_touch_sleep_timeout),
                getString(R.string.all_save),
                touchSleepTimeout,
                getString(R.string.all_seconds),
                0, 999);
        showDialogSafely(dialog);
    }

    @Override
    public void showTouchLongPressTimeoutDialog(int touchLongPressTimeout) {
        DialogFragment dialog = InputNumberDialogFragment.newInstance(Mini8SettingsContract
                        .DIALOG_ID_TOUCH_LONG_PRESS_TIMEOUT,
                getString(R.string.mini8_settings_touch_long_press_timeout),
                getString(R.string.all_save),
                touchLongPressTimeout,
                getString(R.string.all_seconds),
                0, 999);
        showDialogSafely(dialog);
    }

    @Override
    public void showProgramsDialog(List<TouchProgramViewModel> programs, TouchProgramViewModel
            touchProgramToRun) {
        DialogFragment dialog = Mini8SettingsProgramsDialogFragment.newInstance(programs,
                touchProgramToRun);
        showDialogSafely(dialog);
    }

    @Override
    public void goToManualWateringDurationScreen() {
        startActivity(WateringDurationActivity.getStartIntent(this));
    }
}
