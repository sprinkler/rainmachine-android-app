package com.rainmachine.presentation.screens.cloudaccounts;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.remote.cloud.CloudSprinklersApiDelegate;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.injection.AppModule;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.drawer.DrawerPresenter;
import com.rainmachine.presentation.screens.drawer.DrawerView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                CloudAccountsView.class,
                DrawerView.class,
                CloudAccountsActivity.class,
                CloudAccountsDialogFragment.class,
                ActionMessageParcelableDialogFragment.class,
                InfoMessageDialogFragment.class
        }
)
class CloudAccountsModule {

    private CloudAccountsActivity activity;

    CloudAccountsModule(CloudAccountsActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    CloudAccountsActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    ActionMessageParcelableDialogFragment.Callback provideActionMessageParcelableDialogCallback
            (CloudAccountsPresenter cloudAccountsPresenter) {
        return cloudAccountsPresenter;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(CloudAccountsPresenter
                                                                                cloudAccountsPresenter) {
        return cloudAccountsPresenter;
    }

    @Provides
    @Singleton
    CloudAccountsPresenter providePresenter(CloudAccountsActivity activity,
                                            CloudAccountsMixer mixer) {
        return new CloudAccountsPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    DrawerPresenter providePresenter(CloudAccountsActivity activity) {
        return new DrawerPresenter(activity, 1);
    }

    @Provides
    @Singleton
    CloudAccountsMixer provideCloudAccountsMixer(DatabaseRepositoryImpl databaseRepository,
                                                 CloudSprinklersApiDelegate
                                                         cloudSprinklersApiDelegate,
                                                 DeviceRepository deviceRepository) {
        return new CloudAccountsMixer(databaseRepository, cloudSprinklersApiDelegate,
                deviceRepository);
    }
}
