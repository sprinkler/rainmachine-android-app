package com.rainmachine.presentation.screens.programdetailszones;

import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.presentation.util.BasePresenter;

import io.reactivex.disposables.CompositeDisposable;

class ProgramDetailsZonesPresenter extends BasePresenter<ProgramDetailsZonesContract.View>
        implements ProgramDetailsZonesContract.Presenter {

    private ProgramDetailsZonesContract.Container container;
    private ProgramDetailsZonesContract.SuggestedDialog suggestedDialog;

    private ProgramDetailsZonesExtra extra;
    private ProgramWateringTimes programWateringTimes;
    private CompositeDisposable disposables;

    ProgramDetailsZonesPresenter(ProgramDetailsZonesContract.Container container) {
        this.container = container;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
        extra = container.getExtra();
        programWateringTimes = extra.program.wateringTimes.get(extra.positionInList);
        view.render(extra.program, programWateringTimes, zonePosition());
        container.updateTitle(programWateringTimes);
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onClickBack() {
        container.closeScreen(extra.program);
    }

    @Override
    public void onSelectedCustom() {
        programWateringTimes.active = true;
        container.showCustomZoneDurationDialog(programWateringTimes);
    }

    @Override
    public void onSelectedDoNotWater() {
        programWateringTimes.active = false;
    }

    @Override
    public void onSelectedDetermined() {
        programWateringTimes.setDetermined();
    }

    @Override
    public void onDialogZoneDurationPositiveClick(long zoneId, int duration) {
        programWateringTimes.setCustom(duration);
        view.updateCustomWatering(programWateringTimes);
    }

    @Override
    public void onDialogZoneDurationCancel(long zoneId, int duration) {
        programWateringTimes.setCustom(duration);
        view.updateCustomWatering(programWateringTimes);
    }

    @Override
    public void onClickMinus() {
        programWateringTimes.decreaseUserPercentageBy5Percent();
        view.updateDeterminedWatering(extra.program, programWateringTimes);
        suggestedDialog.render(extra.program, programWateringTimes);
    }

    @Override
    public void onClickPlus() {
        programWateringTimes.increaseUserPercentageBy5Percent();
        view.updateDeterminedWatering(extra.program, programWateringTimes);
        suggestedDialog.render(extra.program, programWateringTimes);
    }

    @Override
    public void onClickPreviousZone() {
        if (extra.positionInList > 0) {
            extra.positionInList--;
            programWateringTimes = extra.program.wateringTimes.get(extra.positionInList);
            view.render(extra.program, programWateringTimes, zonePosition());
            container.updateTitle(programWateringTimes);
        }
    }

    @Override
    public void onClickNextZone() {
        if (extra.positionInList < extra.program.wateringTimes.size() - 1) {
            extra.positionInList++;
            programWateringTimes = extra.program.wateringTimes.get(extra.positionInList);
            view.render(extra.program, programWateringTimes, zonePosition());
            container.updateTitle(programWateringTimes);
        }
    }

    @Override
    public void onClickDefaultZone() {
        container.goToZone(programWateringTimes.id);
    }

    @Override
    public void onComingBackFromChangingZoneProperties(ZoneProperties zoneProperties) {
        programWateringTimes = extra.program.wateringTimes.get(extra.positionInList);
        programWateringTimes.name = zoneProperties.name;
        programWateringTimes.referenceTime = zoneProperties.referenceTime;
        programWateringTimes.hasDefaultAdvancedSettings = zoneProperties
                .hasDefaultAdvancedSettings();

        view.render(extra.program, programWateringTimes, zonePosition());
        container.updateTitle(programWateringTimes);
    }

    @Override
    public void onShownSuggestedDialog(ProgramDetailsZonesContract.SuggestedDialog
                                               suggestedDialog) {
        this.suggestedDialog = suggestedDialog;
        suggestedDialog.render(extra.program, programWateringTimes);
    }

    private ZonePosition zonePosition() {
        if (extra.positionInList == 0) {
            return ZonePosition.FIRST;
        } else if (extra.positionInList == extra.program.wateringTimes.size() - 1) {
            return ZonePosition.LAST;
        } else {
            return ZonePosition.MIDDLE;
        }
    }
}
