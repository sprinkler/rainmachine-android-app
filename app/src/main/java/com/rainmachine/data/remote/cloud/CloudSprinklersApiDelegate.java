package com.rainmachine.data.remote.cloud;

import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.remote.cloud.mapper.SprinklersCloudResponseMapper;
import com.rainmachine.data.remote.cloud.request.CheckCloudAccountRequest;
import com.rainmachine.data.remote.cloud.request.CloudCredentialRequest;
import com.rainmachine.data.remote.cloud.request.CloudRequest;
import com.rainmachine.data.remote.util.RemoteErrorTransformer;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.CheckCloudAccountStatus;
import com.rainmachine.domain.model.CloudEntry;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import kotlin.Pair;

public class CloudSprinklersApiDelegate {

    private CloudSprinklersApi cloudSprinklersApi;
    private InfrastructureService infrastructureService;
    private RemoteErrorTransformer remoteErrorTransformer;

    public CloudSprinklersApiDelegate(CloudSprinklersApi cloudSprinklersApi,
                                      InfrastructureService infrastructureService) {
        this.cloudSprinklersApi = cloudSprinklersApi;
        this.infrastructureService = infrastructureService;
        remoteErrorTransformer = new RemoteErrorTransformer();
    }

    public Single<CheckCloudAccountStatus> checkCloudAccount(CloudInfo cloudInfo) {
        CheckCloudAccountRequest request = new CheckCloudAccountRequest();
        request.email = cloudInfo.email;
        request.pwd = cloudInfo.password;
        request.phoneId = infrastructureService.getSystemId();
        return cloudSprinklersApi
                .checkCloudAccount(request)
                .flatMap(response -> {
                    if (response.knownCount == 0) {
                        return Single.just(CheckCloudAccountStatus.ERROR_TYPE_NO_DEVICE);
                    } else if (response.activeCount == 0) {
                        return Single.just(CheckCloudAccountStatus
                                .ERROR_TYPE_NOT_CONNECTED_INTERNET);
                    } else if (response.authCount == 0) {
                        return Single.just(CheckCloudAccountStatus
                                .ERROR_TYPE_INVALID_PASSWORD);
                    } else {
                        return Single.just(CheckCloudAccountStatus.SUCCESS);
                    }
                })
                .onErrorReturn(throwable -> CheckCloudAccountStatus.ERROR_TYPE_SERVER_PROBLEM);
    }

    public Single<List<CloudEntry>> cloudEntries(List<Pair<String, String>> emailPasswordPairs) {
        CloudRequest request = new CloudRequest();
        request.phoneID = infrastructureService.getPhoneId();
        request.credentials = new ArrayList<>();
        for (Pair<String, String> emailPasswordPair : emailPasswordPairs) {
            CloudCredentialRequest credentialRequest = new CloudCredentialRequest();
            credentialRequest.email = emailPasswordPair.getFirst();
            credentialRequest.pwd = emailPasswordPair.getSecond();
            request.credentials.add(credentialRequest);
        }
        return cloudSprinklersApi
                .getSprinklers(request)
                .map(SprinklersCloudResponseMapper.instance())
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) remoteErrorTransformer;
    }
}
