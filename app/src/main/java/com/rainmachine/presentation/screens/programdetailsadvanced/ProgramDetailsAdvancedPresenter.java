package com.rainmachine.presentation.screens.programdetailsadvanced;

import com.rainmachine.presentation.util.BasePresenter;

import io.reactivex.disposables.CompositeDisposable;

class ProgramDetailsAdvancedPresenter extends BasePresenter<ProgramDetailsAdvancedContract.View>
        implements ProgramDetailsAdvancedContract.Presenter {

    private static final int DIALOG_ID_DELAY_ZONES = 0;
    private static final int DIALOG_ID_CYCLE_SOAK = 1;
    private static final int DIALOG_ID_DO_NOT_RUN = 2;

    private ProgramDetailsAdvancedContract.Container container;

    private ProgramDetailsAdvancedExtra extra;
    private CompositeDisposable disposables;

    ProgramDetailsAdvancedPresenter(ProgramDetailsAdvancedContract.Container container) {
        this.container = container;
        extra = container.getExtra();
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ProgramDetailsAdvancedContract.View view) {
        super.attachView(view);
        view.setup(extra.program, extra.isUnitsMetric);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        if (dialogId == DIALOG_ID_DO_NOT_RUN) {
            extra.program.maxRainAmountMm = container.getMaxRainAmount(checkedItemPosition);
            view.updateMaxAmountNotRun(extra.program, extra.isUnitsMetric);
        }
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogClickableRadioOptionsItem(int dialogId, String[] items, int
            clickedItemPosition) {
        if (dialogId == DIALOG_ID_DELAY_ZONES) {
            if (clickedItemPosition == 0) {
                container.showCustomDelayZonesDialog(extra.program);
            } else if (clickedItemPosition == 1) {
                extra.program.isDelayEnabled = false;
                extra.program.delaySeconds = 0;
                view.updateDelayZones(extra.program);
            }
        } else if (dialogId == DIALOG_ID_CYCLE_SOAK) {
            if (clickedItemPosition == 0) {
                container.showCustomCycleSoakDialog(extra.program);
            } else if (clickedItemPosition == 1) {
                extra.program.setCycleSoakAuto();
                view.updateCycleSoak(extra.program);
            } else if (clickedItemPosition == 2) {
                extra.program.isCycleSoakEnabled = false;
                extra.program.numCycles = 0;
                view.updateCycleSoak(extra.program);
            }
        }
    }

    @Override
    public void onDialogClickableRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogStationDelayPositiveClick(int duration) {
        extra.program.isDelayEnabled = duration > 0;
        extra.program.delaySeconds = duration;
        view.updateDelayZones(extra.program);
    }

    @Override
    public void onDialogStationDelayCancel() {
        view.updateDelayZones(extra.program);
    }

    @Override
    public void onDialogCycleSoakPositiveClick(int cycles, int soak) {
        extra.program.isCycleSoakEnabled = true;
        extra.program.numCycles = cycles;
        extra.program.soakSeconds = soak;
        view.updateCycleSoak(extra.program);
    }

    @Override
    public void onDialogCycleSoakCancel() {
        view.updateCycleSoak(extra.program);
    }

    @Override
    public void onChangeAdjustWeatherTimes(boolean isChecked) {
        extra.program.ignoreWeatherData = !isChecked;
    }

    @Override
    public void onClickCycleSoak() {
        container.showCycleSoakDialog(DIALOG_ID_CYCLE_SOAK, extra.program);
    }

    @Override
    public void onClickDelayZones() {
        container.showDelayZonesDialog(DIALOG_ID_DELAY_ZONES, extra.program);
    }

    @Override
    public void onClickDoNotRun() {
        container.showDoNotRunDialog(DIALOG_ID_DO_NOT_RUN, extra.program, extra.isUnitsMetric);
    }

    @Override
    public void onClickBack() {
        container.closeScreen(extra.program);
    }
}
