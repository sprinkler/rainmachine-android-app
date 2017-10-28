package com.rainmachine.presentation.screens.wizardtimezone;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WizardTimezoneView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    WizardTimezonePresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_timezone)
    TextView tvTimezone;

    public WizardTimezoneView(Context context, AttributeSet attrs) {
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

    @OnClick({R.id.tv_date, R.id.tv_time, R.id.tv_timezone, R.id.btn_save, R.id.btn_retry})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_date) {
            LocalDate date = (LocalDate) tvDate.getTag();
            presenter.onClickDate(date);
        } else if (id == R.id.tv_time) {
            LocalTime time = (LocalTime) tvTime.getTag();
            presenter.onClickTime(time);
        } else if (id == R.id.tv_timezone) {
            presenter.onClickTimezone();
        } else if (id == R.id.btn_save) {
            String timezone = (String) tvTimezone.getTag();
            LocalDate date = (LocalDate) tvDate.getTag();
            LocalTime time = (LocalTime) tvTime.getTag();
            presenter.onClickSave(date, time, timezone);
        } else if (id == R.id.btn_retry) {
            presenter.onClickRetry();
        }
    }

    public void updateContentDate(LocalDate date) {
        tvDate.setTag(date);
        tvDate.setText(formatter.monthDayYear(date));
    }

    public void updateContentTime(LocalTime time) {
        tvTime.setTag(time);
        tvTime.setText(CalendarFormatter.hourMinColon(time, DateFormat.is24HourFormat(getContext
                ())));
    }

    public void updateContentTimezone(String timezoneId) {
        tvTimezone.setTag(timezoneId);
        tvTimezone.setText(timezoneId);
    }

    public void updateContent(WizardTimezoneViewModel viewModel) {
        updateContentDate(viewModel.localDateTime.toLocalDate());
        updateContentTime(viewModel.localDateTime.toLocalTime());
        if (!Strings.isBlank(viewModel.timezone)) {
            tvTimezone.setText(viewModel.timezone);
            tvTimezone.setTag(viewModel.timezone);
        }
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }
}
