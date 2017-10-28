package com.rainmachine.presentation.screens.wizardpassword;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.presentation.util.Toasts;

import java.util.List;

import io.reactivex.Observable;

class WizardPasswordMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private DatabaseRepositoryImpl databaseRepository;
    private SprinklerState sprinklerState;

    WizardPasswordMixer(SprinklerRepositoryImpl sprinklerRepository,
                        DatabaseRepositoryImpl databaseRepository,
                        SprinklerState sprinklerState) {
        this.sprinklerState = sprinklerState;
        this.sprinklerRepository = sprinklerRepository;
        this.databaseRepository = databaseRepository;
    }

    Observable<WizardPasswordViewModel> refresh() {
        return Observable.fromCallable(() -> databaseRepository.getCloudInfoList())
                .filter(cloudInfoList -> cloudInfoList.size() > 0)
                .map(cloudInfoList -> buildViewModel(cloudInfoList));
    }

    private WizardPasswordViewModel buildViewModel(List<CloudInfo> cloudInfoList) {
        WizardPasswordViewModel viewModel = new WizardPasswordViewModel();
        viewModel.preFillPassword = cloudInfoList.get(0).password;
        return viewModel;
    }

    Observable<Irrelevant> savePassword(final String newPass) {
        return sprinklerRepository
                .changePassword("", newPass).toObservable()
                .doOnNext(irrelevant -> {
                    Toasts.show(R.string.wizard_password_success_set_password);
                    sprinklerState.keepPasswordForLater(newPass);
                })
                .compose(RunToCompletion.instance());
    }
}
