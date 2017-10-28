package com.rainmachine.data.remote.sprinkler.v3;

import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.infrastructure.SprinklerUtils;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SprinklerApiRequestInterceptor3 implements Interceptor {

    private SprinklerUtils sprinklerUtils;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    public SprinklerApiRequestInterceptor3(SprinklerUtils sprinklerUtils,
                                           SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        this.sprinklerUtils = sprinklerUtils;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (sprinklerUtils.isAuthenticated()) {
            Headers headers = request.headers().newBuilder()
                    .add("Cookie", sprinklerPrefsRepository.sessionCookie())
                    .build();
            request = request.newBuilder().headers(headers).build();
        }
        return chain.proceed(request);
    }
}
