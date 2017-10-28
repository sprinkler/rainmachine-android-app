package com.rainmachine.presentation.screens.rainsensitivity;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import io.reactivex.Observable;

class RainSensitivityMixer {

    private final SprinklerRepositoryImpl sprinklerRepository;

    RainSensitivityMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<RainSensitivityViewModel> refresh() {
        return sprinklerRepository.provision()
                .toObservable()
                .map(provision -> {
                    RainSensitivityViewModel viewModel = new RainSensitivityViewModel();
                    viewModel.rainSensitivity = provision.location.rainSensitivity;
                    return viewModel;
                });
    }

    Observable<Irrelevant> saveRainSensitivity(final float rainSensitivity) {
        return sprinklerRepository
                .saveRainSensitivity(rainSensitivity)
                .toObservable()
                .compose(RunToCompletion.instance());
    }
}
