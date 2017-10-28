package com.rainmachine.presentation.screens.wizardremoteaccess;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.usecases.remoteaccess.EnableRemoteAccessEmail;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                WizardRemoteAccessView.class,
                WizardRemoteAccessActivity.class,
                InfoMessageDialogFragment.class,
                ActionMessageDialogFragment.class
        }
)
class WizardRemoteAccessModule {

    private WizardRemoteAccessActivity activity;

    WizardRemoteAccessModule(WizardRemoteAccessActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    WizardRemoteAccessActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(WizardRemoteAccessPresenter
                                                                                wizardRemoteAccessPresenter) {
        return wizardRemoteAccessPresenter;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (WizardRemoteAccessPresenter

                     wizardRemoteAccessPresenter) {
        return wizardRemoteAccessPresenter;
    }

    @Provides
    @Singleton
    WizardRemoteAccessPresenter providePresenter(WizardRemoteAccessActivity activity,
                                                 WizardRemoteAccessMixer mixer,
                                                 SprinklerPrefRepositoryImpl
                                                         sprinklerPrefsRepository) {
        return new WizardRemoteAccessPresenter(activity, mixer, sprinklerPrefsRepository);
    }

    @Provides
    @Singleton
    WizardRemoteAccessMixer provideWizardRemoteAccessMixer(Device device,
                                                           DatabaseRepositoryImpl
                                                                   databaseRepository,
                                                           EnableRemoteAccessEmail
                                                                   enableRemoteAccessEmail) {
        return new WizardRemoteAccessMixer(device, databaseRepository, enableRemoteAccessEmail);
    }
}
