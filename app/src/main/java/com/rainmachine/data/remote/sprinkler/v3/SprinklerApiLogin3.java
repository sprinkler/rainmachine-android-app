package com.rainmachine.data.remote.sprinkler.v3;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SprinklerApiLogin3 {

    @FormUrlEncoded
    @POST("ui.cgi")
    Single<ResponseBody> login3(@Field("action") String action, @Field("user") String user,
                                @Field("password") String pass, @Field("remember") String
                                        remember);
}
