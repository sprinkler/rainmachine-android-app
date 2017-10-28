package com.rainmachine.data.remote.wunderground;

import com.rainmachine.data.remote.util.RemoteErrorTransformer;
import com.rainmachine.data.remote.util.RemoteRetry;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;

public class WundergroundApiDelegate {

    private WUndergroundApi wUndergroundApi;
    private RemoteRetry remoteRetry;
    private RemoteErrorTransformer remoteErrorTransformer;

    public WundergroundApiDelegate(WUndergroundApi wUndergroundApi) {
        this.wUndergroundApi = wUndergroundApi;
        remoteRetry = new RemoteRetry();
        remoteErrorTransformer = new RemoteErrorTransformer();
    }

    public Single<Boolean> checkDeveloperApiKey(String key) {
        return wUndergroundApi
                .checkDeveloperApiKey(key)
                .retry(remoteRetry)
                .flatMap(conditionsResponse ->
                        Single.just(conditionsResponse.current_observation != null))
                .compose(dealWithError());
    }

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> dealWithError() {
        return (SingleTransformer<T, T>) remoteErrorTransformer;
    }
}
