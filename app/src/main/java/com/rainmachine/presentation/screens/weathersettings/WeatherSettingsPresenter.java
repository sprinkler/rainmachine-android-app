package com.rainmachine.presentation.screens.weathersettings;

import com.rainmachine.presentation.screens.weathersources.WeatherSource;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WeatherSettingsPresenter extends BasePresenter<WeatherSettingsContract.View> implements
        WeatherSettingsContract.Presenter {

    private WeatherSettingsContract.Container container;
    private WeatherSettingsMixer mixer;

    private WeatherSettingsViewModel viewModel;
    private CompositeDisposable disposables;

    WeatherSettingsPresenter(WeatherSettingsContract.Container container, WeatherSettingsMixer
            mixer) {
        this.container = container;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
    }

    @Override
    public void start() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onDialogActionMessagePositiveClick(int dialogId) {
        view.showProgress();
        container.hideDefaultsMenuItem();
        boolean isWeatherSensitivityChanged = viewModel.isRainSensitivityChanged || viewModel
                .isFieldCapacityChanged || viewModel.isWindSensitivityChanged;
        disposables.add(mixer.defaults(isWeatherSensitivityChanged)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void onDialogActionMessageNegativeClick(int dialogId) {
        // Do nothing
    }

    @Override
    public void onDialogActionMessageCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    @Override
    public void onClickWeatherServices() {
        container.goToWeatherSourcesScreen();
    }

    @Override
    public void onClickWeatherSensitivity() {
        container.goToWeatherSensitivityScreen();
    }

    @Override
    public void onClickWeatherSource(WeatherSource weatherSource) {
        container.goToWeatherSourceDetailsScreen(weatherSource.parser.uid);
    }

    @Override
    public void onClickDefaults() {
        container.showDefaultsDialog();
    }

    private void refresh() {
        view.showProgress();
        container.hideDefaultsMenuItem();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WeatherSettingsViewModel> {

        @Override
        public void onNext(WeatherSettingsViewModel viewModel) {
            WeatherSettingsPresenter.this.viewModel = viewModel;
            view.updateContent(viewModel);
            view.showContent();
            container.showDefaultsMenuItem();
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
}
