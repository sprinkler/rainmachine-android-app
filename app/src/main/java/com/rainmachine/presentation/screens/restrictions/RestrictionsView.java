package com.rainmachine.presentation.screens.restrictions;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.infrastructure.util.BaseApplication;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.HourlyRestrictionFormatter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RestrictionsView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    RestrictionsPresenter presenter;
    @Inject
    CalendarFormatter formatter;
    @Inject
    HourlyRestrictionFormatter hourlyRestrictionFormatter;

    @BindView(R.id.toggle_hot_days)
    SwitchCompat toggleHotDays;
    @BindView(R.id.tv_freeze_protect)
    TextView tvFreezeProtect;
    @BindView(R.id.tv_months)
    TextView tvMonths;
    @BindView(R.id.tv_weekdays)
    TextView tvWeekdays;
    @BindView(R.id.tv_hours)
    TextView tvHours;
    @BindView(R.id.toggle_freeze_protect)
    SwitchCompat toggleFreezeProtect;
    @BindView(R.id.tv_hot_days)
    TextView tvHotDays;
    @BindView(R.id.tv_duration_threshold)
    TextView tvDurationThreshold;

    public RestrictionsView(Context context, AttributeSet attrs) {
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

    @OnClick({R.id.view_hot_days, R.id.view_freeze_protect, R.id.view_months, R.id.view_weekdays,
            R.id.view_hours, R.id.view_duration_threshold})
    public void onClickOption(View view) {
        int id = view.getId();
        if (id == R.id.view_hot_days) {
            presenter.onClickHotDays();
        } else if (id == R.id.view_freeze_protect) {
            presenter.onClickFreezeProtect();
        } else if (id == R.id.view_months) {
            presenter.onClickMonths();
        } else if (id == R.id.view_weekdays) {
            presenter.onClickWeekDays();
        } else if (id == R.id.view_hours) {
            presenter.onClickHours();
        } else if (id == R.id.view_duration_threshold) {
            presenter.onClickDurationThreshold();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.toggle_hot_days) {
            presenter.onCheckedChangedHotDays(isChecked);
        } else if (id == R.id.toggle_freeze_protect) {
            presenter.onCheckedChangedFreezeProtect(isChecked);
        }
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onClickedRetry();
    }

    public void render(RestrictionsViewModel viewModel) {
        toggleHotDays.setOnCheckedChangeListener(null);
        toggleHotDays.setChecked(viewModel.globalRestrictions.hotDaysExtraWatering);
        toggleHotDays.setOnCheckedChangeListener(this);
        tvHotDays.setTextColor(ContextCompat.getColor(getContext(), viewModel.globalRestrictions
                .hotDaysExtraWatering ? R.color.text_green : R.color.text_primary));
        if (viewModel.globalRestrictions.hotDaysExtraWatering) {
            tvHotDays.setText(getContext().getString(R.string.restrictions_max_watering_percent,
                    viewModel.maxWateringCoefficient));
        } else {
            tvHotDays.setText(R.string.restrictions_allow_extra_watering);
        }

        toggleFreezeProtect.setOnCheckedChangeListener(null);
        toggleFreezeProtect.setChecked(viewModel.globalRestrictions.freezeProtectEnabled);
        toggleFreezeProtect.setOnCheckedChangeListener(this);
        Truss freezeProtectStyled = new Truss();
        if (viewModel.globalRestrictions.freezeProtectEnabled) {
            freezeProtectStyled.pushSpan(new ForegroundColorSpan(ContextCompat.getColor
                    (getContext(), R.color.text_red)));
        }
        String temperatureUnit = viewModel.isUnitsMetric ? getContext().getString(R.string
                .all_temperature_unit_celsius) : getContext().getString(R.string
                .all_temperature_unit_fahrenheit);
        freezeProtectStyled.append(getContext().getString(R.string.restrictions_do_not_water_under,
                viewModel.globalRestrictions.freezeProtectTemperature(viewModel.isUnitsMetric),
                temperatureUnit));
        if (viewModel.globalRestrictions.freezeProtectEnabled) {
            freezeProtectStyled.popSpan();
        }
        tvFreezeProtect.setText(freezeProtectStyled.build());

        tvMonths.setText(formatter.months(viewModel.globalRestrictions.noWaterInMonths));
        tvWeekdays.setText(formatter.weekDays(viewModel.globalRestrictions.noWaterInWeekDays));
        tvHours.setText(toString(viewModel.hourlyRestrictions, viewModel.use24HourFormat));

        tvDurationThreshold.setText(getResources().getQuantityString(R.plurals
                .mini8_settings_x_seconds, viewModel.minWateringDurationThreshold, viewModel
                .minWateringDurationThreshold));
    }

    private String toString(List<HourlyRestriction> restrictions, boolean use24HourFormat) {
        StringBuilder sb = new StringBuilder();
        for (HourlyRestriction restriction : restrictions) {
            sb.append(toLongString(restriction, use24HourFormat)).append(", ");
        }
        // delete last comma and space
        int len = sb.length();
        if (len >= 2) {
            sb.deleteCharAt(len - 1);
            sb.deleteCharAt(len - 2);
        }
        return sb.toString();
    }

    private String toLongString(HourlyRestriction restriction, boolean use24HourFormat) {
        String textBefore;
        if (restriction.isDaily()) {
            textBefore = BaseApplication.getContext().getString(R.string.all_every_day_restriction);
        } else {
            textBefore = formatter.weekDays(restriction.weekDays);
        }
        return textBefore + " " + hourlyRestrictionFormatter.interval(restriction, use24HourFormat);
    }

    public void setup() {
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
