package com.rainmachine.presentation.screens.weathersensitivity;

import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.rainsensitivity.RainSensitivityActivity;
import com.rainmachine.presentation.screens.windsensitivity.WindSensitivityActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WeatherSensitivityPresenter extends BasePresenter<WeatherSensitivityView> {

    private WeatherSensitivityActivity activity;
    private WeatherSensitivityMixer mixer;

    private WeatherSensitivityViewModel viewModel;
    private CompositeDisposable disposables;

    WeatherSensitivityPresenter(WeatherSensitivityActivity activity,
                                WeatherSensitivityMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WeatherSensitivityView view) {
        super.attachView(view);

        view.showProgress();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onClickRainSensitivity() {
        activity.startActivity(RainSensitivityActivity.getStartIntent(activity));
    }

    public void onClickWindSensitivity() {
        activity.startActivity(WindSensitivityActivity.getStartIntent(activity));
    }

    public void onCheckedChangedUseCorrection(boolean isChecked) {
        viewModel.useCorrection = isChecked;
        view.showProgress();
        disposables.add(mixer.saveUseCorrection(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<WeatherSensitivityViewModel> {

        @Override
        public void onNext(WeatherSensitivityViewModel viewModel) {
            WeatherSensitivityPresenter.this.viewModel = viewModel;
            view.render(viewModel);
            view.showContent();
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
            view.showContent();
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
