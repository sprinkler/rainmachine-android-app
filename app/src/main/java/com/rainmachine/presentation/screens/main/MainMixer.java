package com.rainmachine.presentation.screens.main;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.model.CloudSettings;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.usecases.remoteaccess.SendConfirmationEmail;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.SprinklerManager;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.DateTime;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class MainMixer {

    private Device device;
    private Features features;
    private UpdateHandler updateHandler;
    private TriggerUpdateCheck triggerUpdateCheck;
    private SprinklerRepositoryImpl sprinklerRepository;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;
    private SprinklerManager sprinklerManager;
    private SendConfirmationEmail sendConfirmationEmail;

    MainMixer(Device device, Features features, UpdateHandler updateHandler,
              TriggerUpdateCheck triggerUpdateCheck,
              SprinklerRepositoryImpl sprinklerRepository,
              SprinklerPrefRepositoryImpl sprinklerPrefsRepository,
              SprinklerManager sprinklerManager, SendConfirmationEmail sendConfirmationEmail) {
        this.device = device;
        this.features = features;
        this.updateHandler = updateHandler;
        this.triggerUpdateCheck = triggerUpdateCheck;
        this.sprinklerRepository = sprinklerRepository;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        this.sprinklerManager = sprinklerManager;
        this.sendConfirmationEmail = sendConfirmationEmail;
    }

    public Observable<MainViewModel> refresh(final boolean checkUpdate, final boolean
            checkCloudEmailPending) {
        if (checkUpdate && checkCloudEmailPending) {
            return Observable.combineLatest(
                    updateStream(),
                    cloudSettingsStream(),
                    (update, cloudSettings) -> buildViewModel(update, cloudSettings));
        } else if (checkUpdate) {
            return updateStream()
                    .map(update -> buildViewModel(update, null));
        } else if (checkCloudEmailPending) {
            return cloudSettingsStream()
                    .map(cloudSettings -> buildViewModel(null, cloudSettings));
        }
        throw new IllegalArgumentException("Both flags should never be false");
    }

    private Observable<Update> updateStream() {
        Single<Update> stream;
        if (features.useNewApi()) {
            stream = triggerUpdateCheck
                    .execute(new TriggerUpdateCheck.RequestModel())
                    .andThen(sprinklerRepository.update(true));
        } else {
            stream = sprinklerRepository.update3(true);
        }
        return stream
                .doOnSuccess(update ->
                        sprinklerPrefsRepository.saveLastUpdate(new DateTime().getMillis()))
                .toObservable();
    }

    private Observable<CloudSettings> cloudSettingsStream() {
        return sprinklerRepository.cloudSettings()
                .doOnSuccess(cloudSettings ->
                        sprinklerPrefsRepository.saveLastCloudEmailPending(new DateTime()
                                .getMillis()))
                .toObservable();
    }

    private MainViewModel buildViewModel(Update update, CloudSettings cloudSettings) {
        MainViewModel viewModel = new MainViewModel();
        if (update != null) {
            viewModel.showUpdateDialog = update.update;
            viewModel.newUpdateVersion = update.newVersion;
        }
        if (cloudSettings != null) {
            viewModel.showCloudEmailPendingDialog = cloudSettings.isEmailPending();
            viewModel.cloudPendingEmail = cloudSettings.pendingEmail;
        }
        viewModel.showCloudSetupDialog = !sprinklerPrefsRepository.shownCloudSetupDialog() &&
                features.showCloudSetupDialog();
        return viewModel;
    }

    void makeUpdate() {
        updateHandler
                .triggerUpdate()
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    void resendConfirmationEmail(final String pendingEmail) {
        sendConfirmationEmail
                .execute(new SendConfirmationEmail.RequestModel(device.deviceId, device.isManual
                        (), device.name, pendingEmail))
                .map(responseModel -> true)
                .onErrorReturn(throwable -> false)
                .doOnSubscribe(disposable -> Toasts.show(R.string.main_sending_confirmation_email))
                .doOnNext(success -> {
                    if (success) {
                        Toasts.show(R.string.all_success_send_confirmation_email);
                    } else {
                        Toasts.show(R.string.all_failure_send_confirmation_email);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    void syncZoneImages() {
        sprinklerManager.syncZoneImages();
    }
}
