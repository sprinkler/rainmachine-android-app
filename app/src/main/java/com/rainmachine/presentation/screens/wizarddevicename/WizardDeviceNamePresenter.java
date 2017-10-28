package com.rainmachine.presentation.screens.wizarddevicename;

import com.rainmachine.R;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.presentation.screens.location.LocationActivity;
import com.rainmachine.presentation.screens.wizardtimezone.WizardTimezoneActivity;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.ExtraConstants;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;
import com.rainmachine.presentation.util.Toasts;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class WizardDeviceNamePresenter extends BasePresenter<WizardDeviceNameView> {

    private WizardDeviceNameActivity activity;
    private WizardDeviceNameMixer mixer;
    private Device device;

    private final CompositeDisposable disposables;

    private boolean isWizard;
    private boolean showOldPass;

    WizardDeviceNamePresenter(WizardDeviceNameActivity activity, WizardDeviceNameMixer mixer,
                              Device device) {
        this.activity = activity;
        this.mixer = mixer;
        this.device = device;
        isWizard = activity.getIntent().getBooleanExtra(ExtraConstants.IS_WIZARD, false);
        showOldPass = activity.getIntent().getBooleanExtra(ExtraConstants.SHOW_OLD_PASS_INPUT,
                false);
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(WizardDeviceNameView view) {
        super.attachView(view);

        view.updateContent(null, showOldPass);
    }

    @Override
    public void init() {
        disposables.add(mixer
                .refresh()
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    public void onClickSave(String deviceName, String oldPass, String newPass) {
        view.showProgress();
        disposables.add(mixer
                .saveDeviceAndPassword(deviceName, oldPass, newPass)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SaveSubscriber()));
    }

    public boolean showOldPass() {
        return showOldPass;
    }

    private final class RefreshSubscriber extends DisposableObserver<WizardDeviceNameViewModel> {

        @Override
        public void onNext(WizardDeviceNameViewModel viewModel) {
            view.updateContent(viewModel.preFillPassword, showOldPass);
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
            Toasts.show(R.string.wizard_device_name_success_set_name_password);
            if (isWizard) {
                if (device.isAp()) {
                    activity.startActivity(WizardTimezoneActivity.getStartIntent(activity));
                } else {
                    activity.startActivity(LocationActivity.getStartIntent(activity, true, null));
                }
            }
            activity.finish();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showContent();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
