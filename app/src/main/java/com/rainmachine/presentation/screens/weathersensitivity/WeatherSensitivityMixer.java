package com.rainmachine.presentation.screens.weathersensitivity;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import io.reactivex.Observable;

class WeatherSensitivityMixer {

    private SprinklerRepositoryImpl sprinklerRepository;

    WeatherSensitivityMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<WeatherSensitivityViewModel> refresh() {
        return sprinklerRepository
                .provision()
                .map(provision -> {
                    WeatherSensitivityViewModel viewModel = new WeatherSensitivityViewModel();
                    viewModel.useCorrection = provision.system.useCorrectionForPast;
                    return viewModel;
                })
                .toObservable();
    }

    Observable<Irrelevant> saveUseCorrection(boolean isEnabled) {
        return sprinklerRepository
                .saveUseCorrection(isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }
}
