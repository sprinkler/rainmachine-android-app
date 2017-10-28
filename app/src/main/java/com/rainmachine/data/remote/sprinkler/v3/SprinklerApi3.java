package com.rainmachine.data.remote.sprinkler.v3;

import com.rainmachine.data.remote.sprinkler.v3.request.PasswordRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.ProgramRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.RainDelayRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.TimeDateRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.UnitsRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.UpdateRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.WaterZoneRequest3;
import com.rainmachine.data.remote.sprinkler.v3.request.ZonePropertiesRequest3;
import com.rainmachine.data.remote.sprinkler.v3.response.BaseResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.MessageResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ProgramsResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.RainDelayResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.TimeDateResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.UnitsResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.UpdateResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.WeatherDataResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZoneResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesPropertiesResponse3;
import com.rainmachine.data.remote.sprinkler.v3.response.ZonesResponse3;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SprinklerApi3 {

    @GET("ui.cgi?action=stopall")
    Single<ResponseBody> stopAll3();

    @GET("api/3/update")
    Single<UpdateResponse3> getUpdate3();

    @POST("api/3/update")
    Single<BaseResponse3> makeUpdate3(@Body UpdateRequest3 updateRequest);

    @POST("ui.cgi?action=settings&what=run_now")
    Single<MessageResponse3> runStopProgram3(@Query("pid") long programId,
                                             @Body String emptyBody);

    @GET("ui.cgi?action=weatherdata")
    Single<WeatherDataResponse3> getWeatherData3();

    @GET("ui.cgi?action=settings&what=programs")
    Single<ProgramsResponse3> getPrograms3();

    /* If you want to create a new program, set the program.id to -1. Else,
    if you want to update a program, use the id of the program
     */
    @POST("ui.cgi?action=settings&what=programs")
    Single<MessageResponse3> createUpdateProgram3(@Body ProgramRequest3 programRequest);

    @FormUrlEncoded
    @POST("ui.cgi")
    Single<MessageResponse3> deleteProgram3(@Field("action") String action,
                                            @Field("what") String what,
                                            @Field("pid") String pid);

    @GET("ui.cgi?action=settings&what=rainDelay")
    Single<RainDelayResponse3> getRainDelay3();

    @POST("ui.cgi?action=settings&what=rainDelay")
    Single<MessageResponse3> setRainDelay3(@Body RainDelayRequest3 rainDelayRq);

    @POST("ui.cgi?action=settings&what=password")
    Single<MessageResponse3> changePassword3(@Body PasswordRequest3 passwordRequest);

    @GET("ui.cgi?action=settings&what=timedate")
    Single<TimeDateResponse3> getTimeDate3();

    @POST("ui.cgi?action=settings&what=timedate")
    Single<MessageResponse3> setTimeDate3(@Body TimeDateRequest3 timeDateRequest);

    @GET("ui.cgi?action=settings&what=units")
    Single<UnitsResponse3> getUnits3();

    @POST("ui.cgi?action=settings&what=units")
    Single<MessageResponse3> setUnits3(@Body UnitsRequest3 unitsRequest);

    @GET("ui.cgi?action=zones")
    Single<ZonesResponse3> getZones3();

    @GET("ui.cgi?action=zoneedit")
    Single<ZoneResponse3> getZone3(@Query("zid") long zoneId);

    @POST("ui.cgi?action=zonesave&from=zoneedit")
    Single<ResponseBody> waterZone3(@Query("zid") long zoneId,
                                    @Body WaterZoneRequest3 waterZoneRequest);

    @GET("ui.cgi?action=settings&what=zones")
    Single<ZonesPropertiesResponse3> getZonesProperties3();

    @POST("ui.cgi?action=settings&what=zones")
    Single<MessageResponse3> saveZoneProperties3(@Query("zid") long zoneId,
                                                 @Body ZonePropertiesRequest3 zoneRequest);
}
