package com.rainmachine.data.remote.netatmo;

import com.rainmachine.data.remote.netatmo.response.NetatmoResponse;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface NetatmoApi {

    @FormUrlEncoded
    @POST("oauth2/token")
    Single<NetatmoResponse> checkNetatmoUser(@Field("username") String username,
                                             @Field("password") String password,
                                             @Field("client_id") String clientId,
                                             @Field("client_secret") String clientSecret,
                                             @Field("grant_type") String grantType,
                                             @Field("scope") String scope);
}
