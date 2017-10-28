package com.rainmachine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.rainmachine.infrastructure.Sleeper;

import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RxJavaTest {

    private Api api;

    @Before
    public void setup() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        OkHttpClient okHttpClient = builder.build();
        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build();
        api = retrofit.create(Api.class);
    }

    @Test
    public void replayRefCount() {
        Relay<Long> forceRefresh = PublishRelay.<Long>create().toSerialized();
        Observable<Long> interval = Observable.interval(0, 5, TimeUnit.SECONDS);

        Observable<ResponseBody> network = Observable.merge(forceRefresh, interval)
                .switchMap(step -> {
                    System.out.println("API call for step " + step);
                    return apiCall();
                });
        Observable<ResponseBody> stream = network.replay(1).refCount();

        CompositeDisposable disposables = new CompositeDisposable();
        disposables.add(stream.subscribe(responseBody -> {
            System.out.println("Subscriber 1 Received body " + responseBody);
        }));

        Sleeper.sleep(11 * DateTimeConstants.MILLIS_PER_SECOND);

        forceRefresh.accept(-1L);

        Sleeper.sleep(2 * DateTimeConstants.MILLIS_PER_SECOND);

        disposables.clear();

        Sleeper.sleep(13 * DateTimeConstants.MILLIS_PER_SECOND);

        disposables.add(stream.subscribe(responseBody -> {
            System.out.println("Subscriber 2 Received body " + responseBody);
        }));

        Sleeper.sleep(13 * DateTimeConstants.MILLIS_PER_SECOND);
    }

    private Observable<ResponseBody> apiCall() {
        return api.geocode("Los Angeles, California");
    }

    private String BASE_URL = "https://maps.googleapis.com/maps/api/";

    interface Api {
        @GET("geocode/json?sensor=true")
        Observable<ResponseBody> geocode(@Query("address") String address);
    }
}
