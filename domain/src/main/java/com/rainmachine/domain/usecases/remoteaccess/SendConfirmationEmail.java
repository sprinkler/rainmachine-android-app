package com.rainmachine.domain.usecases.remoteaccess;

import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import io.reactivex.Observable;

public class SendConfirmationEmail extends ObservableUseCase<SendConfirmationEmail.RequestModel,
        SendConfirmationEmail.ResponseModel> {

    private CloudRepository cloudRepository;
    private GetMacAddress getMacAddress;

    public SendConfirmationEmail(CloudRepository cloudRepository, GetMacAddress getMacAddress) {
        this.cloudRepository = cloudRepository;
        this.getMacAddress = getMacAddress;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(final RequestModel requestModel) {
        return getMacAddress
                .execute(new GetMacAddress.RequestModel(requestModel.deviceId, requestModel
                        .isDeviceManual))
                .map(responseModel -> responseModel.macAddress.toLowerCase(Locale.getDefault()))
                .flatMapCompletable(macAddress -> cloudRepository
                        .validateEmail(requestModel.email, requestModel.deviceName, macAddress))
                .andThen(Observable.just(new ResponseModel()));
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
    }
}
