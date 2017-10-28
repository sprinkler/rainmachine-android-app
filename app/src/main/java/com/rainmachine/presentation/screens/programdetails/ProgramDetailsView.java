package com.rainmachine.presentation.screens.programdetails;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.ProgramFormatter;
import com.rainmachine.presentation.widgets.DividerItemDecoration;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsView extends ViewFlipper implements ProgramDetailsContract.View {

    private static final int FLIPPER_CONTENT = 0;
    private static final int FLIPPER_PROGRESS = 1;

    @Inject
    ProgramDetailsContract.Presenter presenter;
    @Inject
    CalendarFormatter calendarFormatter;
    @Inject
    ProgramFormatter programFormatter;

    @BindView(R.id.input_name)
    EditText inputName;
    @BindView(R.id.tv_next_run)
    TextView tvNextRun;
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.recycler_zones)
    RecyclerView recyclerZones;
    @BindView(R.id.tv_total_watering)
    TextView tvTotalWatering;
    @BindView(R.id.tv_frequency)
    TextView tvFrequency;

    private ProgramZonesAdapter adapter;

    public ProgramDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_program_details, this);
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
    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showErrorSaveMessage() {
        Toasts.show(R.string.program_details_error_saving_program);
    }

    @Override
    public void showErrorNoDaySelectedMessage() {
        Toasts.showLong(R.string.program_details_select_weekday);
    }

    @Override
    public void showErrorAtLeastOneWateringTime() {
        Toasts.showLong(R.string.program_details_at_least_one_watering_time);
    }

    @Override
    public void setup(Program program, boolean isUnitsMetric, boolean use24HourFormat) {
        inputName.setText(program.name);
        inputName.setSelection(inputName.length());
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.onChangeProgramName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateStartTime(program, use24HourFormat);
        updateFrequency(program);
        updateNextRun(program, use24HourFormat);

        updateTotalWatering(program);

        recyclerZones.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager
                .VERTICAL, false));
        recyclerZones.setHasFixedSize(true);
        recyclerZones.addItemDecoration(new DividerItemDecoration(getContext(), null, true));
        adapter = new ProgramZonesAdapter(getContext(), presenter, calendarFormatter, program,
                programFormatter);
        recyclerZones.setAdapter(adapter);
    }

    @Override
    public void updateNextRun(Program program, boolean use24HourFormat) {
        if (program.enabled) {
            if (program.nextRunSprinklerLocalDate != null) {
                String date = calendarFormatter.dayOfWeekmonthDayYear(program
                        .nextRunSprinklerLocalDate);
                String time = programFormatter.startTime(program, use24HourFormat);
                tvNextRun.setText(getResources().getString(R.string.program_details_next_run_full,
                        date, time));
            }
        } else {
            tvNextRun.setText(R.string.program_details_is_inactive);
        }
    }

    @Override
    public void updateStartTime(Program program, boolean use24HourFormat) {
        tvStartTime.setText(programFormatter.startTime(program, use24HourFormat));
    }

    @Override
    public void updateFrequency(Program program) {
        if (program.isDaily()) {
            tvFrequency.setText(R.string.program_details_daily);
        } else if (program.isOddDays()) {
            tvFrequency.setText(R.string.program_details_odd_days);
        } else if (program.isEvenDays()) {
            tvFrequency.setText(R.string.program_details_even_days);
        } else if (program.isWeekDays()) {
            tvFrequency.setText(calendarFormatter.weekDays(program.frequencyWeekDays()));
        } else if (program.isEveryNDays()) {
            int numDays = program.frequencyNumDays();
            tvFrequency.setText(getResources().getString(R.string.program_details_every_n_days,
                    numDays));
        }
    }

    @Override
    public void updateTotalWatering(Program program) {
        tvTotalWatering.setText(programFormatter.wateringTimesDuration(program, program
                .totalWatering()));
    }

    @Override
    public void updateWateringTimes(Program program) {
        adapter.updateData(program);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.card_start_time)
    public void onClickCardStartTime() {
        presenter.onClickStartTime();
    }

    @OnClick(R.id.card_frequency)
    public void onClickCardFrequency() {
        presenter.onClickFrequency();
    }

    @OnClick(R.id.btn_minus)
    public void onClickMinus() {
        presenter.onClickMinus();
    }

    @OnClick(R.id.btn_plus)
    public void onClickPlus() {
        presenter.onClickPlus();
    }

    @OnClick(R.id.card_advanced_settings)
    public void onClickAdvancedSettings() {
        presenter.onClickAdvancedSettings();
    }
}
