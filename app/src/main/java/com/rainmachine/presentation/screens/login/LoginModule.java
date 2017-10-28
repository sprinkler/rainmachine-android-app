package com.rainmachine.presentation.screens.login;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.usecases.remoteaccess.CreateRemoteAccessAccount;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.injection.SprinklerModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                LoginActivity.class,
                LoginView.class
        }
)
class LoginModule {

    private LoginActivity activity;

    LoginModule(LoginActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    LoginActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    LoginPresenter providePresenter(Device device, LoginActivity activity, LoginMixer mixer,
                                    SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        return new LoginPresenter(device, activity, mixer, sprinklerPrefsRepository);
    }

    @Provides
    @Singleton
    LoginMixer provideLoginMixer(CreateRemoteAccessAccount createRemoteAccessAccount,
                                 Features features,
                                 SprinklerRepositoryImpl sprinklerRepository,
                                 SprinklerPrefRepositoryImpl sprinklerPrefsRepository,
                                 SprinklerState sprinklerState) {
        return new LoginMixer(createRemoteAccessAccount, features, sprinklerRepository,
                sprinklerPrefsRepository, sprinklerState);
    }
}
