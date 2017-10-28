package com.rainmachine.presentation.screens.programdetailsold;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsOldView extends ViewFlipper implements CompoundButton
        .OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    private static final int FLIPPER_START_TIME_BASIC = 0;
    private static final int FLIPPER_START_TIME_OFFSET = 1;

    @Inject
    ProgramDetailsOldPresenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.program_name)
    TextView programName;
    @BindView(R.id.toggle_enabled)
    SwitchCompat toggleActive;
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.toggle_cycle_soak)
    SwitchCompat toggleCycleSoak;
    @BindView(R.id.tv_cycle_soak)
    TextView tvCycleSoak;
    @BindView(R.id.toggle_station_delay)
    SwitchCompat toggleStationDelay;
    @BindView(R.id.tv_station_delay)
    TextView tvStationDelay;
    @BindView(R.id.tv_weekdays)
    TextView tvWeekdays;
    @BindView(R.id.radio_every_day)
    RadioButton radioEveryDay;
    @BindView(R.id.radio_odd_days)
    RadioButton radioOddDays;
    @BindView(R.id.radio_even_days)
    RadioButton radioEvenDays;
    @BindView(R.id.radio_weekdays)
    RadioButton radioWeekDays;
    @BindView(R.id.input_name)
    EditText inputProgramName;
    @BindView(R.id.radio_every_n_days)
    RadioButton radioEveryNDays;
    @BindView(R.id.spinner_every_n_days)
    Spinner spinnerEveryNDays;
    @BindView(R.id.view_program_name)
    LinearLayout viewProgramName;
    @BindView(R.id.toggle_weather_data)
    SwitchCompat toggleWeatherData;
    @BindView(R.id.view_ignore_weather_data)
    LinearLayout viewIgnoreWeatherData;
    @BindView(R.id.tv_next_run)
    TextView tvNextRun;
    @BindView(R.id.view_next_run)
    LinearLayout viewNextRun;
    @BindView(android.R.id.list)
    ListView list;
    @BindView(R.id.tv_watering_time)
    TextView tvWateringTime;
    @BindView(R.id.view_every_n_days)
    LinearLayout viewEveryNDays;
    @BindView(R.id.view_weekdays)
    LinearLayout viewWeekdays;
    @BindView(R.id.view_cycle_soak)
    LinearLayout viewCycleSoak;
    @BindView(R.id.view_station_delay)
    LinearLayout viewStationDelay;
    @BindView(R.id.tv_every)
    TextView tvEvery;
    @BindView(R.id.tv_selected_days)
    TextView tvSelectedDays;
    @BindView(R.id.flipper_start_time)
    ViewFlipper flipperStartTime;
    @BindView(R.id.radio_time_of_day)
    RadioButton radioTimeOfDay;
    @BindView(R.id.radio_sunrise_sunset)
    RadioButton radioSunriseSunset;
    @BindView(R.id.tv_start_time_params)
    TextView tvStartTimeParams;

    private WateringZonesAdapter adapter;
    private List<RadioButton> radioButtons;
    private ClearFocusScrollListener scrollListener = new ClearFocusScrollListener();

    public ProgramDetailsOldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
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
        list.setOnScrollListener(scrollListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
        list.setOnScrollListener(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        int oldPos = (Integer) spinnerEveryNDays.getTag();
        if (oldPos != pos) {
            spinnerEveryNDays.setTag(pos);
            radioEveryNDays.setChecked(true); // in case it is not already checked
            int numDays = getNumDaysBasedOnPosition(pos);
            presenter.onClickedEveryNDaysItem(numDays);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
    }

    private int getPositionBasedOnNumDays(int numDays) {
        return numDays - 2;
    }

    private int getNumDaysBasedOnPosition(int position) {
        return position + 2;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // We have a header and so the position is off by 1
        position = position - 1;
        final ProgramWateringTimes activeZone = adapter.getItem(position);
        presenter.onClickedWateringZone(activeZone);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int id = compoundButton.getId();
        if (id == R.id.toggle_station_delay) {
            presenter.onToggleStationDelay(isChecked);
        } else if (id == R.id.toggle_cycle_soak) {
            presenter.onToggleCycleSoak(isChecked);
        } else if (id == R.id.toggle_enabled) {
            presenter.onToggleActive(isChecked);
        } else if (id == R.id.toggle_weather_data) {
            presenter.onToggleWeatherData(isChecked);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.view_cycle_soak) {
            presenter.onClickCycleSoak();
        } else if (id == R.id.view_station_delay) {
            presenter.onClickStationDelay();
        } else if (id == R.id.view_every_n_days) {
            presenter.onClickedEveryNDays();
        } else if (id == R.id.view_weekdays) {
            presenter.onClickWeekdays();
        }
    }

    @OnClick(R.id.view_start_time)
    public void onClickedStartTime() {
        presenter.onClickStartTime();
    }

    @OnClick(R.id.view_start_time_params_time_of_day)
    public void onClickedStartTimeOfDay() {
        checkTimeOfDayRadio();
        presenter.onClickStartTime();
    }

    @OnClick(R.id.view_start_time_params_sunrise_sunset)
    public void onClickedStartTimeSunriseSunset() {
        presenter.onClickSunriseSunset();
    }

    @OnClick(R.id.view_next_run)
    public void onClickedNextRun() {
        presenter.onClickNextRun();
    }

    private void checkTimeOfDayRadio() {
        radioTimeOfDay.setChecked(true);
        radioSunriseSunset.setChecked(false);
    }

    private void checkSunriseSunsetRadio() {
        radioSunriseSunset.setChecked(true);
        radioTimeOfDay.setChecked(false);
    }

    private void processRadioButtonClick(CompoundButton buttonView) {
        for (RadioButton button : radioButtons) {
            if (button != buttonView) {
                button.setChecked(false);
            }
        }
    }

    private void setupViews(Program program, boolean showMinutesSeconds, boolean useNextRun,
                            boolean useStartTimeParams, boolean use24HourFormat, boolean
                                    showWeatherOption) {
        updateProgramName(program);
        updateActive(program);

        if (showWeatherOption) {
            viewIgnoreWeatherData.setVisibility(View.VISIBLE);
            updateWeatherData(program);
        } else {
            viewIgnoreWeatherData.setVisibility(View.GONE);
        }

        radioButtons = new ArrayList<>();
        radioButtons.add(radioEveryDay);
        radioButtons.add(radioOddDays);
        radioButtons.add(radioEvenDays);
        radioButtons.add(radioEveryNDays);
        radioButtons.add(radioWeekDays);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.program_details_every_n_days, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEveryNDays.setAdapter(adapter);
        updateFrequency(program);

        if (useNextRun) {
            updateNextRun(program);
        }

        if (useStartTimeParams) {
            flipperStartTime.setDisplayedChild(FLIPPER_START_TIME_OFFSET);
            if (program.startTime.isTimeOfDay()) {
                checkTimeOfDayRadio();
                updateStartTimeOfDay(program, true, use24HourFormat);
                updateStartTimeSunriseSunset(program);
            } else {
                checkSunriseSunsetRadio();
                updateStartTimeOfDay(program, true, use24HourFormat);
                updateStartTimeSunriseSunset(program);
            }
        } else {
            flipperStartTime.setDisplayedChild(FLIPPER_START_TIME_BASIC);
            updateStartTimeOfDay(program, false, use24HourFormat);
        }
        updateCycleSoak(program.isCycleSoakEnabled, program.numCycles, program.soakSeconds);
        updateStationDelay(program.isDelayEnabled, program.delaySeconds, showMinutesSeconds);

        this.adapter = new WateringZonesAdapter(getContext(), presenter, formatter, program
                .wateringTimes, showMinutesSeconds);
        list.setAdapter(this.adapter);
        list.setOnItemClickListener(this);

        toggleActive.setOnCheckedChangeListener(this);

        tvWateringTime.setText(showMinutesSeconds ? R.string
                .program_details_watering_time_minutes_seconds : R
                .string.program_details_watering_time);
    }

    private CompoundButton.OnCheckedChangeListener radioListener = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                processRadioButtonClick(buttonView);
                if (buttonView == radioEveryDay) {
                    presenter.onCheckedFrequencyEveryDay();
                } else if (buttonView == radioWeekDays) {
                    presenter.onCheckedFrequencyWeekdays();
                } else if (buttonView == radioOddDays) {
                    presenter.onCheckedFrequencyOddDays();
                } else if (buttonView == radioEvenDays) {
                    presenter.onCheckedFrequencyEvenDays();
                } else if (buttonView == radioEveryNDays) {
                    int position = spinnerEveryNDays.getSelectedItemPosition();
                    int numDays = getNumDaysBasedOnPosition(position);
                    presenter.onCheckedFrequencyEveryNDays(numDays);
                }
            }
        }
    };

    public void setupContent(Program program, boolean useNextRun, boolean showMinutesSeconds,
                             boolean useStartTimeParams, boolean use24HourFormat, boolean
                                     showWeatherOption) {
        list = ButterKnife.findById(this, android.R.id.list);
        View header = View.inflate(getContext(), R.layout.item_header_program_details_old, null);
        list.addHeaderView(header);
        header.setFocusable(true);
        header.setFocusableInTouchMode(true);
        ButterKnife.bind(this);

        viewNextRun.setVisibility(useNextRun ? View.VISIBLE : View.GONE);

        setupViews(program, showMinutesSeconds, useNextRun, useStartTimeParams, use24HourFormat,
                showWeatherOption);
    }

    public void refreshWateringZones() {
        adapter.notifyDataSetChanged();
    }

    public void updateZoneDuration(long zoneId, int duration) {
        List<ProgramWateringTimes> zones = adapter.getItems();
        for (ProgramWateringTimes wtr : zones) {
            if (wtr.id == zoneId) {
                wtr.duration = duration;
                wtr.active = wtr.duration > 0;
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateProgramName(Program program) {
        inputProgramName.setText(program.name);
        inputProgramName.setSelection(inputProgramName.length());
        inputProgramName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.onChangedProgramName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        viewProgramName.setVisibility(View.VISIBLE);
        programName.setVisibility(View.GONE);
    }

    private void updateActive(Program program) {
        toggleActive.setChecked(program.enabled);
    }

    public void updateWeatherData(Program program) {
        toggleWeatherData.setOnCheckedChangeListener(null);
        toggleWeatherData.setChecked(!program.ignoreWeatherData);
        toggleWeatherData.setOnCheckedChangeListener(this);
        toggleWeatherData.setEnabled(true);
    }

    public void updateFrequency(Program program) {
        for (RadioButton rb : radioButtons) {
            rb.setOnCheckedChangeListener(null);
            rb.setChecked(false);
        }
        if (program.isDaily()) {
            radioEveryDay.setChecked(true);
        } else if (program.isWeekDays()) {
            radioWeekDays.setChecked(true);
            tvWeekdays.setVisibility(DomainUtils.isAtLeastOneWeekDaySelected(program
                    .frequencyWeekDays()) ? View.VISIBLE : View.GONE);
            String print = formatter.weekDays(program.frequencyWeekDays());
            tvWeekdays.setText(print);
        } else if (program.isOddDays()) {
            radioOddDays.setChecked(true);
        } else if (program.isEvenDays()) {
            radioEvenDays.setChecked(true);
        }

        if (program.isEveryNDays()) {
            int pos = getPositionBasedOnNumDays(program.frequencyNumDays());
            spinnerEveryNDays.setTag(pos);
            spinnerEveryNDays.setSelection(pos, false);
            radioEveryNDays.setChecked(true);
        } else {
            int pos = 0;
            spinnerEveryNDays.setTag(pos);
            spinnerEveryNDays.setSelection(pos, false);
        }
        spinnerEveryNDays.setOnItemSelectedListener(this);
        spinnerEveryNDays.setEnabled(true);

        for (RadioButton rb : radioButtons) {
            rb.setOnCheckedChangeListener(radioListener);
            rb.setEnabled(true);
        }
        viewEveryNDays.setClickable(true);
        viewEveryNDays.setOnClickListener(this);
        viewWeekdays.setClickable(true);
        viewWeekdays.setOnClickListener(this);
        tvEvery.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        tvSelectedDays.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
    }

    public void activateWeekdays() {
        radioWeekDays.setOnCheckedChangeListener(null);
        radioWeekDays.setChecked(true);
        processRadioButtonClick(radioWeekDays);
        radioWeekDays.setOnCheckedChangeListener(radioListener);
    }

    public void updateWeekdays(Program program) {
        tvWeekdays.setVisibility(DomainUtils.isAtLeastOneWeekDaySelected(program
                .frequencyWeekDays()) ? View.VISIBLE : View.GONE);
        tvWeekdays.setText(formatter.weekDays(program.frequencyWeekDays()));
    }

    public void hideWeekdays() {
        tvWeekdays.setVisibility(View.GONE);
    }

    public void activateEveryNDays() {
        // This triggers onCheckedChanged and updates the program fields
        radioEveryNDays.setChecked(true);
    }

    public void updateNextRun(Program program) {
        if (program.nextRunSprinklerLocalDate != null) {
            tvNextRun.setText(formatter.dayOfWeekmonthDayYear(program.nextRunSprinklerLocalDate));
        }
    }

    public void updateStartTimeOfDay(Program program, boolean useStartTimeParams, boolean
            use24HourFormat) {
        String sTime = CalendarFormatter.hourMinColon(program.startTime.localDateTime.toLocalTime
                (), use24HourFormat);
        if (useStartTimeParams) {
            tvStartTimeParams.setText(sTime);
        } else {
            tvStartTime.setText(sTime);
        }
    }

    public void updateSunriseSunset(Program program) {
        checkSunriseSunsetRadio();
        updateStartTimeSunriseSunset(program);
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

    public void updateCycleSoak(boolean csOn, int cycles, int soak) {
        int soakMinutes = soak / DateTimeConstants.SECONDS_PER_MINUTE;
        tvCycleSoak.setText(getResources().getQuantityString(R.plurals
                        .program_details_x_cycles, cycles,
                cycles) + " / " + getResources().getQuantityString(R.plurals
                        .program_details_x_soak,
                soakMinutes, soakMinutes));
        toggleCycleSoak.setOnCheckedChangeListener(null);
        toggleCycleSoak.setChecked(csOn);
        toggleCycleSoak.setOnCheckedChangeListener(this);
        toggleCycleSoak.setEnabled(true);
        viewCycleSoak.setClickable(true);
        viewCycleSoak.setOnClickListener(this);
    }

    public void updateStationDelay(boolean delayOn, int stationDelay, boolean showMinutesSeconds) {
        if (showMinutesSeconds) {
            tvStationDelay.setText(formatter.hourMinSecColon(stationDelay) + " " + getContext()
                    .getString(R.string.program_details_min));
        } else {
            int minutes = stationDelay / DateTimeConstants.SECONDS_PER_MINUTE;
            tvStationDelay.setText(getResources().getQuantityString(R.plurals.all_x_minutes,
                    minutes, minutes));
        }
        toggleStationDelay.setOnCheckedChangeListener(null);
        toggleStationDelay.setChecked(delayOn);
        toggleStationDelay.setOnCheckedChangeListener(this);
        toggleStationDelay.setEnabled(true);
        viewStationDelay.setClickable(true);
        viewStationDelay.setOnClickListener(this);
    }

    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    // Use scroll listener to clear focus. This is to try to fix offsetRectBetweenParent issue
    protected class ClearFocusScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // do nothing
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
                View currentFocus = ((Activity) getContext()).getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                }
            }
        }
    }
}
