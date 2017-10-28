package com.rainmachine.presentation.screens.mini8settings;

import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class Mini8SettingsPresenter extends BasePresenter<Mini8SettingsContract.View> implements
        Mini8SettingsContract.Presenter {

    private Mini8SettingsContract.Container container;
    private Mini8SettingsMixer mixer;

    private Mini8SettingsViewModel viewModel;
    private CompositeDisposable disposables;

    Mini8SettingsPresenter(Mini8SettingsContract.Container container, Mini8SettingsMixer mixer) {
        this.container = container;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(Mini8SettingsContract.View view) {
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

    @Override
    public void onDialogInputNumberPositiveClick(int dialogId, int value) {
        if (dialogId == Mini8SettingsContract.DIALOG_ID_MIN_LED_BRIGHTNESS) {
            viewModel.minLedBrightness = value;
            view.showProgress();
            disposables.add(mixer.saveMinLedBrightness(value)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        } else if (dialogId == Mini8SettingsContract.DIALOG_ID_MAX_LED_BRIGHTNESS) {
            viewModel.maxLedBrightness = value;
            view.showProgress();
            disposables.add(mixer.saveMaxLedBrightness(value)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        } else if (dialogId == Mini8SettingsContract.DIALOG_ID_TOUCH_SLEEP_TIMEOUT) {
            viewModel.touchSleepTimeout = value;
            view.showProgress();
            disposables.add(mixer.saveTouchSleepTimeout(value)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        } else if (dialogId == Mini8SettingsContract.DIALOG_ID_TOUCH_LONG_PRESS_TIMEOUT) {
            viewModel.touchLongPressTimeout = value;
            view.showProgress();
            disposables.add(mixer.saveTouchLongPressTimeout(value)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        }
    }

    @Override
    public void onDialogInputNumberCancel(int dialogId) {
        // Do nothing
    }

    @Override
    public void onClickTouchStartProgram() {
        container.showProgramsDialog(viewModel.programs, viewModel.touchProgramToRun);
    }

    @Override
    public void onClickManualWateringDuration() {
        container.goToManualWateringDurationScreen();
    }

    @Override
    public void onCheckedChangedTouchAdvanced(boolean isChecked) {
        viewModel.touchAdvanced = isChecked;
        view.showProgress();
        disposables.add(mixer.saveTouchAdvanced(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onCheckedChangedLedDelay(boolean isChecked) {
        viewModel.showRestrictionsOnLed = isChecked;
        view.showProgress();
        disposables.add(mixer.saveRestrictionsOnLed(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    @Override
    public void onClickMinLedBrightness() {
        container.showMinLedBrightnessDialog(viewModel.minLedBrightness);
    }

    @Override
    public void onClickMaxLedBrightness() {
        container.showMaxLedBrightnessDialog(viewModel.maxLedBrightness);
    }

    @Override
    public void onClickTouchSleepTimeout() {
        container.showTouchSleepTimeoutDialog(viewModel.touchSleepTimeout);
    }

    @Override
    public void onClickTouchLongPressTimeout() {
        container.showTouchLongPressTimeoutDialog(viewModel.touchLongPressTimeout);
    }

    @Override
    public void onSelectedProgram(TouchProgramViewModel selectedItem) {
        if (viewModel != null) {
            viewModel.touchProgramToRun = selectedItem;
            view.showProgress();
            view.render(viewModel);
            disposables.add(mixer.saveTouchProgramToRun(selectedItem.id)
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .compose(RunOnProperThreads.instance())
                    .subscribeWith(new SaveSubscriber()));
        }
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<Mini8SettingsViewModel> {

        @Override
        public void onNext(Mini8SettingsViewModel viewModel) {
            Mini8SettingsPresenter.this.viewModel = viewModel;
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
            view.showContent();
        }
    }
}
