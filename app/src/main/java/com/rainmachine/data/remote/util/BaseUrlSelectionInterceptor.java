package com.rainmachine.data.remote.util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import timber.log.Timber;

public final class BaseUrlSelectionInterceptor implements Interceptor {

    private String baseUrl;
    private String oldBaseUrl;

    public BaseUrlSelectionInterceptor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        oldBaseUrl = this.baseUrl;
        this.baseUrl = baseUrl;
    }

    @Override
    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String fullUrl = request.url().toString();
        if (!fullUrl.startsWith(baseUrl)) {
            String modifiedUrl = fullUrl.replace(oldBaseUrl, baseUrl);
            Timber.d("Full url switch to : [" + modifiedUrl + "]");
            request = request.newBuilder()
                    .url(modifiedUrl)
                    .build();
        }
        return chain.proceed(request);
    }
}
