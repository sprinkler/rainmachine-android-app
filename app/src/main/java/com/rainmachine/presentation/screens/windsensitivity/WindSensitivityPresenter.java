package com.rainmachine.presentation.screens.windsensitivity;

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

class WindSensitivityPresenter extends BasePresenter<WindSensitivityView> {

    private WindSensitivityActivity activity;
    private WindSensitivityMixer mixer;

    private final CompositeDisposable disposables;

    private WindSensitivityViewModel viewModel;

    WindSensitivityPresenter(WindSensitivityActivity activity,
                             WindSensitivityMixer mixer) {
        this.activity = activity;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WindSensitivityView view) {
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
        viewModel.windSensitivity = progress / 100.0f;
    }

    public void onClickedSave() {
        view.showProgress();
        activity.hideActionBarView();
        disposables.add(mixer
                .saveWindSensitivity(viewModel.windSensitivity)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public void onClickedDefaults() {
        viewModel.windSensitivity = DomainUtils.DEFAULT_WIND_SENSITIVITY;
        view.updateSeekBar(viewModel.windSensitivity);
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

    public WindSensitivityViewModel getViewModel() {
        return viewModel;
    }

    private final class RefreshSubscriber extends DisposableObserver<WindSensitivityViewModel> {

        @Override
        public void onNext(WindSensitivityViewModel viewModel) {
            WindSensitivityPresenter.this.viewModel = viewModel;
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
            Toasts.show(R.string.wind_sensitivity_success_set);
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
