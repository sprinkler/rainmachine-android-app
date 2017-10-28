package com.rainmachine.presentation.screens.programdetailsadvanced;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Truss;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsAdvancedView extends ViewFlipper implements
        ProgramDetailsAdvancedContract.View,
        CompoundButton.OnCheckedChangeListener {

    @Inject
    ProgramDetailsAdvancedContract.Presenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.tv_cycle_soak)
    TextView tvCycleSoak;
    @BindView(R.id.tv_cycle_soak_duration)
    TextView tvCycleSoakDuration;
    @BindView(R.id.tv_delay_zones)
    TextView tvDelayZones;
    @BindView(R.id.tv_delay_zones_duration)
    TextView tvDelayZonesDuration;
    @BindView(R.id.tv_not_run_program)
    TextView tvNotRunProgram;
    @BindView(R.id.radio_weather_adaptive)
    RadioButton radioWeatherAdaptive;
    @BindView(R.id.radio_weather_fixed)
    RadioButton radioWeatherFixed;

    public ProgramDetailsAdvancedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_program_details_advanced, this);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (isChecked) {
            if (id == R.id.radio_weather_adaptive) {
                radioWeatherFixed.setChecked(false);
                presenter.onChangeAdjustWeatherTimes(true);
            } else if (id == R.id.radio_weather_fixed) {
                radioWeatherAdaptive.setChecked(false);
                presenter.onChangeAdjustWeatherTimes(false);
            }
        }
    }

    @Override
    public void setup(Program program, boolean isUnitsMetric) {
        updateCycleSoak(program);
        updateDelayZones(program);

        updateMaxAmountNotRun(program, isUnitsMetric);

        Truss truss = new Truss()
                .pushSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen
                        .text_larger)))
                .append(getResources().getString(R.string.program_details_adjust_times))
                .popSpan()
                .append("\n")
                .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color
                        .text_gray)))
                .append(getResources().getString(R.string.program_details_weather_adaptive_hint))
                .popSpan();
        radioWeatherAdaptive.setText(truss.build());
        radioWeatherAdaptive.setChecked(!program.ignoreWeatherData);
        radioWeatherAdaptive.setOnCheckedChangeListener(this);

        truss = new Truss()
                .pushSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen
                        .text_larger)))
                .append(getResources().getString(R.string.program_details_weather_fixed))
                .popSpan()
                .append("\n")
                .pushSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color
                        .text_gray)))
                .append(getResources().getString(R.string.program_details_weather_fixed_hint))
                .popSpan();
        radioWeatherFixed.setText(truss.build());
        radioWeatherFixed.setChecked(program.ignoreWeatherData);
        radioWeatherFixed.setOnCheckedChangeListener(this);
    }

    @OnClick(R.id.card_cycle_soak)
    public void onClickCycleSoak() {
        presenter.onClickCycleSoak();
    }

    @OnClick(R.id.card_delay_zones)
    public void onClickDelayZones() {
        presenter.onClickDelayZones();
    }

    @OnClick(R.id.card_do_not_run)
    public void onClickDoNotRun() {
        presenter.onClickDoNotRun();
    }

    @OnClick(R.id.card_weather_adaptive)
    public void onClickWeatherAdaptive() {
        radioWeatherAdaptive.setChecked(true);
    }

    @OnClick(R.id.card_weather_fixed)
    public void onClickWeatherFixed() {
        radioWeatherFixed.setChecked(true);
    }

    @Override
    public void updateDelayZones(Program program) {
        if (!program.isDelayEnabled) {
            tvDelayZones.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));
            tvDelayZones.setText(R.string.program_details_off);
            tvDelayZonesDuration.setVisibility(View.GONE);
        } else {
            tvDelayZones.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            tvDelayZones.setText(R.string.all_custom);

            tvDelayZonesDuration.setText(formatter.hourMinSecColon(program.delaySeconds) + " " +
                    getContext().getString(R.string.program_details_min));
            tvDelayZonesDuration.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateCycleSoak(Program program) {
        if (!program.isCycleSoakEnabled) {
            tvCycleSoak.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));
            tvCycleSoak.setText(R.string.program_details_off);
            tvCycleSoakDuration.setVisibility(View.GONE);
        } else if (program.isCycleSoakAuto()) {
            tvCycleSoak.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            tvCycleSoak.setText(R.string.program_details_auto);
            tvCycleSoakDuration.setVisibility(View.GONE);
        } else {
            tvCycleSoak.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            tvCycleSoak.setText(R.string.all_custom);
            int soakMinutes = program.soakSeconds / DateTimeConstants.SECONDS_PER_MINUTE;
            tvCycleSoakDuration.setText(getResources().getQuantityString(R.plurals
                    .program_details_x_cycles, program.numCycles, program.numCycles) + " / " +
                    getResources().getQuantityString(R.plurals.program_details_x_soak,
                            soakMinutes, soakMinutes));
            tvCycleSoakDuration.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateMaxAmountNotRun(Program program, boolean isUnitsMetric) {
        if (program.maxRainAmountMm <= 0) {
            tvNotRunProgram.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));
            tvNotRunProgram.setText(R.string.all_not_set);
        } else {
            tvNotRunProgram.setTextColor(ContextCompat.getColor(getContext(), R.color.main));
            String s;
            if (isUnitsMetric) {
                s = program.maxRainAmountMm + " " + getContext().getString(R.string.all_mm);
            } else {
                String[] items = getResources().getStringArray(R.array
                        .program_details_not_run_inch);
                String item = items[0]; // default
                int[] values = getResources().getIntArray(R.array.program_details_not_run_values);
                for (int i = 0; i < values.length; i++) {
                    int value = values[i];
                    if (value == program.maxRainAmountMm) {
                        item = items[i];
                        break;
                    }
                }
                s = item;
            }
            tvNotRunProgram.setText(s);
        }
    }
}
