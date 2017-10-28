package com.rainmachine.presentation.screens.location;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.domain.model.Autocomplete;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.usecases.backup.GetBackups;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.Toasts;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class LocationMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GoogleApiDelegate googleApiDelegate;
    private GetBackups getBackups;
    private Features features;

    LocationMixer(SprinklerRepositoryImpl sprinklerRepository, GoogleApiDelegate
            googleApiDelegate, GetBackups getBackups, Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.googleApiDelegate = googleApiDelegate;
        this.getBackups = getBackups;
        this.features = features;
    }

    Observable<List<Autocomplete>> refreshAutocomplete(final CharSequence text) {
        return googleApiDelegate.autocomplete(text).toObservable();
    }

    Observable<LocationInfo> refreshPlace(final Autocomplete place) {
        return googleApiDelegate
                .placeDetails(place.placeId)
                .map(location -> {
                    // Keep the address selected by user as the full address
                    location.fullAddress = place.description;
                    return location;
                })
                .doOnError(throwable -> Toasts.show(R.string
                        .location_could_not_retrieve_coordinates))
                .toObservable();
    }

    Observable<LocationInfo> refreshReverseGeocode(final double latitude, final double
            longitude) {
        return googleApiDelegate
                .reverseGeocode(latitude, longitude)
                .onErrorReturn(throwable -> {
                    LocationInfo location = new LocationInfo();
                    location.latitude = latitude;
                    location.longitude = longitude;
                    return location;
                })
                .toObservable();
    }

    Observable<BackupViewModel> saveLocation(final LocationInfo location, final boolean
            isWizard) {
        return Single.zip(
                googleApiDelegate.timezone(location.latitude, location.longitude),
                googleApiDelegate.elevation(location.latitude, location.longitude),
                (timezoneId, elevation) -> new LocationInfoRequest(location, timezoneId, elevation))
                .toObservable()
                .flatMap(locationInfoRequest -> networkSaveLocation(locationInfoRequest))
                .flatMap(irrelevant -> {
                    if (isWizard && features.hasBackup()) {
                        return getBackups
                                .execute(new GetBackups.RequestModel())
                                .map(responseModel -> responseModel.deviceBackups.size() > 0);
                    } else {
                        return Observable.just(false);
                    }
                })
                .map(hasBackups -> {
                    BackupViewModel viewModel = new BackupViewModel();
                    viewModel.hasBackups = hasBackups;
                    return viewModel;
                })
                .compose(RunToCompletion.instance());
    }

    private Observable<Irrelevant> networkSaveLocation(final LocationInfoRequest
                                                               locationInfoRequest) {
        return sprinklerRepository
                .saveLocation(locationInfoRequest.location, locationInfoRequest.timezone,
                        locationInfoRequest.elevation).toObservable()
                .doOnNext(irrelevant -> {
                    if (Strings.isBlank(locationInfoRequest.location.country)) {
                        return;
                    }
                    final boolean isNorthAmerica = locationInfoRequest.location
                            .isFromNorthAmerica();
                    sprinklerRepository
                            .parsers().toObservable()
                            .flatMap(parsers -> {
                                Observable<Irrelevant> observableNOAA = null;
                                Observable<Irrelevant> observableMETNO = null;
                                for (Parser parser : parsers) {
                                    if (parser.isNOAA()) {
                                        if (isNorthAmerica && !parser.enabled) {
                                            observableNOAA = sprinklerRepository
                                                    .saveParserEnabled(parser.uid, true)
                                                    .toObservable();
                                        }
                                        if (!isNorthAmerica && parser.enabled) {
                                            observableNOAA = sprinklerRepository
                                                    .saveParserEnabled(parser.uid, false).toObservable();
                                        }
                                    } else if (parser.isMETNO()) {
                                        if (isNorthAmerica && parser.enabled) {
                                            observableMETNO = sprinklerRepository
                                                    .saveParserEnabled(parser.uid, false).toObservable();
                                        }
                                        if (!isNorthAmerica && !parser.enabled) {
                                            observableMETNO = sprinklerRepository
                                                    .saveParserEnabled(parser.uid, true).toObservable();
                                        }
                                    }
                                }
                                if (observableNOAA == null && observableMETNO == null) {
                                    return Observable.empty();
                                }
                                if (observableNOAA != null && observableMETNO != null) {
                                    return Observable.combineLatest(observableNOAA,
                                            observableMETNO, (irrelevant1, irrelevant2) ->
                                                    Irrelevant.INSTANCE);
                                }
                                if (observableNOAA != null) {
                                    return observableNOAA;
                                } else {
                                    return observableMETNO;
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .onErrorResumeNext(Observable.empty())
                            .subscribe();
                });
    }

    private static class LocationInfoRequest {
        public LocationInfo location;
        public String timezone;
        public double elevation;

        LocationInfoRequest(LocationInfo location, String timezone, double elevation) {
            this.location = location;
            this.timezone = timezone;
            this.elevation = elevation;
        }
    }
}
