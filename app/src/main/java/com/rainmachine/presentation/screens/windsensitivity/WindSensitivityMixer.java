package com.rainmachine.presentation.screens.windsensitivity;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import io.reactivex.Observable;

class WindSensitivityMixer {

    private SprinklerRepositoryImpl sprinklerRepository;

    WindSensitivityMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<WindSensitivityViewModel> refresh() {
        return sprinklerRepository.provision().toObservable()
                .map(provision -> buildViewModel(provision));
    }

    private WindSensitivityViewModel buildViewModel(Provision provision) {
        WindSensitivityViewModel viewModel = new WindSensitivityViewModel();
        viewModel.windSensitivity = provision.location.windSensitivity;
        return viewModel;
    }

    Observable<Irrelevant> saveWindSensitivity(final float windSensitivity) {
        return sprinklerRepository
                .saveWindSensitivity(windSensitivity).toObservable()
                .compose(RunToCompletion.instance());
    }
}
