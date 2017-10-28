package com.rainmachine.presentation.screens.weathersettings;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.domain.model.LocationDetails;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.screens.weathersources.WeatherSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

class WeatherSettingsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GoogleApiDelegate googleApiDelegate;

    WeatherSettingsMixer(SprinklerRepositoryImpl sprinklerRepository,
                         GoogleApiDelegate googleApiDelegate) {
        this.sprinklerRepository = sprinklerRepository;
        this.googleApiDelegate = googleApiDelegate;
    }

    public Observable<WeatherSettingsViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.parsers().toObservable(),
                sprinklerRepository
                        .provision().toObservable()
                        .map(provision -> {
                            TransitionRefreshData data = new TransitionRefreshData();
                            data.provision = provision;
                            return data;
                        })
                        .flatMap(transitionData -> googleApiDelegate
                                .detailsBasedOnLocation(transitionData.provision.location
                                        .latitude, transitionData.provision.location
                                        .longitude)
                                .onErrorReturn(throwable -> DomainUtils.inferredLocation
                                        (transitionData.provision))
                                .map(locationDetails -> {
                                    transitionData.locationDetails = locationDetails;
                                    return transitionData;
                                })
                                .toObservable()),
                (parsers, transitionData) -> {
                    WeatherSettingsViewModel viewModel = new WeatherSettingsViewModel();
                    viewModel.enabledSources = new ArrayList<>();
                    for (Parser parser : parsers) {
                        if (parser.isNOAA() && transitionData.locationDetails
                                .isInNorthAmerica()) {
                            WeatherSource sourceData = new WeatherSource();
                            sourceData.parser = parser;
                            viewModel.defaultSource = sourceData;
                        } else if (parser.isMETNO() && !transitionData.locationDetails
                                .isInNorthAmerica()) {
                            WeatherSource sourceData = new WeatherSource();
                            sourceData.parser = parser;
                            viewModel.defaultSource = sourceData;
                        } else if (parser.enabled) {
                            WeatherSource sourceData = new WeatherSource();
                            sourceData.parser = parser;
                            viewModel.enabledSources.add(sourceData);
                        } else {
                            // Exclude counting parsers that can only be used in certain
                            // locations of the globe
                            if ((parser.isCIMIS() && !transitionData.locationDetails
                                    .isInCalifornia()) || (parser.isFAWN() && !transitionData
                                    .locationDetails.isInFlorida())) {
                                continue;
                            }
                            viewModel.numDisabledSources++;
                        }
                    }
                    Collections.sort(viewModel.enabledSources, new WeatherSource.Comparator());

                    viewModel.rainSensitivity = transitionData.provision.location.rainSensitivity;
                    viewModel.isRainSensitivityChanged = viewModel.rainSensitivity != DomainUtils
                            .DEFAULT_RAIN_SENSITIVITY;
                    viewModel.fieldCapacity = transitionData.provision.location.wsDays;
                    viewModel.isFieldCapacityChanged = viewModel.fieldCapacity != DomainUtils
                            .DEFAULT_WS_DAYS;
                    viewModel.windSensitivity = transitionData.provision.location.windSensitivity;
                    viewModel.isWindSensitivityChanged = viewModel.windSensitivity != DomainUtils
                            .DEFAULT_WIND_SENSITIVITY;
                    return viewModel;
                });
    }

    private static class TransitionRefreshData {
        Provision provision;
        LocationDetails locationDetails;
    }

    Observable<WeatherSettingsViewModel> defaults(boolean isWeatherSensitivityChanged) {
        List<Observable<Irrelevant>> observables = new ArrayList<>(2);
        observables.add(defaultsParsers());
        if (isWeatherSensitivityChanged) {
            observables.add(sprinklerRepository
                    .saveWeatherSensitivity(DomainUtils.DEFAULT_RAIN_SENSITIVITY, DomainUtils
                            .DEFAULT_WS_DAYS, DomainUtils.DEFAULT_WIND_SENSITIVITY).toObservable());
        }
        return Observable
                .combineLatest(observables, args -> Irrelevant.INSTANCE)
                .flatMap(irrelevant -> refresh())
                .compose(RunToCompletion.instance());
    }

    private Observable<Irrelevant> defaultsParsers() {
        return Observable.combineLatest(
                sprinklerRepository.parsers().toObservable(),
                sprinklerRepository.provision().toObservable()
                        .flatMap(provision -> googleApiDelegate
                                .detailsBasedOnLocation(provision.location.latitude,
                                        provision.location.longitude)
                                .onErrorReturn(throwable -> DomainUtils.inferredLocation(provision))
                                .toObservable()),
                (parsers, locationDetails) -> {
                    TransitionDefaultsData data = new TransitionDefaultsData();
                    data.parsers = parsers;
                    data.locationDetails = locationDetails;
                    return data;
                })
                .flatMap(transitionData -> {
                    final boolean isNorthAmerica = transitionData.locationDetails
                            .isInNorthAmerica();
                    List<Observable<Irrelevant>> observables = new ArrayList<>();
                    for (final Parser parser : transitionData.parsers) {
                        if (parser.enabled) {
                            if ((parser.isNOAA() && isNorthAmerica) || (parser.isMETNO() &&
                                    !isNorthAmerica)) {
                                continue;
                            }
                            observables.add(sprinklerRepository
                                    .saveParserEnabled(parser.uid, false).toObservable());
                        } else {
                            if ((parser.isNOAA() && isNorthAmerica) || (parser.isMETNO() &&
                                    !isNorthAmerica)) {
                                observables.add(sprinklerRepository
                                        .saveParserEnabled(parser.uid, true).toObservable());
                            }
                        }
                    }
                    if (observables.isEmpty()) {
                        return Observable.just(Irrelevant.INSTANCE);
                    }
                    return Observable
                            .combineLatest(observables, args -> Irrelevant.INSTANCE);
                });
    }

    private static class TransitionDefaultsData {
        List<Parser> parsers;
        LocationDetails locationDetails;
    }
}
