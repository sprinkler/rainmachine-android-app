package com.rainmachine.data.remote.cloud;

import com.rainmachine.data.remote.cloud.request.CheckCloudAccountRequest;
import com.rainmachine.data.remote.cloud.request.CloudRequest;
import com.rainmachine.data.remote.cloud.response.CheckCloudAccountResponse;
import com.rainmachine.data.remote.cloud.response.CloudResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CloudSprinklersApi {

    @POST("get-sprinklers")
    Single<CloudResponse> getSprinklers(@Body CloudRequest request);

    @POST("check-cloud-account")
    Single<CheckCloudAccountResponse> checkCloudAccount(@Body CheckCloudAccountRequest request);
}
