package com.rainmachine.presentation.screens.currentrestrictions;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.LocalTime;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CurrentRestrictionsView extends ViewFlipper implements CurrentRestrictionsContract
        .View {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;
    private static final int FLIPPER_ERROR = 2;

    @Inject
    CurrentRestrictionsContract.Presenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.tv_snooze_title)
    TextView tvSnoozeTitle;
    @BindView(R.id.tv_snooze_subtitle)
    TextView tvSnoozeSubtitle;
    @BindView(R.id.view_snooze)
    LinearLayout viewSnooze;
    @BindView(R.id.tv_rain_sensor_subtitle)
    TextView tvRainSensorSubtitle;
    @BindView(R.id.view_rain_sensor)
    LinearLayout viewRainSensor;
    @BindView(R.id.tv_freeze_protect_title)
    TextView tvFreezeProtectTitle;
    @BindView(R.id.tv_freeze_protect_subtitle)
    TextView tvFreezeProtectSubtitle;
    @BindView(R.id.view_freeze_protect)
    LinearLayout viewFreezeProtect;
    @BindView(R.id.tv_month_title)
    TextView tvMonthTitle;
    @BindView(R.id.tv_month_subtitle)
    TextView tvMonthSubtitle;
    @BindView(R.id.view_month)
    LinearLayout viewMonth;
    @BindView(R.id.tv_day_title)
    TextView tvDayTitle;
    @BindView(R.id.tv_day_subtitle)
    TextView tvDaySubtitle;
    @BindView(R.id.view_day)
    LinearLayout viewDay;
    @BindView(R.id.view_hour)
    LinearLayout viewHour;
    @BindView(R.id.tv_hour_title)
    TextView tvHourTitle;
    @BindView(R.id.tv_hour_subtitle)
    TextView tvHourSubtitle;

    public CurrentRestrictionsView(Context context, AttributeSet attrs) {
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

    @OnClick(R.id.btn_retry)
    public void onClickRetry() {
        presenter.onClickRetry();
    }

    @OnClick(R.id.card_snooze)
    public void onClickSnooze() {
        presenter.onClickSnooze();
    }

    @OnClick(R.id.card_rain_sensor)
    public void onClickRainSensor() {
        presenter.onClickRainSensor();
    }

    @OnClick(R.id.card_freeze_protect)
    public void onClickFreezeProtect() {
        presenter.onClickFreezeProtect();
    }

    @OnClick(R.id.card_month)
    public void onClickMonth() {
        presenter.onClickMonth();
    }

    @OnClick(R.id.card_day)
    public void onClickDay() {
        presenter.onClickDay();
    }

    @OnClick(R.id.card_hour)
    public void onClickHour() {
        presenter.onClickHour();
    }

    public void updateContent(CurrentRestrictionsViewModel viewModel) {
        if (viewModel.isRainDelay) {
            tvSnoozeTitle.setText(formatter.daysHoursMinutes(viewModel.rainDelayCounterRemaining,
                    false));
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan();
            CharSequence subtitle = truss.build();
            tvSnoozeSubtitle.setText(subtitle);
            viewSnooze.setVisibility(View.VISIBLE);
        } else {
            viewSnooze.setVisibility(View.GONE);
        }

        if (viewModel.isRainSensor) {
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan()
                    .append(rainSensorDuration(viewModel.rainSensorSnoozeDuration));
            CharSequence subtitle = truss.build();
            tvRainSensorSubtitle.setText(subtitle);
            viewRainSensor.setVisibility(View.VISIBLE);
        } else {
            viewRainSensor.setVisibility(View.GONE);
        }

        if (viewModel.isFreezeProtect) {
            tvFreezeProtectTitle.setText(getResources().getString(R.string
                            .current_restrictions_freeze_protect_under,
                    viewModel.freezeProtectTemp, viewModel.isUnitsMetric ? getResources()
                            .getString(R
                                    .string.all_temperature_unit_celsius) : getResources()
                            .getString(R
                                    .string
                                    .all_temperature_unit_fahrenheit)));
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan();
            CharSequence subtitle = truss.build();
            tvFreezeProtectSubtitle.setText(subtitle);
            viewFreezeProtect.setVisibility(View.VISIBLE);
        } else {
            viewFreezeProtect.setVisibility(View.GONE);
        }

        if (viewModel.isMonth) {
            tvMonthTitle.setText(getResources().getString(R.string
                    .current_restrictions_month_day, viewModel
                    .monthOfYear.getAsText(Locale.ENGLISH)));
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan();
            CharSequence subtitle = truss.build();
            tvMonthSubtitle.setText(subtitle);
            viewMonth.setVisibility(View.VISIBLE);
        } else {
            viewMonth.setVisibility(View.GONE);
        }

        if (viewModel.isDay) {
            tvDayTitle.setText(getResources().getString(R.string
                    .current_restrictions_month_day, viewModel
                    .dayOfWeek.getAsText(Locale.ENGLISH)));
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan();
            CharSequence subtitle = truss.build();
            tvDaySubtitle.setText(subtitle);
            viewDay.setVisibility(View.VISIBLE);
        } else {
            viewDay.setVisibility(View.GONE);
        }

        if (viewModel.isHour) {
            LocalTime startTime = viewModel.hourlyRestriction.fromLocalTime();
            LocalTime endTime = viewModel.hourlyRestriction.toLocalTime();
            tvHourTitle.setText(getResources().getString(R.string
                            .current_restrictions_hourly_from_to,
                    CalendarFormatter.hourMinColon(startTime, viewModel.use24HourFormat),
                    CalendarFormatter.hourMinColon(endTime, viewModel.use24HourFormat)));
            Truss truss = new Truss()
                    .append(getResources().getString(R.string
                            .current_restrictions_rainmachine_is))
                    .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R
                            .color.text_red)))
                    .append(getResources().getString(R.string
                            .current_restrictions_paused).toUpperCase(Locale.ENGLISH))
                    .popSpan()
                    .append(getResources().getString(R.string
                            .current_restrictions_for_x_minutes, viewModel
                            .hourlyRestriction.minuteDuration));
            CharSequence subtitle = truss.build();
            tvHourSubtitle.setText(subtitle);
            viewHour.setVisibility(View.VISIBLE);
        } else {
            viewHour.setVisibility(View.GONE);
        }
    }

    private String rainSensorDuration(Provision.RainSensorSnoozeDuration snoozeDuration) {
        switch (snoozeDuration) {
            case RESUME:
                return getResources().getString(R.string.current_restrictions_for_resume);
            case UNTIL_MIDNIGHT:
                return getResources().getString(R.string.current_restrictions_for_one_day);
            case SNOOZE_6_HOURS:
                return getResources().getString(R.string.current_restrictions_for_6);
            case SNOOZE_12_HOURS:
                return getResources().getString(R.string.current_restrictions_for_12);
            case SNOOZE_24_HOURS:
                return getResources().getString(R.string.current_restrictions_for_24);
            case SNOOZE_48_HOURS:
                return getResources().getString(R.string.current_restrictions_for_48);
        }
        throw new IllegalArgumentException("Unrecognized rain sensor snooze duration");
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
