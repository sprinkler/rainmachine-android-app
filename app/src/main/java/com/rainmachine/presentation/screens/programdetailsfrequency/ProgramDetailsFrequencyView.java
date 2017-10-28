package com.rainmachine.presentation.screens.programdetailsfrequency;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.ViewUtils;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProgramDetailsFrequencyView extends ScrollView implements
        ProgramDetailsFrequencyContract.View {

    @Inject
    ProgramDetailsFrequencyContract.Presenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.radio_daily)
    RadioButton radioDaily;
    @BindView(R.id.radio_odd_days)
    RadioButton radioOddDays;
    @BindView(R.id.radio_even_days)
    RadioButton radioEvenDays;
    @BindView(R.id.radio_every_n_days)
    RadioButton radioEveryNDays;
    @BindView(R.id.radio_selected_days)
    RadioButton radioWeekDays;
    @BindView(R.id.tv_weekdays)
    TextView tvWeekDays;
    @BindView(R.id.tv_next_run_label)
    TextView tvNextRunLabel;
    @BindView(R.id.tv_next_run)
    TextView tvNextRun;
    @BindView(R.id.card_next_run)
    CardView cardNextRun;

    private List<RadioButton> radioFrequencyButtons;

    public ProgramDetailsFrequencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            ((SprinklerActivity) getContext()).inject(this);
        }
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_program_details_frequency, this);
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
    public void setup(Program program) {
        radioFrequencyButtons = new ArrayList<>();
        radioFrequencyButtons.add(radioDaily);
        radioFrequencyButtons.add(radioOddDays);
        radioFrequencyButtons.add(radioEvenDays);
        radioFrequencyButtons.add(radioEveryNDays);
        radioFrequencyButtons.add(radioWeekDays);
        updateFrequency(program);
        updateNextRun(program);
    }

    @Override
    public void updateWeekDays(Program program) {
        tvWeekDays.setVisibility(DomainUtils.isAtLeastOneWeekDaySelected(program
                .frequencyWeekDays()) ? View.VISIBLE : View.GONE);
        tvWeekDays.setText(formatter.weekDays(program.frequencyWeekDays()));
    }

    @Override
    public void hideWeekDays() {
        tvWeekDays.setVisibility(View.GONE);
    }

    @Override
    public void updateEveryNDays(Program program) {
        int numDays = program.frequencyNumDays();
        radioEveryNDays.setText(getResources().getString(R.string.program_details_every_n_days,
                numDays));
    }

    @Override
    public void updateNextRun(Program program) {
        if (program.nextRunSprinklerLocalDate != null) {
            cardNextRun.setCardBackgroundColor(ContextCompat.getColor(getContext(), program
                    .isEveryNDays() ? R.color.main : R.color.white));
            tvNextRunLabel.setText(program.isEveryNDays() ? R.string
                    .program_details_change_next_run : R.string.program_details_next_run);
            tvNextRunLabel.setTextColor(ContextCompat.getColor(getContext(), program.isEveryNDays
                    () ? R.color.text_white : R.color.text_primary));
            tvNextRun.setTextColor(ContextCompat.getColor(getContext(), program.isEveryNDays() ?
                    R.color.text_white : R.color.text_gray));
            tvNextRun.setText(formatter.dayOfWeekmonthDayYear(program.nextRunSprinklerLocalDate));
            tvNextRun.setCompoundDrawablesWithIntrinsicBounds(0, 0, program.isEveryNDays() ? R
                    .drawable.ic_action_next_item : 0, 0);
            int rightMargin = 0;
            if (!program.isEveryNDays()) {
                rightMargin = (int) ViewUtils.dpToPixels(40, getContext());
            }
            ((LinearLayout.LayoutParams) tvNextRun.getLayoutParams()).rightMargin = rightMargin;
        }
    }

    @OnClick(R.id.card_daily)
    void onClickCardDaily() {
        radioDaily.setChecked(true);
    }

    @OnClick(R.id.card_odd_days)
    void onClickCardOddDays() {
        radioOddDays.setChecked(true);
    }

    @OnClick(R.id.card_even_days)
    void onClickCardEvenDays() {
        radioEvenDays.setChecked(true);
    }

    @OnClick({R.id.card_every_n_days, R.id.radio_every_n_days})
    void onClickCardEveryNDays() {
        radioEveryNDays.setChecked(true);
        presenter.onCheckFrequencyEveryNDays();
    }

    @OnClick({R.id.card_week_days, R.id.radio_selected_days})
    void onClickCardWeekDays() {
        radioWeekDays.setChecked(true);
        presenter.onCheckFrequencyWeekdays();
    }

    @OnClick(R.id.card_next_run)
    public void onClickCardNextRun() {
        presenter.onClickNextRun();
    }

    private void updateFrequency(Program program) {
        for (RadioButton rb : radioFrequencyButtons) {
            rb.setOnCheckedChangeListener(null);
            rb.setChecked(false);
        }
        if (program.isDaily()) {
            radioDaily.setChecked(true);
        } else if (program.isOddDays()) {
            radioOddDays.setChecked(true);
        } else if (program.isEvenDays()) {
            radioEvenDays.setChecked(true);
        } else if (program.isWeekDays()) {
            radioWeekDays.setChecked(true);
            updateWeekDays(program);
        } else if (program.isEveryNDays()) {
            radioEveryNDays.setChecked(true);
        }

        updateEveryNDays(program);

        for (RadioButton rb : radioFrequencyButtons) {
            rb.setOnCheckedChangeListener(radioFrequencyListener);
        }
    }

    private void processRadioButtonClick(CompoundButton buttonView) {
        for (RadioButton button : radioFrequencyButtons) {
            if (button != buttonView) {
                button.setChecked(false);
            }
        }
    }

    private CompoundButton.OnCheckedChangeListener radioFrequencyListener = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                processRadioButtonClick(buttonView);
                if (buttonView == radioDaily) {
                    presenter.onCheckFrequencyDaily();
                } else if (buttonView == radioOddDays) {
                    presenter.onCheckFrequencyOddDays();
                } else if (buttonView == radioEvenDays) {
                    presenter.onCheckFrequencyEvenDays();
                }
            }
        }
    };
}
