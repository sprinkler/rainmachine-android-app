package com.rainmachine.presentation.screens.rainsensitivity;

import com.rainmachine.R;
import com.rainmachine.domain.util.DomainUtils;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class RainSensitivityPresenter extends BasePresenter<RainSensitivityView> {

    private RainSensitivityActivity activity;
    private RainSensitivityMixer mixer;

    private final CompositeDisposable disposables;

    private RainSensitivityViewModel viewModel;

    RainSensitivityPresenter(RainSensitivityActivity activity, RainSensitivityMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(RainSensitivityView view) {
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

    public void onRetry() {
        refresh();
    }

    public void onProgressChanged(int progress) {
        viewModel.rainSensitivity = progress / 100.0f;
    }

    public void onClickSave() {
        view.showProgress();
        activity.hideActionBarView();
        disposables.add(mixer
                .saveRainSensitivity(viewModel.rainSensitivity)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickDefaults() {
        viewModel.rainSensitivity = DomainUtils.DEFAULT_RAIN_SENSITIVITY;
        view.updateSeekBar(viewModel.rainSensitivity);
    }

    private void refresh() {
        view.showProgress();
        activity.hideActionBarView();
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<RainSensitivityViewModel> {

        @Override
        public void onNext(RainSensitivityViewModel viewModel) {
            RainSensitivityPresenter.this.viewModel = viewModel;
            view.render(viewModel);
            view.showContent();
            activity.showActionBarView();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.hideActionBarView();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
            Toasts.show(R.string.rain_sensitivity_success_set);
            activity.finish();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
            activity.hideActionBarView();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
