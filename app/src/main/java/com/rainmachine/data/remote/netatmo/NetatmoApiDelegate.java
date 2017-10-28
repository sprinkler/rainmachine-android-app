package com.rainmachine.data.remote.netatmo;

import io.reactivex.Single;

public class NetatmoApiDelegate {

    private NetatmoApi netatmoApi;

    public NetatmoApiDelegate(NetatmoApi netatmoApi) {
        this.netatmoApi = netatmoApi;
    }

    public Single<Boolean> checkCredentials(String username, String password) {
        return netatmoApi
                .checkNetatmoUser(username, password, "5614c091c8bd0056218b4569",
                        "bCHWLP8GqDFxzqP2hYWac0OmpiW85gEGY2R", "password", "read_station")
                .flatMap(response -> Single.just(true))
                .onErrorResumeNext(Single.just(false));
    }
}
