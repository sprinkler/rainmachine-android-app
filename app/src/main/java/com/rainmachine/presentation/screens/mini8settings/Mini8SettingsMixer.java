package com.rainmachine.presentation.screens.mini8settings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletionCompletable;
import com.rainmachine.domain.util.RunToCompletionSingle;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

class Mini8SettingsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;

    Mini8SettingsMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<Mini8SettingsViewModel> refresh() {
        return Single.zip(
                sprinklerRepository.provision(),
                sprinklerRepository.programs(false),
                ((provision, programs) -> {
                    Mini8SettingsViewModel viewModel = new Mini8SettingsViewModel();
                    viewModel.touchAdvanced = provision.system.touchAdvanced;
                    viewModel.showRestrictionsOnLed = provision.system.showRestrictionsOnLed;
                    viewModel.minLedBrightness = provision.system.minLEDBrightness;
                    viewModel.maxLedBrightness = provision.system.maxLEDBrightness;
                    viewModel.touchSleepTimeout = provision.system.touchSleepTimeout;
                    viewModel.touchLongPressTimeout = provision.system.touchLongPressTimeout;

                    viewModel.programs = new ArrayList<>();
                    for (Program program : programs) {
                        TouchProgramViewModel touchProgram = new TouchProgramViewModel(program
                                .id, program.name);
                        viewModel.programs.add(touchProgram);
                    }

                    if (!provision.system.touchCyclePrograms) {
                        viewModel.touchProgramToRun = TouchProgramViewModel.NOT_SET;
                    } else {
                        if (provision.system.touchProgramToRun == Provision.TouchProgram.NONE) {
                            TouchProgramViewModel firstProgram = null;
                            for (TouchProgramViewModel touchProgram : viewModel.programs) {
                                if (firstProgram == null || touchProgram.id < firstProgram.id) {
                                    firstProgram = touchProgram;
                                }
                            }
                            viewModel.touchProgramToRun = firstProgram != null ? firstProgram :
                                    TouchProgramViewModel.NOT_SET;
                        } else {
                            viewModel.touchProgramToRun = TouchProgramViewModel.NOT_SET;
                            for (TouchProgramViewModel touchProgram : viewModel.programs) {
                                if (provision.system.touchProgramToRun.programId() ==
                                        touchProgram.id) {
                                    viewModel.touchProgramToRun = touchProgram;
                                    break;
                                }
                            }
                        }
                    }
                    viewModel.programs.add(0, TouchProgramViewModel.NOT_SET);
                    return viewModel;
                }))
                .toObservable();
    }

    Observable<Irrelevant> saveTouchProgramToRun(long programId) {
        return sprinklerRepository
                .saveTouchProgramToRun((int) programId)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveTouchAdvanced(boolean isEnabled) {
        return sprinklerRepository
                .saveTouchAdvanced(isEnabled)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveMinLedBrightness(int value) {
        return sprinklerRepository
                .saveMinLedBrightness(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveMaxLedBrightness(int value) {
        return sprinklerRepository
                .saveMaxLedBrightness(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveTouchSleepTimeout(int value) {
        return sprinklerRepository
                .saveTouchSleepTimeout(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveTouchLongPressTimeout(int value) {
        return sprinklerRepository
                .saveTouchLongPressTimeout(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveRestrictionsOnLed(boolean isEnabled) {
        return sprinklerRepository
                .saveRestrictionsOnLed(isEnabled)
                .compose(RunToCompletionCompletable.instance())
                .toObservable();
    }
}
