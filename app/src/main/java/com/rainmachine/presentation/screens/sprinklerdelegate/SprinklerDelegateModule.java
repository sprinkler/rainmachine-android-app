package com.rainmachine.presentation.screens.sprinklerdelegate;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.usecases.auth.CheckAuthenticationValid;
import com.rainmachine.domain.usecases.auth.LogInDefault;
import com.rainmachine.domain.usecases.remoteaccess.ToggleRemoteAccess;
import com.rainmachine.domain.util.Features;
import com.rainmachine.infrastructure.SprinklerUtils;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                SprinklerDelegateActivity.class,
                ActionMessageDialogFragment.class
        }
)
class SprinklerDelegateModule {

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback
            (SprinklerDelegateContract.Presenter presenter) {
        return presenter;
    }

    @Provides
    @Singleton
    SprinklerDelegateContract.Presenter providePresenter(Bus bus, SprinklerDelegateMixer mixer) {
        return new SprinklerDelegatePresenter(bus, mixer);
    }

    @Provides
    @Singleton
    SprinklerDelegateMixer provideSprinklerDelegateMixer(Bus bus, Device device,
                                                         SprinklerUtils sprinklerUtils,
                                                         Features features,
                                                         DatabaseRepositoryImpl databaseRepository,
                                                         SprinklerRepositoryImpl
                                                                 sprinklerRepository,
                                                         ToggleRemoteAccess toggleRemoteAccess,
                                                         CrashReporter crashReporter,
                                                         LogInDefault logInDefault,
                                                         CheckAuthenticationValid
                                                                 checkAuthenticationValid) {
        return new SprinklerDelegateMixer(bus, device, sprinklerUtils, features,
                databaseRepository, sprinklerRepository, toggleRemoteAccess,
                crashReporter, logInDefault, checkAuthenticationValid);
    }
}
