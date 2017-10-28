package com.rainmachine.presentation.screens.programdetails;

import com.rainmachine.R;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class ProgramDetailsPresenter extends BasePresenter<ProgramDetailsContract.View> implements
        ProgramDetailsContract.Presenter {

    private ProgramDetailsContract.Container container;
    private SaveProgram saveProgram;

    private ProgramDetailsExtra extra;
    private CompositeDisposable disposables;

    ProgramDetailsPresenter(ProgramDetailsContract.Container container, SaveProgram saveProgram) {
        this.container = container;
        this.saveProgram = saveProgram;
        extra = container.getExtra();
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ProgramDetailsContract.View view) {
        super.attachView(view);
        view.setup(extra.program, extra.isUnitsMetric, extra.use24HourFormat);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        leaveScreen();
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing. Stay on this screen
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onClickDiscardOrBack() {
        if (hasUnsavedChanges()) {
            confirmLeaveScreen();
        } else {
            leaveScreen();
        }
    }

    @Override
    public void onClickSave() {
        if (!canSaveProgram()) {
            return;
        }
        view.showProgress();
        container.toggleCustomActionBar(false);
        disposables.add(saveProgram
                .execute(new SaveProgram.RequestModel(extra.program, extra.originalProgram, extra
                        .use24HourFormat))
                .compose(RunOnProperThreads.instance())
                .doOnNext(responseModel -> Toasts.show(R.string
                        .program_details_success_save_program))
                .doOnError(GenericErrorDealer.INSTANCE)
                .subscribeWith(new SaveProgramSubscriber()));
    }

    @Override
    public void onClickProgramZone(int positionInList) {
        container.goToProgramZone(extra.program, positionInList, extra.sprinklerLocalDateTime);
    }

    @Override
    public void onClickStartTime() {
        container.goToStartTimeScreen(extra.program, extra.sprinklerLocalDateTime, extra
                .use24HourFormat);
    }

    @Override
    public void onClickFrequency() {
        container.goToFrequencyScreen(extra.program, extra.sprinklerLocalDateTime);
    }

    @Override
    public void onChangeProgramName(String name) {
        extra.program.name = name;
    }

    @Override
    public void onClickAdvancedSettings() {
        container.goToAdvancedScreen(extra.program, extra.isUnitsMetric);
    }

    @Override
    public void onComingBackFromZones(Program program) {
        extra.program = program;
        view.updateWateringTimes(extra.program);
        view.updateTotalWatering(extra.program);
    }

    @Override
    public void onComingBackFromFrequency(Program program) {
        extra.program = program;
        view.updateFrequency(extra.program);
        view.updateNextRun(extra.program, extra.use24HourFormat);
        view.updateWateringTimes(program);
        view.updateTotalWatering(extra.program);
    }

    @Override
    public void onComingBackFromStartTime(Program program) {
        extra.program = program;
        view.updateStartTime(extra.program, extra.use24HourFormat);
        view.updateNextRun(extra.program, extra.use24HourFormat);
    }

    @Override
    public void onComingBackFromAdvanced(Program program) {
        extra.program = program;
    }

    @Override
    public void onClickMinus() {
        extra.program.decreaseWateringBy5Percent();
        view.updateWateringTimes(extra.program);
        view.updateTotalWatering(extra.program);
    }

    @Override
    public void onClickPlus() {
        extra.program.increaseWateringBy5Percent();
        view.updateWateringTimes(extra.program);
        view.updateTotalWatering(extra.program);
    }

    private boolean canSaveProgram() {
        if (extra.program.isWeekDays()) {
            if (!DomainUtils.isAtLeastOneWeekDaySelected(extra.program.frequencyWeekDays())) {
                view.showErrorNoDaySelectedMessage();
                return false;
            }
        }

        boolean hasAtLeastOneWateringTime = false;
        for (ProgramWateringTimes wtr : extra.program.wateringTimes) {
            if (wtr.active) {
                hasAtLeastOneWateringTime = true;
                break;
            }
        }

        if (!hasAtLeastOneWateringTime) {
            view.showErrorAtLeastOneWateringTime();
            return false;
        }

        return true;
    }

    private boolean hasUnsavedChanges() {
        // If this is a new program, of course it has unsaved changes
        if (extra.program.isNew()) {
            return true;
        }
        if (!extra.originalProgram.name.equals(extra.program.name)) {
            return true;
        }
        return isProgramDifferent(extra.program, extra.originalProgram);
    }

    private boolean isProgramDifferent(Program prog1, Program prog2) {
        if (prog1.enabled != prog2.enabled) {
            return true;
        }
        if (prog1.ignoreWeatherData != prog2.ignoreWeatherData) {
            return true;
        }
        if (Program.isFrequencyDifferent(prog1, prog2)) {
            return true;
        }

        if (!prog1.startTime.equals(prog2.startTime)) {
            return true;
        }

        if (prog1.isCycleSoakEnabled != prog2.isCycleSoakEnabled) {
            return true;
        }
        if (prog1.isCycleSoakEnabled && prog1.numCycles != prog2.numCycles) {
            return true;
        }
        if (prog1.isCycleSoakEnabled && prog1.soakSeconds != prog2.soakSeconds) {
            return true;
        }
        if (prog1.isDelayEnabled != prog2.isDelayEnabled) {
            return true;
        }
        if (prog1.isDelayEnabled && prog1.delaySeconds != prog2.delaySeconds) {
            return true;
        }
        if (prog1.maxRainAmountMm != prog2.maxRainAmountMm) {
            return true;
        }

        for (int i = 0; i < prog1.wateringTimes.size(); i++) {
            ProgramWateringTimes wt1 = prog1.wateringTimes.get(i);
            ProgramWateringTimes wt2 = prog2.wateringTimes.get(i);
            if (wt1.isDoNotWater() && !wt2.isDoNotWater()) {
                return true;
            }
            if (wt1.isCustom() && !wt2.isCustom()) {
                return true;
            }
            if (wt1.isDetermined() && !wt2.isDetermined()) {
                return true;
            }
            if (wt1.active != wt2.active) {
                return true;
            }
            if (wt1.active && (wt1.duration != wt2.duration || wt1.userPercentage != wt2
                    .userPercentage)) {
                return true;
            }
        }
        return false;
    }

    private void confirmLeaveScreen() {
        container.showDiscardDialog();
    }

    private void leaveScreen() {
        container.closeScreen();
    }

    private final class SaveProgramSubscriber extends DisposableObserver<SaveProgram
            .ResponseModel> {

        @Override
        public void onNext(SaveProgram.ResponseModel responseModel) {
            container.closeScreen();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showErrorSaveMessage();
            view.showContent();
            container.toggleCustomActionBar(true);
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
