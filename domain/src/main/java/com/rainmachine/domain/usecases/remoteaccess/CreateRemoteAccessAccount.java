package com.rainmachine.domain.usecases.remoteaccess;

import com.rainmachine.domain.boundary.data.RemoteAccessAccountRepository;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

public class CreateRemoteAccessAccount extends ObservableUseCase<CreateRemoteAccessAccount
        .RequestModel,
        CreateRemoteAccessAccount.ResponseModel> {

    private SprinklerRepository sprinklerRepository;
    private RemoteAccessAccountRepository remoteAccessAccountRepository;

    public CreateRemoteAccessAccount(SprinklerRepository sprinklerRepository,
                                     RemoteAccessAccountRepository remoteAccessAccountRepository) {
        this.sprinklerRepository = sprinklerRepository;
        this.remoteAccessAccountRepository = remoteAccessAccountRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return sprinklerRepository
                .cloudSettings().toObservable()
                .map(cloudSettings -> {
                    String email = null;
                    if (!Strings.isBlank(cloudSettings.pendingEmail)) {
                        email = cloudSettings.pendingEmail;
                    } else if (!Strings.isBlank(cloudSettings.email)) {
                        email = cloudSettings.email;
                    }
                    return email;
                })
                .switchMap(email -> {
                    remoteAccessAccountRepository.saveAccount(email, requestModel.password);
                    return Observable.just(new ResponseModel());
                });
    }

    public static class RequestModel {
        String password;

        public RequestModel(String password) {
            this.password = password;
        }
    }

    public static class ResponseModel {
    }
}
