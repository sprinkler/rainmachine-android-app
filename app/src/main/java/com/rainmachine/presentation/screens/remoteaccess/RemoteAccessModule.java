package com.rainmachine.presentation.screens.remoteaccess;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.usecases.remoteaccess.CreateRemoteAccessAccount;
import com.rainmachine.domain.usecases.remoteaccess.EnableRemoteAccessEmail;
import com.rainmachine.domain.usecases.remoteaccess.SendConfirmationEmail;
import com.rainmachine.domain.usecases.remoteaccess.ToggleRemoteAccess;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
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
                RemoteAccessView.class,
                RemoteAccessActivity.class,
                CloudEmailDialogFragment.class,
                InfoMessageDialogFragment.class,
                ActionMessageDialogFragment.class,
                ChangePasswordDialogFragment.class
        }
)
class RemoteAccessModule {

    private RemoteAccessActivity activity;

    RemoteAccessModule(RemoteAccessActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    RemoteAccessActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(RemoteAccessPresenter
                                                                                remoteAccessPresenter) {
        return remoteAccessPresenter;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(RemoteAccessPresenter
                                                                                    remoteAccessPresenter) {
        return remoteAccessPresenter;
    }

    @Provides
    @Singleton
    RemoteAccessPresenter providePresenter(RemoteAccessActivity activity,
                                           RemoteAccessMixer mixer, Features features,
                                           SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        return new RemoteAccessPresenter(activity, mixer, features, sprinklerPrefsRepository);
    }

    @Provides
    @Singleton
    RemoteAccessMixer provideRemoteAccessMixer(Device device,
                                               DatabaseRepositoryImpl databaseRepository,
                                               SprinklerRepositoryImpl sprinklerRepository,
                                               EnableRemoteAccessEmail enableRemoteAccessEmail,
                                               CreateRemoteAccessAccount createRemoteAccessAccount,
                                               Features features,
                                               SendConfirmationEmail sendConfirmationEmail,
                                               ToggleRemoteAccess toggleRemoteAccess,
                                               SprinklerState sprinklerState) {
        return new RemoteAccessMixer(device, databaseRepository, sprinklerRepository,
                enableRemoteAccessEmail, createRemoteAccessAccount, features, sendConfirmationEmail,
                toggleRemoteAccess, sprinklerState);
    }

}
