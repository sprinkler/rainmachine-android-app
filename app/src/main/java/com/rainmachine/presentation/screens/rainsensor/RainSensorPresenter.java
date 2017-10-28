package com.rainmachine.presentation.screens.rainsensor;

import com.rainmachine.domain.usecases.restriction.GetRestrictionsDetails;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class RainSensorPresenter implements RainSensorContract.Presenter {

    private RainSensorContract.View view;
    private RainSensorContract.Container container;
    private RainSensorMixer mixer;
    private GetRestrictionsDetails getRestrictionsDetails;
    private Features features;

    private CompositeDisposable disposables;
    private RainSensorViewModel viewModel;

    RainSensorPresenter(RainSensorContract.Container container, RainSensorMixer mixer,
                        GetRestrictionsDetails getRestrictionsDetails, Features features) {
        this.container = container;
        this.mixer = mixer;
        this.getRestrictionsDetails = getRestrictionsDetails;
        this.features = features;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(RainSensorContract.View view) {
        this.view = view;
        view.setup(features.showExtraRainSensorFields());
        detectRain();
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
    public void onRetry() {
        refresh();
    }

    @Override
    public void onClickRainSensor() {
        saveRainSensor(!viewModel.useRainSensor);
    }

    @Override
    public void onCheckedChangedRainSensorHardware(boolean enabled) {
        saveRainSensor(enabled);
    }

    @Override
    public void onCheckedChangedRainSensorClosed(boolean isChecked) {
        viewModel.rainSensorNormallyClosed = isChecked;
        view.showProgress();
        disposables.add(mixer.saveRainSensorNormallyClosed(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onClickRainDetectedOption() {
        container.showRainDetectedOptions(optionsText(), viewModel.rainDetectedOption.position);
    }

    private String[] optionsText() {
        String[] s = new String[viewModel.options.size()];
        for (int i = 0; i < viewModel.options.size(); i++) {
            ItemRainOption option = viewModel.options.get(i);
            s[i] = option.toString();
        }
        return s;
    }

    @Override
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        ItemRainOption option = viewModel.options.get(checkedItemPosition);
        viewModel.rainDetectedOption = option;
        view.showProgress();
        disposables.add(mixer.saveRainSensorSnoozeDuration(option.snoozeDuration)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    private void saveRainSensor(boolean enabled) {
        viewModel.useRainSensor = enabled;
        view.showProgress();
        disposables.add(mixer.saveRainSensor(enabled)
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

    private void detectRain() {
        Observable<RainDetectedViewModel> poll = Observable
                .interval(0, 1, TimeUnit.SECONDS)
                .flatMap(interval -> getRestrictionsDetails.execute(new GetRestrictionsDetails
                        .RequestModel(false)))
                .map(responseModel -> new RainDetectedViewModel(responseModel.rainSensor));
        disposables.add(poll
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RainDetectedSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<RainSensorViewModel> {

        @Override
        public void onNext(RainSensorViewModel viewModel) {
            RainSensorPresenter.this.viewModel = viewModel;
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

    private final class RainDetectedSubscriber extends DisposableObserver<RainDetectedViewModel> {

        @Override
        public void onNext(RainDetectedViewModel viewModel) {
            view.render(viewModel);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }

    private final class SaveSubscriber extends DisposableObserver<Irrelevant> {

        @Override
        public void onNext(Irrelevant irrelevant) {
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
}
