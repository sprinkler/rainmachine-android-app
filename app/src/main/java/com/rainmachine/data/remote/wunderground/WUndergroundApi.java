package com.rainmachine.data.remote.wunderground;

import com.rainmachine.data.remote.wunderground.response.ConditionsResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WUndergroundApi {

    @GET("{key}/conditions/q/CA/San_Francisco.json")
    Single<ConditionsResponse> checkDeveloperApiKey(@Path("key") String key);
}
