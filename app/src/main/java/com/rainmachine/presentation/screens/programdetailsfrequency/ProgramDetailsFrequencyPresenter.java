package com.rainmachine.presentation.screens.programdetailsfrequency;

import com.rainmachine.R;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import io.reactivex.disposables.CompositeDisposable;

class ProgramDetailsFrequencyPresenter extends
        com.rainmachine.presentation.util.BasePresenter<ProgramDetailsFrequencyContract.View>
        implements
        ProgramDetailsFrequencyContract.Presenter {

    private static final int DIALOG_ID_EVERY_N_DAYS = 1;

    private ProgramDetailsFrequencyContract.Container container;

    private ProgramDetailsFrequencyExtra extra;
    private CompositeDisposable disposables;

    ProgramDetailsFrequencyPresenter(ProgramDetailsFrequencyContract.Container container) {
        this.container = container;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
        extra = container.getExtra();
        view.setup(extra.program);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogSelectedDaysPositiveClick(boolean[] checkedItemPositions) {
        extra.program.updateFrequencyWeekDays(checkedItemPositions);
        view.updateWeekDays(extra.program);
        updateNextRun();
    }

    @Override
    public void onDialogSelectedDaysCancel() {
        // Do nothing

    }

    @Override
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        if (dialogId == DIALOG_ID_EVERY_N_DAYS) {
            int numDays = checkedItemPosition + 2;
            extra.program.updateFrequencyEveryNDays(numDays);
            view.updateEveryNDays(extra.program);
            updateNextRun();
        }
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogDatePickerPositiveClick(int dialogId, int year, int month, int day) {
        LocalDate nextRunDate = new LocalDate(year, month + 1, day);
        LocalTime startTime = extra.program.startTime.localDateTime.toLocalTime();
        if (nextRunDate.toLocalDateTime(startTime).isBefore(extra.sprinklerLocalDateTime)) {
            Toasts.show(R.string.program_details_past_next_run);
            return;
        }
        extra.program.nextRunSprinklerLocalDate = nextRunDate;
        view.updateNextRun(extra.program);
    }

    @Override
    public void onDialogDatePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onClickBack() {
        container.closeScreen(extra.program);
    }

    @Override
    public void onCheckFrequencyDaily() {
        extra.program.updateFrequencyDaily();
        view.hideWeekDays();
        updateNextRun();
    }

    @Override
    public void onCheckFrequencyWeekdays() {
        extra.program.updateFrequencyWeekDays();
        container.showWeekDaysDialog(extra.program);
        view.updateWeekDays(extra.program);
        updateNextRun();
    }

    @Override
    public void onCheckFrequencyOddDays() {
        extra.program.updateFrequencyOddDays();
        view.hideWeekDays();
        updateNextRun();
    }

    @Override
    public void onCheckFrequencyEvenDays() {
        extra.program.updateFrequencyEvenDays();
        view.hideWeekDays();
        updateNextRun();
    }

    @Override
    public void onCheckFrequencyEveryNDays() {
        extra.program.updateFrequencyEveryNDays(extra.program.frequencyNumDays());
        container.showEveryNDaysDialog(DIALOG_ID_EVERY_N_DAYS, extra.program);
        view.hideWeekDays();
        updateNextRun();
    }

    @Override
    public void onClickNextRun() {
        if (extra.program.isEveryNDays() && extra.program.nextRunSprinklerLocalDate != null) {
            container.showNextRunDialog(extra.program.nextRunSprinklerLocalDate);
        }
    }

    private void updateNextRun() {
        extra.program.updateNextRunDate(extra.sprinklerLocalDateTime);
        view.updateNextRun(extra.program);
    }
}
