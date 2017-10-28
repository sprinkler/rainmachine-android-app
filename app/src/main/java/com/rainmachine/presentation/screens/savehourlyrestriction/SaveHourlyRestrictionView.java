package com.rainmachine.presentation.screens.savehourlyrestriction;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SaveHourlyRestrictionView extends ViewFlipper {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    SaveHourlyRestrictionPresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.radio_every_day)
    RadioButton radioEveryDay;
    @BindView(R.id.radio_weekdays)
    RadioButton radioWeekDays;
    @BindView(R.id.tv_weekdays)
    TextView tvWeekdays;
    @BindView(R.id.tv_from)
    TextView tvFrom;
    @BindView(R.id.tv_to)
    TextView tvTo;

    private List<RadioButton> mRadioButtons;

    public SaveHourlyRestrictionView(Context context, AttributeSet attrs) {
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

    CompoundButton.OnCheckedChangeListener mRadioListener = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                processRadioButtonClick(buttonView);
                if (buttonView == radioEveryDay) {
                    presenter.onCheckedEveryDay();
                }
            }
        }
    };

    private void processRadioButtonClick(CompoundButton buttonView) {
        for (RadioButton button : mRadioButtons) {
            if (button != buttonView) {
                button.setChecked(false);
            }
        }
    }

    @OnClick(R.id.view_weekdays)
    public void onClickWeekdays() {
        radioWeekDays.setOnCheckedChangeListener(null);
        radioWeekDays.setChecked(true);
        processRadioButtonClick(radioWeekDays);
        radioWeekDays.setOnCheckedChangeListener(mRadioListener);
        presenter.onClickWeekdays();
    }

    @OnClick(R.id.view_from)
    public void onClickFrom() {
        presenter.onClickFrom();
    }

    @OnClick(R.id.view_to)
    public void onClickTo() {
        presenter.onClickTo();
    }

    public void updateFrom(SaveHourlyRestrictionExtra extra) {
        tvFrom.setText(CalendarFormatter.hourMinColon(extra.restriction.fromLocalTime(), extra
                .use24HourFormat));
        tvTo.setText("");
    }

    public void updateTo(SaveHourlyRestrictionExtra extra) {
        tvTo.setText(CalendarFormatter.hourMinColon(extra.restriction.toLocalTime(), extra
                .use24HourFormat));
    }

    public void updateWeekDays(HourlyRestriction restriction) {
        if (!restriction.isDaily()) {
            tvWeekdays.setVisibility(View.VISIBLE);
            tvWeekdays.setText(formatter.weekDays(restriction.weekDays));
        } else {
            tvWeekdays.setVisibility(View.GONE);
        }
    }

    public void updateContent(SaveHourlyRestrictionExtra extra) {
        tvFrom.setText(CalendarFormatter.hourMinColon(extra.restriction.fromLocalTime(), extra
                .use24HourFormat));
        tvTo.setText(CalendarFormatter.hourMinColon(extra.restriction.toLocalTime(), extra
                .use24HourFormat));
        updateWeekDays(extra.restriction);

        boolean isDaily = extra.restriction.isDaily();
        radioEveryDay.setChecked(isDaily);
        radioWeekDays.setChecked(!isDaily);
        mRadioButtons = new ArrayList<>();
        mRadioButtons.add(radioEveryDay);
        mRadioButtons.add(radioWeekDays);
        for (RadioButton rb : mRadioButtons) {
            rb.setOnCheckedChangeListener(mRadioListener);
        }
    }

    public void showWeekDays() {
        radioEveryDay.setChecked(true);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }
}
