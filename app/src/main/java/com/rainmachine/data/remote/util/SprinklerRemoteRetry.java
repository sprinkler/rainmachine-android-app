package com.rainmachine.data.remote.util;

import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.infrastructure.InfrastructureUtils;
import com.rainmachine.infrastructure.NetworkUtils;

import java.io.IOException;

import io.reactivex.functions.BiPredicate;
import retrofit2.HttpException;
import retrofit2.Response;

public class SprinklerRemoteRetry implements BiPredicate<Integer, Throwable> {

    private static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    private final Device device;
    private final BaseUrlSelectionInterceptor baseUrlSelectionInterceptor;

    public SprinklerRemoteRetry(Device device, BaseUrlSelectionInterceptor
            baseUrlSelectionInterceptor) {
        this.device = device;
        this.baseUrlSelectionInterceptor = baseUrlSelectionInterceptor;
    }

    @Override
    public boolean test(Integer attempts, Throwable throwable) {
        if (attempts > 2) {
            return false;
        }
        if (throwable instanceof HttpException) {
            // We had non-2XX http error
            HttpException httpException = (HttpException) throwable;
            Response response = httpException.response();
            if (response != null && response.code() == HTTP_STATUS_INTERNAL_SERVER_ERROR) {
                return true;
            }
        }
        if (throwable instanceof IOException) {
            // A network error happened
            if (InfrastructureUtils.shouldSwitchToCloud(device)) {
                InfrastructureUtils.switchToCloud(device, baseUrlSelectionInterceptor);
            } else if (InfrastructureUtils.shouldSwitchToWifi(device)) {
                InfrastructureUtils.switchToWifi(device, baseUrlSelectionInterceptor);
            } else if (InfrastructureUtils.shouldRouteNetworkTrafficToWiFi(device)) {
                boolean success = NetworkUtils.routeNetworkTrafficToCurrentWiFi();
                if (!success) {
                    InfrastructureUtils.finishAllSprinklerActivities();
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}
