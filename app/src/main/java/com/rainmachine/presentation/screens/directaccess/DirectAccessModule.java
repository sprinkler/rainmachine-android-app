package com.rainmachine.presentation.screens.directaccess;

import com.rainmachine.data.boundary.DeviceRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.injection.AppModule;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = AppModule.class,
        complete = false,
        library = true,
        injects = {
                DirectAccessView.class,
                DirectAccessActivity.class,
                DirectAccessDialogFragment.class,
                ActionMessageParcelableDialogFragment.class
        }
)
class DirectAccessModule {

    private DirectAccessActivity activity;

    DirectAccessModule(DirectAccessActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    DirectAccessActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    ActionMessageParcelableDialogFragment.Callback provideActionMessageParcelableDialogCallback
            (DirectAccessPresenter directAccessPresenter) {
        return directAccessPresenter;
    }

    @Provides
    @Singleton
    DirectAccessPresenter providePresenter(DirectAccessActivity activity,
                                           DirectAccessMixer mixer) {
        return new DirectAccessPresenter(activity, mixer);
    }

    @Provides
    @Singleton
    DirectAccessMixer provideOtherDevicesMixer(DatabaseRepositoryImpl databaseRepository,
                                               PrefRepository prefRepository,
                                               DeviceRepositoryImpl devicesRepository) {
        return new DirectAccessMixer(databaseRepository, prefRepository, devicesRepository);
    }
}
