package com.rainmachine.presentation.screens.nearbystations;

import android.app.Activity;
import android.content.Intent;

import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.weathersourcedetails.WeatherSourceDetailsActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class NearbyStationsPresenter extends BasePresenter<NearbyStationsView> {

    private NearbyStationsActivity activity;
    private NearbyStationsMixer mixer;

    private final CompositeDisposable disposables;

    private NearbyStationsViewModel viewModel;
    private boolean showPersonal;

    NearbyStationsPresenter(NearbyStationsActivity activity, NearbyStationsMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(NearbyStationsView view) {
        super.attachView(view);

        view.setup();
    }

    @Override
    public void init() {
    }

    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onClickRetry() {
        refresh();
    }

    private void refresh() {
        showProgress();
        NearbyStationsExtra extra = activity.getParcelable(NearbyStationsActivity.EXTRA);
        disposables.add(mixer
                .refresh(extra.parser, extra.initialParserEnabled)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    public void onClickSave() {
        showProgress();
        disposables.add(mixer
                .saveParserParams(viewModel.parser)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickDiscard() {
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }

    public void onCheckedChangedPersonalWeatherStations(boolean isChecked) {
        showPersonal = isChecked;
        updateContent();
    }

    private void showProgress() {
        view.showProgress();
        activity.hideActionBarView();
    }

    private void updateContent() {
        view.updateContent(viewModel, showPersonal);
        view.showContent();
        NearbyStationsLocationFragment fragment = (NearbyStationsLocationFragment) activity
                .getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.updateContent(viewModel, showPersonal);
        activity.showActionBarView();
    }

    public void onClickStation(Parser.WeatherStation station) {
        viewModel.parser.wUndergroundParams.useCustomStation = true;
        viewModel.parser.wUndergroundParams.customStationName = station.name;
    }

    private final class RefreshSubscriber extends DisposableObserver<NearbyStationsViewModel> {

        @Override
        public void onNext(NearbyStationsViewModel viewModel) {
            NearbyStationsPresenter.this.viewModel = viewModel;
            updateContent();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.nearby_stations_success_save_weather_station);
            Intent intent = new Intent();
            intent.putExtra(WeatherSourceDetailsActivity.EXTRA_STATION_NAME, viewModel.parser
                    .wUndergroundParams.customStationName);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Toasts.show(R.string.nearby_stations_error_save_weather_station);
            updateContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
