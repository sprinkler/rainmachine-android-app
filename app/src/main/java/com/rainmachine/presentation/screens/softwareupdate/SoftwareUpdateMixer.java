package com.rainmachine.presentation.screens.softwareupdate;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.Versions;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.UpdateHandler;

import io.reactivex.Observable;

class SoftwareUpdateMixer {

    private Features features;
    private UpdateHandler updateHandler;
    private TriggerUpdateCheck triggerUpdateCheck;
    private SprinklerRepositoryImpl sprinklerRepository;

    SoftwareUpdateMixer(Features features, UpdateHandler updateHandler,
                        TriggerUpdateCheck triggerUpdateCheck,
                        SprinklerRepositoryImpl sprinklerRepository) {
        this.features = features;
        this.updateHandler = updateHandler;
        this.triggerUpdateCheck = triggerUpdateCheck;
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<SoftwareUpdateViewModel> refresh() {
        Observable<SoftwareUpdateViewModel> observable;
        if (features.useNewApi()) {
            observable = triggerUpdateCheck
                    .execute(new TriggerUpdateCheck.RequestModel())
                    .andThen(softwareUpdate());
        } else {
            observable = softwareUpdateObservable3();
        }
        return observable;
    }

    private Observable<SoftwareUpdateViewModel> softwareUpdate() {
        return Observable.combineLatest(
                sprinklerRepository.update(true).toObservable(),
                sprinklerRepository.versions().toObservable(),
                (update, versions) -> buildViewModel(update, versions));
    }

    private Observable<SoftwareUpdateViewModel> softwareUpdateObservable3() {
        return Observable.combineLatest(
                sprinklerRepository.update3(true).toObservable(),
                sprinklerRepository.versions().toObservable(),
                (update, versions) -> buildViewModel(update, versions));
    }

    private SoftwareUpdateViewModel buildViewModel(Update update, Versions versions) {
        SoftwareUpdateViewModel viewModel = new SoftwareUpdateViewModel();
        viewModel.update = update;
        if (Strings.isBlank(viewModel.update.currentVersion)) {
            viewModel.update.currentVersion = versions.softwareVersion;
        }
        return viewModel;
    }

    Observable<SoftwareUpdateViewModel> makeUpdate() {
        return updateHandler
                .triggerUpdate()
                .flatMap(ignored -> refresh())
                .compose(RunToCompletion.instance());
    }
}
