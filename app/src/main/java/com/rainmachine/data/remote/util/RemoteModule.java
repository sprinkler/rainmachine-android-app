package com.rainmachine.data.remote.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.rainmachine.BuildConfig;
import com.rainmachine.data.boundary.CloudRepositoryImpl;
import com.rainmachine.data.boundary.ZoneImageRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.remote.cloud.CloudPushApi;
import com.rainmachine.data.remote.cloud.CloudSprinklersApi;
import com.rainmachine.data.remote.cloud.CloudSprinklersApiDelegate;
import com.rainmachine.data.remote.cloud.CloudValidateApi;
import com.rainmachine.data.remote.cloud.CloudValidateApiDelegate;
import com.rainmachine.data.remote.cloud.PushNotificationsDataStoreRemote;
import com.rainmachine.data.remote.firebase.FirebaseDataStore;
import com.rainmachine.data.remote.google.GoogleApi;
import com.rainmachine.data.remote.google.GoogleApiDelegate;
import com.rainmachine.data.remote.netatmo.NetatmoApi;
import com.rainmachine.data.remote.netatmo.NetatmoApiDelegate;
import com.rainmachine.data.remote.wunderground.WUndergroundApi;
import com.rainmachine.data.remote.wunderground.WundergroundApiDelegate;
import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.infrastructure.util.RainApplication;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module(
        complete = false,
        library = true
)
public class RemoteModule {

    private static final String BASE_GOOGLE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String BASE_WUNDERGROUND_URL = "http://api.wunderground.com/api/";
    private static final String BASE_NETATMO_URL = "https://api.netatmo.net/";

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Context context, SSLSocketFactory sslSocketFactory,
                                     X509TrustManager trustManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        builder.hostnameVerifier((hostname, session) -> true);
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(40, TimeUnit.SECONDS);
        File cacheDir = new File(context.getCacheDir(), "rainmachine-cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        Cache cache = new Cache(cacheDir, 50 * 1024 * 1024);
        builder.cache(cache);

        if (RainApplication.isDebugLogging()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        return builder.build();
    }

    @Provides
    @Singleton
    X509TrustManager provideTrustManager() {
        // Create a trust manager that does not validate certificate chains
        return new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws
                    CertificateException {
            }

            @Override
            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws
                    CertificateException {
            }
        };
    }

    @Provides
    @Singleton
    SSLSocketFactory provideSSLSocketFactory(X509TrustManager trustManager) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (Throwable throwable) {
            Timber.w(throwable);
        }
        return null;
    }

    @Provides
    @Singleton
    @Named("cloud_endpoint")
    String provideCloudSprinklersEndpoint() {
        return BuildConfig.CLOUD_SPRINKLERS_LIVE_URL;
    }

    @Provides
    @Singleton
    CloudSprinklersApiDelegate provideCloudSprinklersApiDelegate(@Named("cloud_endpoint") String
                                                                         endpoint,
                                                                 OkHttpClient okHttpClient,
                                                                 Gson gson,
                                                                 InfrastructureService
                                                                         infrastructureService) {
        CloudSprinklersApi cloudSprinklersApi = new Retrofit.Builder()
                .baseUrl(retrofitUrl(endpoint))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io
                        ()))
                .client(okHttpClient)
                .build()
                .create(CloudSprinklersApi.class);
        return new CloudSprinklersApiDelegate(cloudSprinklersApi, infrastructureService);
    }

    @Provides
    @Singleton
    @Named("cloud_validate_endpoint")
    String provideCloudValidateEndpoint() {
        return BuildConfig.CLOUD_VALIDATE_LIVE_URL;
    }

    @Provides
    @Singleton
    CloudValidateApiDelegate provideCloudValidateApiDelegate(@Named("cloud_validate_endpoint")
                                                                     String endpoint,
                                                             OkHttpClient okHttpClient, Gson gson) {
        CloudValidateApi cloudValidateApi = new Retrofit.Builder()
                .baseUrl(retrofitUrl(endpoint))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build()
                .create(CloudValidateApi.class);
        return new CloudValidateApiDelegate(cloudValidateApi);
    }

    @Provides
    @Singleton
    CloudRepository provideCloudRepository(CloudValidateApiDelegate cloudValidateApiDelegate,
                                           CloudSprinklersApiDelegate cloudSprinklersApiDelegate) {
        return new CloudRepositoryImpl(cloudValidateApiDelegate, cloudSprinklersApiDelegate);
    }

    @Provides
    @Singleton
    @Named("cloud_push_endpoint")
    String provideCloudPushEndpoint() {
        return BuildConfig.CLOUD_PUSH_LIVE_URL;
    }

    @Provides
    @Singleton
    CloudPushApi provideCloudPushApi(@Named("cloud_push_endpoint") String endpoint,
                                     OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(retrofitUrl(endpoint))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build()
                .create(CloudPushApi.class);
    }

    @Provides
    @Singleton
    GoogleApiDelegate provideGoogleApiDelegate(OkHttpClient okHttpClient, Gson gson) {
        GoogleApi googleApi = new Retrofit.Builder()
                .baseUrl(retrofitUrl(BASE_GOOGLE_URL))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build()
                .create(GoogleApi.class);
        return new GoogleApiDelegate(googleApi);
    }

    @Provides
    @Singleton
    WundergroundApiDelegate provideWUndergroundApiDelegate(OkHttpClient okHttpClient, Gson gson) {
        WUndergroundApi wUndergroundApi = new Retrofit.Builder()
                .baseUrl(retrofitUrl(BASE_WUNDERGROUND_URL))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build()
                .create(WUndergroundApi.class);
        return new WundergroundApiDelegate(wUndergroundApi);
    }

    @Provides
    @Singleton
    NetatmoApiDelegate provideNetatmoApiDelegate(OkHttpClient okHttpClient, Gson gson) {
        NetatmoApi netatmoApi = new Retrofit.Builder()
                .baseUrl(retrofitUrl(BASE_NETATMO_URL))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers
                        .io()))
                .client(okHttpClient)
                .build()
                .create(NetatmoApi.class);
        return new NetatmoApiDelegate(netatmoApi);
    }

    @Provides
    @Singleton
    FirebaseDataStore provideFirebaseDataStore() {
        return new FirebaseDataStore();
    }

    @Provides
    @Singleton
    ZoneImageRepository provideZoneImageRepository(FirebaseDataStore firebaseDataStore,
                                                   DatabaseRepositoryImpl databaseRepository) {
        return new ZoneImageRepositoryImpl(firebaseDataStore, databaseRepository);
    }

    @Provides
    @Singleton
    PushNotificationsDataStoreRemote providePushNotificationsRemoteDataStore(CloudPushApi
                                                                                     cloudPushApi, Gson gson) {
        return new PushNotificationsDataStoreRemote(cloudPushApi, gson);
    }

    private String retrofitUrl(@NonNull String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }
}
