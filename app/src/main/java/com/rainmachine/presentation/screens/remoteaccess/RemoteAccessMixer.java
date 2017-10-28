package com.rainmachine.presentation.screens.remoteaccess;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.usecases.remoteaccess.CreateRemoteAccessAccount;
import com.rainmachine.domain.usecases.remoteaccess.EnableRemoteAccessEmail;
import com.rainmachine.domain.usecases.remoteaccess.SendConfirmationEmail;
import com.rainmachine.domain.usecases.remoteaccess.ToggleRemoteAccess;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.presentation.util.CustomDataException;
import com.rainmachine.presentation.util.Toasts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class RemoteAccessMixer {

    private Device device;
    private DatabaseRepositoryImpl databaseRepository;
    private SprinklerRepositoryImpl sprinklerRepository;
    private EnableRemoteAccessEmail enableRemoteAccessEmail;
    private CreateRemoteAccessAccount createRemoteAccessAccount;
    private Features features;
    private SendConfirmationEmail sendConfirmationEmail;
    private ToggleRemoteAccess toggleRemoteAccess;
    private SprinklerState sprinklerState;

    RemoteAccessMixer(Device device, DatabaseRepositoryImpl databaseRepository,
                      SprinklerRepositoryImpl sprinklerRepository,
                      EnableRemoteAccessEmail enableRemoteAccessEmail,
                      CreateRemoteAccessAccount createRemoteAccessAccount, Features features,
                      SendConfirmationEmail sendConfirmationEmail,
                      ToggleRemoteAccess toggleRemoteAccess,
                      SprinklerState sprinklerState) {
        this.device = device;
        this.databaseRepository = databaseRepository;
        this.sprinklerRepository = sprinklerRepository;
        this.enableRemoteAccessEmail = enableRemoteAccessEmail;
        this.createRemoteAccessAccount = createRemoteAccessAccount;
        this.features = features;
        this.sendConfirmationEmail = sendConfirmationEmail;
        this.toggleRemoteAccess = toggleRemoteAccess;
        this.sprinklerState = sprinklerState;
    }

    Observable<RemoteAccessViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.cloudSettings().toObservable(),
                cloudInfoList(),
                (cloudSettings, cloudInfoList) -> {
                    RemoteAccessViewModel viewModel = new RemoteAccessViewModel();
                    Set<String> knownEmails = new HashSet<>(cloudInfoList.size());
                    for (CloudInfo cloudInfo : cloudInfoList) {
                        knownEmails.add(cloudInfo.email);
                    }
                    viewModel.knownEmails = new ArrayList<>(cloudInfoList.size());
                    viewModel.knownEmails.addAll(knownEmails);
                    viewModel.currentEmail = cloudSettings.email;
                    viewModel.currentPendingEmail = cloudSettings.pendingEmail;
                    viewModel.cloudEnabled = cloudSettings.enabled;
                    viewModel.isCloudDevice = device.isCloud();
                    return viewModel;
                });
    }

    private Observable<List<CloudInfo>> cloudInfoList() {
        return Observable.fromCallable(() -> databaseRepository.getCloudInfoList());
    }

    Observable<Irrelevant> enableCloudEmail(final String email) {
        return enableRemoteAccessEmail
                .execute(new EnableRemoteAccessEmail.RequestModel(device.deviceId, device
                        .isManual(), device.name, email))
                .flatMap(responseModel -> {
                    if (responseModel.success) {
                        return Observable.just(Irrelevant.INSTANCE);
                    } else {
                        return Observable.error(new CustomDataException
                                (CustomDataException.CustomStatus.ENABLE_CLOUD_EMAIL_ERROR));
                    }
                })
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> disableCloudEmail() {
        return toggleRemoteAccess
                .execute(new ToggleRemoteAccess.RequestModel(false))
                .andThen(Single.just(true))
                .onErrorReturnItem(false)
                .flatMapObservable(success -> {
                    if (success) {
                        return Observable.just(Irrelevant.INSTANCE);
                    } else {
                        return Observable.error(new CustomDataException
                                (CustomDataException.CustomStatus.DISABLE_CLOUD_EMAIL_ERROR));
                    }
                })
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> sendConfirmationEmail(final String email) {
        return sendConfirmationEmail
                .execute(new SendConfirmationEmail.RequestModel(device.deviceId, device
                        .isManual(), device.name, email))
                .map(responseModel -> true)
                .onErrorReturn(throwable -> false)
                .flatMap(success -> {
                    if (success) {
                        return Observable.just(Irrelevant.INSTANCE);
                    } else {
                        return Observable.error(new CustomDataException
                                (CustomDataException.CustomStatus.EMAIL_CONFIRMATION_ERROR));
                    }
                })
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> changePassword(final String oldPass, final String newPass) {
        Observable<Irrelevant> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository.changePassword(oldPass, newPass).toObservable();
        } else {
            observable = sprinklerRepository.changePassword3(oldPass, newPass).toObservable();
        }
        return observable
                .doOnNext(irrelevant -> {
                    Toasts.show(R.string.remote_access_success_change_password);
                    sprinklerState.keepPasswordForLater(newPass);
                    createRemoteAccessAccount
                            .execute(new CreateRemoteAccessAccount.RequestModel(newPass))
                            .onErrorResumeNext(Observable.empty())
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                })
                .compose(RunToCompletion.instance());
    }
}
