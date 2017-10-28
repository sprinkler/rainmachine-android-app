package com.rainmachine.presentation.screens.rainsensor;

import android.content.Context;

import com.rainmachine.R;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.infrastructure.util.BaseApplication;

class ItemRainOption {
    Provision.RainSensorSnoozeDuration snoozeDuration;
    int position;

    ItemRainOption(Provision.RainSensorSnoozeDuration snoozeDuration, int position) {
        this.snoozeDuration = snoozeDuration;
        this.position = position;
    }

    @Override
    public String toString() {
        Context context = BaseApplication.getContext();
        switch (snoozeDuration) {
            case RESUME:
                return context.getString(R.string.rain_sensor_stop);
            case UNTIL_MIDNIGHT:
                return context.getString(R.string.rain_sensor_stop_midnight);
            case SNOOZE_6_HOURS:
                return context.getString(R.string.rain_sensor_stop_6);
            case SNOOZE_12_HOURS:
                return context.getString(R.string.rain_sensor_stop_12);
            case SNOOZE_24_HOURS:
                return context.getString(R.string.rain_sensor_stop_24);
            case SNOOZE_48_HOURS:
                return context.getString(R.string.rain_sensor_stop_48);
        }
        return context.getString(R.string.rain_sensor_stop);
    }
}
