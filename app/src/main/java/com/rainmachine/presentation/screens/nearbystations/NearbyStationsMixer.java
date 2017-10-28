package com.rainmachine.presentation.screens.nearbystations;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.util.CustomDataException;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

class NearbyStationsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;

    NearbyStationsMixer(SprinklerRepositoryImpl sprinklerRepository) {
        this.sprinklerRepository = sprinklerRepository;
    }

    Observable<NearbyStationsViewModel> refresh(final Parser parser, boolean initialParserEnabled) {
        return Observable.combineLatest(
                sprinklerRepository.provision().toObservable(),
                parserSetup(parser, initialParserEnabled),
                (provision, parser1) -> {
                    NearbyStationsViewModel viewModel = new NearbyStationsViewModel();
                    viewModel.parser = parser1;
                    viewModel.currentLocationAddress = provision.location.name;
                    viewModel.currentLocationLatitude = provision.location.latitude;
                    viewModel.currentLocationLongitude = provision.location.longitude;
                    return viewModel;
                });
    }

    private Observable<Parser> parserSetup(final Parser parser, boolean initialParserEnabled) {
        Observable<Irrelevant> enableParser;
        if (initialParserEnabled) {
            enableParser = Observable.just(Irrelevant.INSTANCE);
        } else {
            enableParser = sprinklerRepository.saveParserEnabled(parser.uid, true).toObservable();
        }
        final Observable<Irrelevant> disableParser;
        if (initialParserEnabled) {
            disableParser = Observable.just(Irrelevant.INSTANCE);
        } else {
            disableParser = sprinklerRepository.saveParserEnabled(parser.uid, false).toObservable();
        }
        return enableParser
                .flatMap(irrelevant -> sprinklerRepository.saveParserParams(parser).toObservable())
                .flatMap(irrelevant -> sprinklerRepository.runParser(parser.uid).toObservable())
                .flatMap(irrelevant -> Observable
                        .interval(0, 5, TimeUnit.SECONDS)
                        .take(20)
                        .switchMap(aLong -> sprinklerRepository.parser(parser.uid).toObservable())
                        .filter(parser12 -> !parser12.isRunning)
                        .firstOrError()
                        .toObservable())
                .doOnNext(parser1 -> disableParser.subscribe())
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus.PARSER_ERROR);
                });
    }

    Observable<Irrelevant> saveParserParams(Parser parser) {
        return sprinklerRepository
                .saveParserParams(parser).toObservable()
                .compose(RunToCompletion.instance());
    }
}
