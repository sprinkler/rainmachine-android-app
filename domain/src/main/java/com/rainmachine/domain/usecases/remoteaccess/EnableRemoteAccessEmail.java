package com.rainmachine.domain.usecases.remoteaccess;

import com.rainmachine.domain.boundary.data.RemoteAccessAccountRepository;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class EnableRemoteAccessEmail extends ObservableUseCase<EnableRemoteAccessEmail.RequestModel,
        EnableRemoteAccessEmail.ResponseModel> {

    private final SprinklerRepository sprinklerRepository;
    private final ToggleRemoteAccess toggleRemoteAccess;
    private final SendConfirmationEmail sendConfirmationEmail;
    private final SprinklerState sprinklerState;
    private final RemoteAccessAccountRepository remoteAccessAccountRepository;

    public EnableRemoteAccessEmail(SprinklerRepository sprinklerRepository,
                                   ToggleRemoteAccess toggleRemoteAccess,
                                   SendConfirmationEmail sendConfirmationEmail,
                                   SprinklerState sprinklerState,
                                   RemoteAccessAccountRepository remoteAccessAccountRepository) {
        this.sprinklerRepository = sprinklerRepository;
        this.toggleRemoteAccess = toggleRemoteAccess;
        this.sendConfirmationEmail = sendConfirmationEmail;
        this.sprinklerState = sprinklerState;
        this.remoteAccessAccountRepository = remoteAccessAccountRepository;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return toggleRemoteAccess
                .execute(new ToggleRemoteAccess.RequestModel(true))
                // We sleep here because the API does not work if we call both APIs immediately
                // one after another
                .delay(2, TimeUnit.SECONDS)
                .andThen(sprinklerRepository.saveCloudEmail(requestModel.email))
                .andThen(sendConfirmationEmail
                        .execute(new SendConfirmationEmail.RequestModel(requestModel.deviceId,
                                requestModel.isDeviceManual, requestModel.deviceName,
                                requestModel.email)))
                .doOnNext(responseModel -> {
                    String password = sprinklerState.lastValidPasswordUsed();
                    if (!Strings.isBlank(password)) {
                        remoteAccessAccountRepository.saveAccount(requestModel.email, password);
                    }
                })
                .map(responseModel -> new ResponseModel(true))
                .onErrorReturn(throwable -> new ResponseModel(false));
    }

    public static class RequestModel {
        String deviceId;
        boolean isDeviceManual;
        String deviceName;
        String email;

        public RequestModel(String deviceId, boolean isDeviceManual, String deviceName, String
                email) {
            this.deviceId = deviceId;
            this.isDeviceManual = isDeviceManual;
            this.deviceName = deviceName;
            this.email = email;
        }
    }

    public static class ResponseModel {
        public boolean success;

        public ResponseModel(boolean success) {
            this.success = success;
        }
    }
}
