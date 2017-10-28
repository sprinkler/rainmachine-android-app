package com.rainmachine.presentation.screens.programdetailszones;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.ProgramFormatter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsZonesView extends ScrollView implements ProgramDetailsZonesContract
        .View {

    @Inject
    ProgramDetailsZonesContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    ProgramFormatter programFormatter;

    @BindView(R.id.tv_duration_adjusted_weather)
    TextView tvDurationAdjustedWeather;
    @BindView(R.id.radio_determined)
    RadioButton radioDetermined;
    @BindView(R.id.tv_determined_duration)
    TextView tvDeterminedDuration;
    @BindView(R.id.radio_custom)
    RadioButton radioCustom;
    @BindView(R.id.tv_custom_watering)
    TextView tvCustomWatering;
    @BindView(R.id.radio_no_watering)
    RadioButton radioNoWatering;
    @BindView(R.id.view_next)
    View viewNext;
    @BindView(R.id.view_previous)
    TextView tvPrevious;

    public ProgramDetailsZonesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_program_details_zones, this);
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
    public void render(Program program, ProgramWateringTimes programWateringTimes, ZonePosition
            zonePosition) {
        tvDurationAdjustedWeather.setVisibility(program.ignoreWeatherData ? View.GONE : View
                .VISIBLE);

        if (programWateringTimes.isCustom()) {
            radioCustom.setChecked(true);
            processRadioButtonClick(radioCustom);
        } else if (programWateringTimes.isDetermined()) {
            radioDetermined.setChecked(true);
            processRadioButtonClick(radioDetermined);
        } else {
            radioNoWatering.setChecked(true);
            processRadioButtonClick(radioNoWatering);
        }

        updateCustomWatering(programWateringTimes);
        updateDeterminedWatering(program, programWateringTimes);
        updateNextZoneView(zonePosition);
        updatePreviousZoneView(zonePosition);
    }

    @Override
    public void updateCustomWatering(ProgramWateringTimes programWateringTimes) {
        String sDuration = "0 min 0 sec";
        if (programWateringTimes.duration > 0) {
            sDuration = calendarFormatter.hourMinSecLabel(programWateringTimes.duration);
        }
        tvCustomWatering.setText(sDuration);
    }

    @Override
    public void updateDeterminedWatering(Program program, ProgramWateringTimes
            programWateringTimes) {
        List<ProgramWateringTimes.SelectedDayDuration> values = ProgramWateringTimes
                .suggestedProgramWateringDurations(programWateringTimes, program);
        tvDeterminedDuration.setText(programFormatter.wateringTimesDuration(program, values));
    }

    @OnClick({R.id.card_custom, R.id.card_do_not_water, R.id.card_determined, R.id
            .view_previous, R.id.view_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_custom:
                radioCustom.setChecked(true);
                processRadioButtonClick(radioCustom);
                presenter.onSelectedCustom();
                break;
            case R.id.card_do_not_water:
                radioNoWatering.setChecked(true);
                processRadioButtonClick(radioNoWatering);
                presenter.onSelectedDoNotWater();
                break;
            case R.id.card_determined:
                radioDetermined.setChecked(true);
                processRadioButtonClick(radioDetermined);
                presenter.onSelectedDetermined();
                break;
            case R.id.view_previous:
                presenter.onClickPreviousZone();
                break;
            case R.id.view_next:
                presenter.onClickNextZone();
                break;
        }
    }

    private void processRadioButtonClick(CompoundButton buttonView) {
        if (radioDetermined != buttonView) {
            radioDetermined.setChecked(false);
        }
        if (radioCustom != buttonView) {
            radioCustom.setChecked(false);
        }
        if (radioNoWatering != buttonView) {
            radioNoWatering.setChecked(false);
        }
    }

    private void updateNextZoneView(ZonePosition zonePosition) {
        viewNext.setBackgroundColor(ContextCompat.getColor(getContext(), zonePosition ==
                ZonePosition.LAST ? R.color.gray_light : R.color.main));
    }

    private void updatePreviousZoneView(ZonePosition zonePosition) {
        tvPrevious.setBackgroundColor(ContextCompat.getColor(getContext(), zonePosition ==
                ZonePosition.FIRST ? R.color.gray_light : R.color.white));
        tvPrevious.setTextColor(ContextCompat.getColor(getContext(), zonePosition == ZonePosition
                .FIRST ? R.color.text_white : R.color.main));
    }
}
