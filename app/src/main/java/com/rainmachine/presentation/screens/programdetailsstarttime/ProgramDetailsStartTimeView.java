package com.rainmachine.presentation.screens.programdetailsstarttime;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsStartTimeView extends ScrollView implements
        ProgramDetailsStartTimeContract.View {

    @Inject
    ProgramDetailsStartTimeContract.Presenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.radio_sunrise_sunset)
    RadioButton radioSunriseSunset;
    @BindView(R.id.radio_time_of_day)
    RadioButton radioTimeOfDay;
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;

    public ProgramDetailsStartTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_program_details_start_time, this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            presenter.attachView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
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
    public void setup(Program program, boolean use24HourFormat) {
        if (program.startTime.isTimeOfDay()) {
            checkTimeOfDayRadio();
            updateStartTimeOfDay(program, use24HourFormat);
            updateStartTimeSunriseSunset(program);
        } else {
            checkSunriseSunsetRadio();
            updateStartTimeOfDay(program, use24HourFormat);
            updateStartTimeSunriseSunset(program);
        }
    }

    @Override
    public void updateSunriseSunset(Program program) {
        checkSunriseSunsetRadio();
        updateStartTimeSunriseSunset(program);
    }

    @Override
    public void updateStartTimeOfDay(Program program, boolean use24HourFormat) {
        String sTime = CalendarFormatter.hourMinColon(program.startTime.localDateTime.toLocalTime
                (), use24HourFormat);
        tvStartTime.setText(sTime);
    }

    @OnClick(R.id.card_time_of_day)
    public void onClickStartTimeOfDay() {
        checkTimeOfDayRadio();
        presenter.onClickStartTime();
    }

    @OnClick(R.id.card_sunrise_sunset)
    public void onClickStartTimeSunriseSunset() {
        presenter.onClickSunriseSunset();
    }

    private void checkTimeOfDayRadio() {
        radioTimeOfDay.setChecked(true);
        radioSunriseSunset.setChecked(false);
    }

    private void checkSunriseSunsetRadio() {
        radioSunriseSunset.setChecked(true);
        radioTimeOfDay.setChecked(false);
    }

    private void updateStartTimeSunriseSunset(Program program) {
        StringBuilder sb = new StringBuilder(15);
        // As requested, we show x minutes for the first time
        if (program.startTime.isTimeOfDay() && program.isNew()) {
            sb.append("x");
        } else {
            sb.append(program.startTime.offsetMinutes);
        }
        sb.append(" ")
                .append(getContext().getString(R.string.all_minutes))
                .append(" ");
        if (program.startTime.isBefore()) {
            sb.append(getContext().getString(R.string.all_before));
        } else {
            sb.append(getContext().getString(R.string.all_after));
        }
        sb.append(" ");
        if (program.startTime.isSunrise()) {
            sb.append(getContext().getString(R.string.all_sunrise));
        } else {
            sb.append(getContext().getString(R.string.all_sunset));
        }
        radioSunriseSunset.setText(sb.toString());
    }
}
