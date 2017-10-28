package com.rainmachine.data.remote.cloud;

import com.rainmachine.BuildConfig;
import com.rainmachine.data.remote.cloud.request.ValidateEmailRequest;
import com.rainmachine.data.remote.util.RemoteErrorTransformer;
import com.rainmachine.data.remote.util.RemoteRetry;

import io.reactivex.Completable;
import io.reactivex.SingleTransformer;

public class CloudValidateApiDelegate {

    private CloudValidateApi cloudValidateApi;
    private RemoteRetry remoteRetry;
    private RemoteErrorTransformer remoteErrorTransformer;

    public CloudValidateApiDelegate(CloudValidateApi cloudValidateApi) {
        this.cloudValidateApi = cloudValidateApi;
        remoteRetry = new RemoteRetry();
        remoteErrorTransformer = new RemoteErrorTransformer();
    }

    public Completable validateEmail(String email, String deviceName, String mac) {
        ValidateEmailRequest request = new ValidateEmailRequest();
        request.email = email;
        request.deviceName = deviceName;
        request.mac = mac;
        request.apiKey = BuildConfig.CLOUD_VALIDATE_API_KEY;
        return cloudValidateApi
                .validateEmail(request)
                .retry(remoteRetry)
                .compose(dealWithError())
                .toCompletable();
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) remoteErrorTransformer;
    }
}
