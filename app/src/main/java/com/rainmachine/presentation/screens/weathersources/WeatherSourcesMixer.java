package com.rainmachine.presentation.screens.weathersources;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.domain.model.LocationDetails;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.infrastructure.StreamUtils;
import com.rainmachine.infrastructure.util.RainApplication;
import com.rainmachine.presentation.util.CustomDataException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

class WeatherSourcesMixer {

    private OkHttpClient okHttpClient;
    private SprinklerRepositoryImpl sprinklerRepository;
    private GoogleApiDelegate googleApiDelegate;

    WeatherSourcesMixer(OkHttpClient okHttpClient, SprinklerRepositoryImpl sprinklerRepository,
                        GoogleApiDelegate googleApiDelegate) {
        this.okHttpClient = okHttpClient;
        this.sprinklerRepository = sprinklerRepository;
        this.googleApiDelegate = googleApiDelegate;
    }

    Observable<WeatherSourcesViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.parsers().toObservable(),
                sprinklerRepository.provision().toObservable()
                        .flatMap(provision -> googleApiDelegate
                                .detailsBasedOnLocation(provision.location.latitude,
                                        provision.location.longitude)
                                .onErrorReturn(throwable -> DomainUtils.inferredLocation(provision))
                                .toObservable()),
                (parsers, locationDetails) -> buildViewModel(parsers, locationDetails));
    }

    private WeatherSourcesViewModel buildViewModel(List<Parser> parsers, LocationDetails
            locationDetails) {
        WeatherSourcesViewModel viewModel = new WeatherSourcesViewModel();
        viewModel.sources = new ArrayList<>(parsers.size());
        boolean isNorthAmerica = locationDetails.isInNorthAmerica();
        for (Parser parser : parsers) {
            if (!parser.enabled) {
                // Exclude the default parser because it always appears in the weather screen
                if ((parser.isNOAA() && isNorthAmerica) || (parser.isMETNO() && !isNorthAmerica)) {
                    continue;
                }
                // Exclude parsers that can only be used in certain locations of the globe
                if ((parser.isCIMIS() && !locationDetails.isInCalifornia()) || (parser.isFAWN()
                        && !locationDetails.isInFlorida())) {
                    continue;
                }
                WeatherSource sourceData = new WeatherSource();
                sourceData.parser = parser;
                viewModel.sources.add(sourceData);
            }
        }
        Collections.sort(viewModel.sources, new WeatherSource.Comparator());
        return viewModel;
    }

    Observable<Irrelevant> saveParserEnabled(Parser parser, boolean isEnabled) {
        return sprinklerRepository
                .saveParserEnabled(parser.uid, isEnabled).toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> addDataSource(final String url) {
        return Observable.create(subscriber -> {
            try {
                Request request = new Request.Builder().url(url).build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response.code());
                }
                InputStream inputStream = response.body().byteStream();
                String filename = getFilenameFromUrl(url);
                File file = new File(RainApplication.get().getFilesDir(), filename);
                boolean success = StreamUtils.saveFile(file, inputStream);
                if (!success) {
                    throw new IOException("Could not save file locally");
                }

                sprinklerRepository
                        .addParser(filename, file).toObservable()
                        .blockingFirst();

                subscriber.onNext(Irrelevant.INSTANCE);
                subscriber.onComplete();
            } catch (Exception e) {
                Timber.w(e, e.getMessage());
                subscriber.onError(new CustomDataException(CustomDataException.CustomStatus
                        .ADD_WEATHER_SOURCE_ERROR));
            }
        });
    }

    private String getFilenameFromUrl(String url) {
        int lastIndex = url.lastIndexOf('/');
        if (url.endsWith("/")) {
            lastIndex = url.substring(0, lastIndex).lastIndexOf('/');
        }
        if (lastIndex >= 0) {
            return url.substring(lastIndex + 1);
        } else {
            return System.currentTimeMillis() + "_custom_data_source.py";
        }
    }
}
