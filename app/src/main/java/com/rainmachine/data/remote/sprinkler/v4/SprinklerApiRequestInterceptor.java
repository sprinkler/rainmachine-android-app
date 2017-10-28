package com.rainmachine.data.remote.sprinkler.v4;

import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.infrastructure.SprinklerUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SprinklerApiRequestInterceptor implements Interceptor {

    private SprinklerUtils sprinklerUtils;
    private SprinklerPrefRepositoryImpl sprinklerPrefsRepository;

    public SprinklerApiRequestInterceptor(SprinklerUtils sprinklerUtils,
                                          SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        this.sprinklerUtils = sprinklerUtils;
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (sprinklerUtils.isAuthenticated()) {
            HttpUrl url = request.url().newBuilder()
                    .addQueryParameter("access_token", sprinklerPrefsRepository.sessionCookie())
                    .build();
            request = request.newBuilder().url(url).build();
        }
        return chain.proceed(request);
    }
}
