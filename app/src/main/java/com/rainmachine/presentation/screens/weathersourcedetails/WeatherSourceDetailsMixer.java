package com.rainmachine.presentation.screens.weathersourcedetails;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.netatmo.NetatmoApiDelegate;
import com.rainmachine.data.remote.wunderground.WundergroundApiDelegate;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.util.CustomDataException;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;

class WeatherSourceDetailsMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private NetatmoApiDelegate netatmoApiDelegate;
    private WundergroundApiDelegate wundergroundApiDelegate;

    WeatherSourceDetailsMixer(SprinklerRepositoryImpl sprinklerRepository,
                              NetatmoApiDelegate netatmoApiDelegate,
                              WundergroundApiDelegate wundergroundApiDelegate) {
        this.sprinklerRepository = sprinklerRepository;
        this.netatmoApiDelegate = netatmoApiDelegate;
        this.wundergroundApiDelegate = wundergroundApiDelegate;
    }

    Observable<WeatherSourceDetailViewModel> refresh(final long parserUid) {
        return sprinklerRepository
                .parser(parserUid).toObservable()
                .map(parser -> {
                    WeatherSourceDetailViewModel viewModel = new WeatherSourceDetailViewModel();
                    viewModel.parser = parser;
                    return viewModel;
                });
    }

    Observable<Irrelevant> saveParser(final Parser parser) {
        return Observable
                .just(parser.isNetatmo() && parser.enabled)
                .flatMap(shouldCheckNetatmoCredentials -> {
                    if (shouldCheckNetatmoCredentials) {
                        return netatmoApiDelegate
                                .checkCredentials(parser.netatmoParams.username, parser
                                        .netatmoParams.password)
                                .flatMapObservable(isValid -> {
                                    if (isValid) {
                                        return Observable.just(Irrelevant.INSTANCE);
                                    } else {
                                        return Observable.error(new CustomDataException
                                                (CustomDataException.CustomStatus
                                                        .INVALID_NETATMO_CREDENTIALS));
                                    }
                                });
                    } else {
                        return Observable.just(Irrelevant.INSTANCE);
                    }
                })
                .flatMap(irrelevant -> Observable.just(parser.isWUnderground() && parser.enabled))
                .flatMap(shouldCheckWUndergroundDeveloperApiKey -> {
                    if (shouldCheckWUndergroundDeveloperApiKey) {
                        return wundergroundApiDelegate
                                .checkDeveloperApiKey(parser.wUndergroundParams.apiKey)
                                .flatMapObservable(isValid -> {
                                    if (isValid) {
                                        return Observable.just(Irrelevant.INSTANCE);
                                    } else {
                                        return Observable.error(new CustomDataException
                                                (CustomDataException.CustomStatus
                                                        .INVALID_WUNDERGROUND_API_KEY));
                                    }
                                });
                    } else {
                        return Observable.just(Irrelevant.INSTANCE);
                    }
                })
                .flatMap(irrelevant -> sprinklerRepository.saveParserEnabled(parser.uid, parser
                        .enabled).toObservable())
                .flatMap(irrelevant -> {
                    if (parser.hasParams) {
                        return sprinklerRepository.saveParserParams(parser).toObservable();
                    } else {
                        return Observable.just(irrelevant);
                    }
                })
                .compose(RunToCompletion.instance());
    }

    Observable<WUndergroundDeveloperApiKeyCheck> checkWUndergroundDeveloperApiKey(String key) {
        return Single.zip(Single.just(key),
                wundergroundApiDelegate.checkDeveloperApiKey(key),
                (s, aBoolean) -> new WUndergroundDeveloperApiKeyCheck(s, aBoolean))
                .toObservable();
    }

    Observable<NetatmoCredentialsCheck> checkNetatmoCredentials(final NetatmoCredentialsCheck
                                                                        check) {
        return netatmoApiDelegate
                .checkCredentials(check.username, check.password)
                .flatMapObservable(isValid -> {
                    check.isValid = isValid;
                    return Observable.just(check);
                });
    }

    Observable<Parser> refreshNetatmoModules(final Parser parser, boolean
            initialParserEnabled) {
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
                        .switchMap(aLong -> sprinklerRepository
                                .parser(parser.uid).toObservable()
                                .filter(parser12 -> !parser12.isRunning))
                        .firstOrError()
                        .toObservable())
                .doOnNext(parser1 -> disableParser.subscribe())
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus
                            .REFRESH_NETATMO_ERROR);
                });
    }

    Observable<WeatherSourceDetailViewModel> runParser(final Parser parser, boolean
            initialParserEnabled) {
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
                .flatMap(irrelevant -> sprinklerRepository
                        .runParser(parser.uid).toObservable()
                        .flatMap(irrelevant1 -> Observable
                                .interval(0, 5, TimeUnit.SECONDS)
                                .take(20)
                                .switchMap(aLong -> sprinklerRepository.parser(parser.uid)
                                        .toObservable()
                                        .filter(parser12 -> !parser12.isRunning))
                                .firstOrError()
                                .toObservable()))
                .flatMap(parser1 -> refresh(parser.uid))
                .doOnNext(weatherSourceDetailsData -> disableParser.subscribe())
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus
                            .RUN_PARSER_ERROR);
                })
                .compose(RunToCompletion.instance());
    }

    Observable<WeatherSourceDetailViewModel> setParserDefaults(final Parser parser) {
        return sprinklerRepository
                .setParserDefaults(parser.uid).toObservable()
                .flatMap(irrelevant -> refresh(parser.uid))
                .compose(RunToCompletion.instance());
    }
}
