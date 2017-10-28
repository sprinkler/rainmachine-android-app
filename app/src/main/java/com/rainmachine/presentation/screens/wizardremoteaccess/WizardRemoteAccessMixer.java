package com.rainmachine.presentation.screens.wizardremoteaccess;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.usecases.remoteaccess.EnableRemoteAccessEmail;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.util.CustomDataException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

class WizardRemoteAccessMixer {

    private Device device;
    private DatabaseRepositoryImpl databaseRepository;
    private EnableRemoteAccessEmail enableRemoteAccessEmail;

    WizardRemoteAccessMixer(Device device, DatabaseRepositoryImpl databaseRepository,
                            EnableRemoteAccessEmail enableRemoteAccessEmail) {
        this.device = device;
        this.databaseRepository = databaseRepository;
        this.enableRemoteAccessEmail = enableRemoteAccessEmail;
    }

    Observable<WizardRemoteAccessViewModel> refresh() {
        return cloudInfoList()
                .map(cloudInfos -> buildViewModel(cloudInfos));
    }

    private Observable<List<CloudInfo>> cloudInfoList() {
        return Observable.fromCallable(() -> databaseRepository.getCloudInfoList());
    }

    private WizardRemoteAccessViewModel buildViewModel(List<CloudInfo> cloudInfoList) {
        WizardRemoteAccessViewModel viewModel = new WizardRemoteAccessViewModel();
        Set<String> knownEmails = new HashSet<>(cloudInfoList.size());
        for (CloudInfo cloudInfo : cloudInfoList) {
            knownEmails.add(cloudInfo.email);
        }
        viewModel.knownEmails = new ArrayList<>(cloudInfoList.size());
        viewModel.knownEmails.addAll(knownEmails);
        return viewModel;
    }

    Observable<Irrelevant> enableCloudEmail(final String email) {
        return enableRemoteAccessEmail
                .execute(new EnableRemoteAccessEmail.RequestModel(device.deviceId, device
                        .isManual(), device.name, email))
                .flatMap(responseModel -> {
                    if (responseModel.success) {
                        return Observable.just(Irrelevant.INSTANCE);
                    } else {
                        return Observable.error(new CustomDataException(CustomDataException
                                .CustomStatus.ENABLE_CLOUD_EMAIL_ERROR));
                    }
                })
                .compose(RunToCompletion.instance());
    }
}
