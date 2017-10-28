package com.rainmachine.presentation.screens.advancedsettings;

import android.support.v4.app.DialogFragment;

import com.rainmachine.R;
import com.rainmachine.domain.model.HandPreference;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.dialogs.RadioOptionsDialogFragment;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class AdvancedSettingsPresenter extends BasePresenter<AdvancedSettingsView> implements
        RadioOptionsDialogFragment.Callback {

    private static final int DIALOG_ID_LOG_LEVEL = 1;
    private static final int DIALOG_ID_INTERFACE_OPTIONS = 2;

    private AdvancedSettingsActivity activity;
    private AdvancedSettingsMixer mixer;
    private Features features;

    private AdvancedSettingsViewModel viewModel;
    private CompositeDisposable disposables;

    AdvancedSettingsPresenter(AdvancedSettingsActivity activity, AdvancedSettingsMixer mixer,
                              Features features) {
        this.activity = activity;
        this.mixer = mixer;
        this.features = features;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(AdvancedSettingsView view) {
        super.attachView(view);
        view.setup(features.showBonjourService());
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
    public void onDialogRadioOptionsPositiveClick(int dialogId, String[] items, int
            checkedItemPosition) {
        if (dialogId == DIALOG_ID_LOG_LEVEL) {
            viewModel.logLevel = checkedItemPosition == 0 ? LogLevel.DEBUG :
                    (checkedItemPosition == 1 ? LogLevel.NORMAL : LogLevel.WARNING_ERRORS);
            view.showProgress();
            disposables.add(mixer.saveLogLevel(viewModel.logLevel)
                    .compose(RunOnProperThreads.instance())
                    .doOnError(GenericErrorDealer.INSTANCE)
                    .subscribeWith(new SaveSubscriber()));
            view.render(viewModel);
        } else if (dialogId == DIALOG_ID_INTERFACE_OPTIONS) {
            HandPreference handPreference = checkedItemPosition == 0 ? HandPreference.RIGHT_HAND :
                    HandPreference.LEFT_HAND;
            if (viewModel.handPreference != handPreference) {
                viewModel.handPreference = handPreference;
                view.showProgress();
                disposables.add(mixer.saveHandPreference(viewModel.handPreference)
                        .doOnError(GenericErrorDealer.INSTANCE)
                        .compose(RunOnProperThreads.instance())
                        .subscribeWith(new SaveSubscriber()));
                view.render(viewModel);
            }
        }
    }

    @Override
    public void onDialogRadioOptionsCancel(int dialogId) {
        // Do nothing
    }

    void onCheckedChangedAmazonAlexa(boolean isChecked) {
        viewModel.amazonAlexa = isChecked;
        view.showProgress();
        disposables.add(mixer.saveAmazonAlexa(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    void onCheckedChangedBetaUpdates(boolean isChecked) {
        viewModel.betaUpdates = isChecked;
        view.showProgress();
        disposables.add(mixer.saveBetaUpdates(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    void onCheckedChangedSshAccess(boolean isChecked) {
        viewModel.sshAccess = isChecked;
        view.showProgress();
        disposables.add(mixer.saveSshAccess(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    void onCheckedChangedBonjour(boolean isChecked) {
        viewModel.bonjourService = isChecked;
        view.showProgress();
        disposables.add(mixer.saveBonjourService(isChecked)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    void onClickInterfaceOptions() {
        String[] items = new String[]{
                activity.getString(R.string.advanced_settings_right_hand),
                activity.getString(R.string.advanced_settings_left_hand)};
        int checkedItemPosition = viewModel.handPreference == HandPreference.RIGHT_HAND ? 0 : 1;
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(DIALOG_ID_INTERFACE_OPTIONS,
                activity.getString(R.string.all_interface_options),
                activity.getString(R.string.all_save), items, checkedItemPosition);
        activity.showDialogSafely(dialog);
    }

    void onClickRetry() {
        refresh();
    }

    void onClickLogLevel() {
        String[] items = getLogLevelStrings();
        int checkedItemPosition = viewModel.logLevel == LogLevel.DEBUG ? 0 : (viewModel.logLevel ==
                LogLevel.NORMAL ? 1 : 2);
        DialogFragment dialog = RadioOptionsDialogFragment.newInstance(DIALOG_ID_LOG_LEVEL,
                activity.getString(R.string
                        .advanced_settings_log_level), activity.getString(R.string.all_save),
                items, checkedItemPosition);
        activity.showDialogSafely(dialog);
    }

    private String[] getLogLevelStrings() {
        return new String[]{
                activity.getString(R.string.advanced_settings_log_debug),
                activity.getString(R.string.advanced_settings_log_normal),
                activity.getString(R.string.advanced_settings_log_warnings)};
    }

    private void refresh() {
        view.showProgress();
        disposables.add(mixer.refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private final class RefreshSubscriber extends DisposableObserver<AdvancedSettingsViewModel> {

        @Override
        public void onNext(AdvancedSettingsViewModel viewModel) {
            AdvancedSettingsPresenter.this.viewModel = viewModel;
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
