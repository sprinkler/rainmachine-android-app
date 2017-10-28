package com.rainmachine.data.remote.cloud;

import com.rainmachine.data.remote.cloud.request.ValidateEmailRequest;
import com.rainmachine.data.remote.cloud.response.BaseResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CloudValidateApi {

    @POST("validate-email")
    Single<BaseResponse> validateEmail(@Body ValidateEmailRequest request);
}
