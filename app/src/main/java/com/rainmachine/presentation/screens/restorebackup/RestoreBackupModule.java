package com.rainmachine.presentation.screens.restorebackup;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.usecases.backup.GetBackups;
import com.rainmachine.domain.usecases.backup.RestoreBackup;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                RestoreBackupActivity.class,
                RestoreBackupView.class,
                RestoreBackupDialogFragment.class,
                ActionMessageParcelableDialogFragment.class,
                InfoMessageDialogFragment.class
        }
)
class RestoreBackupModule {

    private RestoreBackupActivity activity;

    RestoreBackupModule(RestoreBackupActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    RestoreBackupActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    RestoreBackupDialogFragment.Callback provideRestoreBackupDialogCallback(RestoreBackupPresenter
                                                                                    presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    ActionMessageParcelableDialogFragment.Callback provideActionMessageParcelableDialogCallback
            (RestoreBackupPresenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(RestoreBackupPresenter
                                                                                presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    RestoreBackupPresenter providePresenter(RestoreBackupActivity activity, RestoreBackupMixer
            mixer) {
        return new RestoreBackupPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    RestoreBackupMixer provideRestoreBackupMixer(SprinklerRepositoryImpl sprinklerRepository,
                                                 RestoreBackup restoreBackup,
                                                 GetBackups getBackups, Device device) {
        return new RestoreBackupMixer(sprinklerRepository, restoreBackup, getBackups, device);
    }
}
