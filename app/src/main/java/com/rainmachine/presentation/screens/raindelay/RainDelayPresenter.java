package com.rainmachine.presentation.screens.raindelay;

import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class RainDelayPresenter extends BasePresenter<RainDelayContract.View> implements
        RainDelayContract.Presenter {

    private RainDelayContract.Container container;
    private RainDelayMixer mixer;

    private RainDelayViewModel viewModel;
    private CompositeDisposable disposables;

    RainDelayPresenter(RainDelayContract.Container container, RainDelayMixer mixer) {
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
    public void onClickDelay(int timerValue) {
        int rainDelayValue = 0;
        if (viewModel.counterRemaining <= 0) {
            rainDelayValue = timerValue;
        }

        view.showProgress();
        disposables.add(mixer.saveRainDelay(rainDelayValue)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onClickSnooze(int numSeconds) {
        if (numSeconds > 0) {
            view.showProgress();
            disposables.add(mixer.saveSnooze(numSeconds)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
        } else {
            view.showInvalidDurationMessage();
        }
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<RainDelayViewModel> {

        @Override
        public void onNext(RainDelayViewModel viewModel) {
            RainDelayPresenter.this.viewModel = viewModel;
            view.render(viewModel);
            if (viewModel.counterRemaining <= 0 && viewModel.showGranularContent) {
                view.showContent();
            } else {
                view.showContentOld();
            }
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
            container.closeScreen();
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
