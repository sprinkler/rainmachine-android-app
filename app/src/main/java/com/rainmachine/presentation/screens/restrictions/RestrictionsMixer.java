package com.rainmachine.presentation.screens.restrictions;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletionSingle;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

class RestrictionsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GetRestrictionsLive getRestrictionsLive;

    RestrictionsMixer(SprinklerRepositoryImpl sprinklerRepository,
                      GetRestrictionsLive getRestrictionsLive) {
        this.sprinklerRepository = sprinklerRepository;
        this.getRestrictionsLive = getRestrictionsLive;
    }

    Observable<RestrictionsViewModel> refresh() {
        return Single.zip(
                sprinklerRepository.globalRestrictions(),
                sprinklerRepository.hourlyRestrictions(),
                sprinklerRepository.devicePreferences(),
                sprinklerRepository.provision(),
                (globalRestrictions, hourlyRestrictions, devicePreferences, provision) ->
                        buildViewModel(globalRestrictions, hourlyRestrictions,
                                devicePreferences, provision))
                .toObservable();
    }

    private RestrictionsViewModel buildViewModel(GlobalRestrictions globalRestrictions,
                                                 List<HourlyRestriction> hourlyRestrictions,
                                                 DevicePreferences devicePreferences,
                                                 Provision provision) {
        RestrictionsViewModel viewModel = new RestrictionsViewModel();
        viewModel.globalRestrictions = globalRestrictions;
        viewModel.hourlyRestrictions = hourlyRestrictions;
        viewModel.isUnitsMetric = devicePreferences.isUnitsMetric;
        viewModel.use24HourFormat = devicePreferences.use24HourFormat;
        viewModel.minWateringDurationThreshold = provision.system.minWateringDurationThreshold;
        viewModel.maxWateringCoefficient = (int) (provision.system.maxWateringCoefficient * 100);
        return viewModel;
    }

    Observable<Irrelevant> saveGlobalRestrictions(final RestrictionsViewModel viewModel) {
        return sprinklerRepository
                .saveGlobalRestrictions(viewModel.globalRestrictions)
                .doOnSuccess(irrelevant -> getRestrictionsLive.forceRefresh())
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveMinWateringDurationThreshold(int value) {
        return sprinklerRepository
                .saveMinWateringDurationThreshold(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }

    Observable<Irrelevant> saveMaxWateringCoefficient(int percent) {
        float value = (percent * 1.0f) / 100;
        return sprinklerRepository
                .saveMaxWateringCoefficient(value)
                .compose(RunToCompletionSingle.instance())
                .toObservable();
    }
}
