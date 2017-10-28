package com.rainmachine.presentation.screens.raindelay;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;
import com.rainmachine.presentation.util.Toasts;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.shawnlin.numberpicker.NumberPicker;

import org.joda.time.DateTimeConstants;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

public class RainDelayView extends ViewFlipper implements RainDelayContract.View {

    private static final int FLIPPER_CONTENT_OLD = 0;
    private static final int FLIPPER_CONTENT = 1;
    private static final int FLIPPER_PROGRESS = 2;
    private static final int FLIPPER_ERROR = 3;
    private static final int TIMER_VALUE_DEFAULT = 0;

    @Inject
    RainDelayContract.Presenter presenter;
    @Inject
    CalendarFormatter formatter;

    @BindView(R.id.timer_value)
    TextView tvTimerValue;
    @BindView(R.id.btn_up)
    ImageView btnUp;
    @BindView(R.id.btn_down)
    ImageView btnDown;
    @BindView(R.id.plus_minus)
    ViewGroup viewPlusMinus;
    @BindView(R.id.btn_timer)
    Button btnTimer;
    @BindView(R.id.picker_days)
    NumberPicker pickerDays;
    @BindView(R.id.picker_hours)
    NumberPicker pickerHours;
    @BindView(R.id.picker_minutes)
    NumberPicker pickerMinutes;

    @State
    int timerValue = TIMER_VALUE_DEFAULT;
    @State
    boolean timerValueManuallyChanged;

    public RainDelayView(Context context, AttributeSet attrs) {
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
    protected Parcelable onSaveInstanceState() {
        return Icepick.saveInstanceState(this, super.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(Icepick.restoreInstanceState(this, state));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            presenter.destroy();
        }
    }

    @OnClick(R.id.btn_up)
    public void onUp() {
        timerValue += 1;
        btnDown.setEnabled(true);
        updateTimerView();
        timerValueManuallyChanged = true;
    }

    @OnClick(R.id.btn_down)
    public void onDown() {
        timerValue -= 1;
        if (timerValue < 1) {
            timerValue = 1;
        }
        btnDown.setEnabled(timerValue > 1);
        updateTimerView();
        timerValueManuallyChanged = true;
    }

    @OnClick(R.id.btn_timer)
    public void onTimerClick() {
        presenter.onClickDelay(timerValue);
    }

    @OnClick(R.id.btn_snooze)
    public void onClickSnooze() {
        int numSeconds = pickerDays.getValue() * DateTimeConstants.SECONDS_PER_DAY + pickerHours
                .getValue() * DateTimeConstants.SECONDS_PER_HOUR + pickerMinutes.getValue() *
                DateTimeConstants.SECONDS_PER_MINUTE;
        presenter.onClickSnooze(numSeconds);
    }

    private void updateTimerView() {
        tvTimerValue.setText(getResources().getQuantityString(R.plurals.rain_delay_days,
                timerValue, timerValue));
    }

    @OnClick(R.id.btn_retry)
    public void onRetry() {
        presenter.onClickRetry();
    }

    @Override
    public void render(RainDelayViewModel viewModel) {
        if (viewModel.counterRemaining <= 0) {
            if (!timerValueManuallyChanged) {
                timerValue = 1;
            }
            tvTimerValue.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.text_timer));
            updateTimerView();

            btnTimer.setText(viewModel.showSnoozePhrasing ? R.string.all_snooze : R.string
                    .rain_delay_delay);
            btnTimer.setBackgroundResource(R.drawable.rain_btn_default_holo_light);
            btnUp.setEnabled(true);
            btnDown.setEnabled(timerValue > 1);
            viewPlusMinus.setVisibility(View.VISIBLE);
        } else {
            tvTimerValue.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.text_timer_rain_delay));
            tvTimerValue.setText(formatter.daysHoursMinutes(viewModel.counterRemaining, true));

            btnTimer.setText(R.string.rain_delay_resume);
            btnTimer.setBackgroundResource(R.drawable.rain_btn_alert_holo_light);
            btnUp.setEnabled(false);
            btnDown.setEnabled(false);
            viewPlusMinus.setVisibility(View.GONE);
        }

        setDisplayedChild(FLIPPER_CONTENT_OLD);
    }

    @Override
    public void showContent() {
        setDisplayedChild(FLIPPER_CONTENT);
    }

    @Override
    public void showContentOld() {
        setDisplayedChild(FLIPPER_CONTENT_OLD);
    }

    @Override
    public void showProgress() {
        setDisplayedChild(FLIPPER_PROGRESS);
    }

    @Override
    public void showError() {
        setDisplayedChild(FLIPPER_ERROR);
    }

    @Override
    public void showInvalidDurationMessage() {
        Toasts.show(R.string.rain_delay_invalid_message);
    }
}
