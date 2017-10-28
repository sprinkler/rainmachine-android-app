package com.rainmachine.data.remote.cloud;

import com.rainmachine.data.remote.cloud.request.ToggleNotificationRequest;
import com.rainmachine.data.remote.cloud.request.UpdatePushRegistrationRequest;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface CloudPushApi {

    @POST("submit-device-identifier")
    Single<ResponseBody> updateRegistration(@Body UpdatePushRegistrationRequest request);

    @PUT("toggle-notification")
    Single<ResponseBody> toggleNotification(@Body ToggleNotificationRequest request);

    @GET("status")
    Single<ResponseBody> notificationsStatus(@Query("phoneId") String phoneId);

    @GET("test-push-notification")
    Single<ResponseBody> triggerNotification(@Query("token") String token,
                                             @Query("notificationType") int notificationType,
                                             @Query("os") String os,
                                             @Query("event") String event,
                                             @Query("isUnitsMetric") int isUnitsMetric,
                                             @Query("use24HourFormat") int use24HourFormat);
}
