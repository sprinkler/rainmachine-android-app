package com.rainmachine.presentation.screens.advancedsettings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.usecases.handpreference.GetHandPreference;
import com.rainmachine.domain.usecases.handpreference.SaveHandPreference;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import io.reactivex.Observable;

class AdvancedSettingsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GetHandPreference getHandPreference;
    private SaveHandPreference saveHandPreference;

    AdvancedSettingsMixer(SprinklerRepositoryImpl sprinklerRepository,
                          GetHandPreference getHandPreference,
                          SaveHandPreference saveHandPreference) {
        this.sprinklerRepository = sprinklerRepository;
        this.getHandPreference = getHandPreference;
        this.saveHandPreference = saveHandPreference;
    }

    Observable<AdvancedSettingsViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.provision().toObservable(),
                sprinklerRepository.betaUpdates().toObservable(),
                sprinklerRepository.devicePreferences().toObservable(),
                getHandPreference.execute(new GetHandPreference.RequestModel())
                        .map(responseModel -> responseModel.handPreference)
                        .toObservable(),
                (provision, betaUpdates, devicePreferences, handPreference) -> {
                    AdvancedSettingsViewModel viewModel = new AdvancedSettingsViewModel();
                    viewModel.amazonAlexa = provision.system.allowAlexaDiscovery;
                    viewModel.betaUpdates = betaUpdates;
                    viewModel.bonjourService = provision.system.useBonjourService;
                    // there is no API to get the value so we show Normal by default
                    viewModel.logLevel = LogLevel.NORMAL;
                    viewModel.handPreference = handPreference;
                    return viewModel;
                });
    }

    Observable<Irrelevant> saveAmazonAlexa(boolean isEnabled) {
        return sprinklerRepository
                .saveAmazonAlexa(isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveBetaUpdates(boolean isEnabled) {
        return sprinklerRepository
                .saveBetaUpdates(isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveBonjourService(boolean isEnabled) {
        return sprinklerRepository
                .saveBonjourService(isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveSshAccess(boolean isEnabled) {
        return sprinklerRepository
                .saveSshAccess(isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveLogLevel(LogLevel logLevel) {
        int value = logLevel == LogLevel.DEBUG ? 10 : (logLevel == LogLevel.NORMAL ? 20 : 30);
        return sprinklerRepository
                .saveLogLevel(value).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveHandPreference(HandPreference handPreference) {
        return saveHandPreference
                .execute(new SaveHandPreference.RequestModel(handPreference))
                .map(responseModel -> Irrelevant.INSTANCE)
                .compose(RunToCompletion.instance());
    }
}
