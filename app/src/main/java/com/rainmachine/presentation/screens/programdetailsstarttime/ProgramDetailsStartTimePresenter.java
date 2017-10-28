package com.rainmachine.presentation.screens.programdetailsstarttime;

import com.rainmachine.domain.model.ProgramStartTime;

import org.joda.time.LocalDateTime;

import io.reactivex.disposables.CompositeDisposable;

class ProgramDetailsStartTimePresenter extends
        com.rainmachine.presentation.util.BasePresenter<ProgramDetailsStartTimeContract.View>
        implements
        ProgramDetailsStartTimeContract.Presenter {

    private ProgramDetailsStartTimeContract.Container container;

    private ProgramDetailsStartTimeExtra extra;
    private CompositeDisposable disposables;

    ProgramDetailsStartTimePresenter(ProgramDetailsStartTimeContract.Container container) {
        this.container = container;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
        extra = container.getExtra();
        view.setup(extra.program, extra.use24HourFormat);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogStartTimeSunriseSunsetPositiveClick(ProgramStartTime programStartTime) {
        extra.program.startTime.type = programStartTime.type;
        extra.program.startTime.sunPosition = programStartTime.sunPosition;
        extra.program.startTime.beforeAfter = programStartTime.beforeAfter;
        extra.program.startTime.offsetMinutes = programStartTime.offsetMinutes;
        view.updateSunriseSunset(extra.program);
    }

    @Override
    public void onDialogStartTimeSunriseSunsetCancel() {
        // Do nothing
    }

    @Override
    public void onDialogTimePickerPositiveClick(int dialogId, int hourOfDay, int minute) {
        LocalDateTime dateTime = extra.program.startTime.localDateTime;
        extra.program.startTime.localDateTime = dateTime.withHourOfDay(hourOfDay).withMinuteOfHour
                (minute);
        view.updateStartTimeOfDay(extra.program, extra.use24HourFormat);
    }

    @Override
    public void onDialogTimePickerCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onClickBack() {
        extra.program.updateNextRunDate(extra.sprinklerLocalDateTime);
        container.closeScreen(extra.program);
    }

    @Override
    public void onClickStartTime() {
        extra.program.startTime.type = ProgramStartTime.StartTimeType.TIME_OF_DAY;
        container.showTimeOfDayDialog(extra.program, extra.use24HourFormat);
    }

    @Override
    public void onClickSunriseSunset() {
        ProgramStartTime currentStartTime = extra.program.startTime;
        ProgramStartTime programStartTime = new ProgramStartTime();
        programStartTime.type = ProgramStartTime.StartTimeType.SUN_POSITION;
        programStartTime.offsetMinutes = currentStartTime.offsetMinutes;
        programStartTime.beforeAfter = currentStartTime.beforeAfter;
        programStartTime.sunPosition = currentStartTime.sunPosition;
        container.showSunriseSunsetDialog(programStartTime);
    }
}
