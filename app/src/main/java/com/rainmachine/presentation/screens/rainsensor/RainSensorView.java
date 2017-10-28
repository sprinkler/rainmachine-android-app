package com.rainmachine.presentation.screens.rainsensor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTime;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RainSensorView extends ViewFlipper implements RainSensorContract.View,
        CompoundButton.OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    private static final String SUPPORT_LINK = "https://support.rainmachine" +
            ".com/hc/en-us/articles/227832747";

    @Inject
    RainSensorContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;

    @BindView(R.id.toggle_rain_sensor)
    SwitchCompat toggleRainSensor;
    @BindView(R.id.view_rain_sensor_closed_parent)
    View viewRainSensorClosedParent;
    @BindView(R.id.view_rain_sensor_closed)
    View viewRainSensorClosed;
    @BindView(R.id.toggle_rain_sensor_closed)
    SwitchCompat toggleRainSensorClosed;
    @BindView(R.id.view_last_rain_event)
    View viewLastRainEvent;
    @BindView(R.id.tv_last_rain_event)
    TextView tvLastRainEvent;
    @BindView(R.id.view_rain_detected_parent)
    View viewRainDetectedParent;
    @BindView(R.id.view_rain_detected)
    View viewRainDetected;
    @BindView(R.id.tv_rain_option)
    TextView tvRainOption;
    @BindView(R.id.tv_rain_detected)
    View tvRainDetected;

    public RainSensorView(Context context, AttributeSet attrs) {
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
        if (id == R.id.toggle_rain_sensor) {
            presenter.onCheckedChangedRainSensorHardware(isChecked);
        } else if (id == R.id.toggle_rain_sensor_closed) {
            presenter.onCheckedChangedRainSensorClosed(isChecked);
        }
    }

    @OnClick({R.id.view_rain_sensor_option, R.id.view_rain_sensor_closed})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.view_rain_sensor_option) {
            presenter.onClickRainSensor();
        } else if (id == R.id.view_rain_sensor_closed) {
            toggleRainSensorClosed.toggle();
        }
    }

    @OnClick(R.id.view_rain_detected)
    void onClickRainDetected() {
        presenter.onClickRainDetectedOption();
    }

    @OnClick(R.id.view_help)
    public void onClickHelp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(SUPPORT_LINK));
        getContext().startActivity(intent);
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onRetry();
    }

    @Override
    public void setup(boolean showExtraFields) {
        if (showExtraFields) {
            viewLastRainEvent.setVisibility(View.VISIBLE);
            viewRainDetected.setVisibility(View.VISIBLE);
        } else {
            viewLastRainEvent.setVisibility(View.GONE);
            viewRainDetected.setVisibility(View.GONE);
        }
    }

    @Override
    public void render(RainSensorViewModel viewModel) {
        toggleRainSensor.setOnCheckedChangeListener(null);
        toggleRainSensor.setChecked(viewModel.useRainSensor);
        toggleRainSensor.setOnCheckedChangeListener(this);

        toggleRainSensorClosed.setOnCheckedChangeListener(null);
        toggleRainSensorClosed.setChecked(viewModel.rainSensorNormallyClosed);
        toggleRainSensorClosed.setOnCheckedChangeListener(this);

        if (viewModel.rainSensorLastEvent != Provision.RainSensorLastEvent.NEVER) {
            DateTime dateTime = viewModel.rainSensorLastEvent.lastDateTime();
            tvLastRainEvent.setText(calendarFormatter.dayOfWeekMonthDay(dateTime) + ", " +
                    CalendarFormatter.hourMinColon(dateTime.toLocalTime(), viewModel
                            .use24HourFormat));
        } else {
            tvLastRainEvent.setText(R.string.rain_sensor_never);
        }

        tvRainOption.setText(viewModel.rainDetectedOption.toString());

        if (viewModel.useRainSensor) {
            viewRainSensorClosedParent.setVisibility(View.VISIBLE);
            viewLastRainEvent.setVisibility(viewModel.showExtraFields ? View.VISIBLE : View.GONE);
            viewRainDetectedParent.setVisibility(viewModel.showExtraFields ? View.VISIBLE : View
                    .GONE);
        } else {
            viewRainSensorClosedParent.setVisibility(View.GONE);
            viewLastRainEvent.setVisibility(View.GONE);
            viewRainDetectedParent.setVisibility(View.GONE);
        }
    }

    @Override
    public void render(RainDetectedViewModel viewModel) {
        tvRainDetected.setVisibility(viewModel.rainDetected ? View.VISIBLE : View.GONE);
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