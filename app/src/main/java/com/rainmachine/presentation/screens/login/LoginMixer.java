package com.rainmachine.presentation.screens.login;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.usecases.remoteaccess.CreateRemoteAccessAccount;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SprinklerState;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

class LoginMixer {

    private CreateRemoteAccessAccount createRemoteAccessAccount;
    private Features features;
    private SprinklerRepositoryImpl sprinklerRepository;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;
    private SprinklerState sprinklerState;

    LoginMixer(CreateRemoteAccessAccount createRemoteAccessAccount, Features features,
               SprinklerRepositoryImpl sprinklerRepository,
               SprinklerPrefRepositoryImpl sprinklerPrefsRepository,
               SprinklerState sprinklerState) {
        this.createRemoteAccessAccount = createRemoteAccessAccount;
        this.features = features;
        this.sprinklerRepository = sprinklerRepository;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
        this.sprinklerState = sprinklerState;
    }

    public Observable<LoginStatus> login(final String username, final String pass, final boolean
            isRemember) {
        Observable<LoginStatus> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository
                    .login(pass, isRemember)
                    .toObservable()
                    .doOnNext(status -> {
                        if (isRemember && status == LoginStatus.SUCCESS) {
                            sprinklerState.keepPasswordForLater(pass);
                            createRemoteAccessAccount
                                    .execute(new CreateRemoteAccessAccount.RequestModel(pass))
                                    .onErrorResumeNext(Observable.empty())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe();
                        }
                    });
        } else {
            observable = sprinklerRepository
                    .login3(username, pass, isRemember).toObservable()
                    .flatMap(success -> {
                        if (success) {
                            sprinklerPrefsRepository.saveUsername(username);
                            return Observable.just(LoginStatus.SUCCESS);
                        } else {
                            return Observable.just(LoginStatus.ERROR_NETWORK);
                        }
                    });
        }
        return observable
                .onErrorResumeNext(Observable.just(LoginStatus.ERROR_NETWORK));
    }
}
