package com.rainmachine.injection;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.rainmachine.data.boundary.BackupRepositoryImpl;
import com.rainmachine.data.boundary.RemoteAccessAccountRepositoryImpl;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.cache.CacheManager;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefModule;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApi3;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApiDelegate3;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApiLogin3;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApiRequestInterceptor3;
import com.rainmachine.data.remote.sprinkler.v3.SprinklerApiUtils3;
import com.rainmachine.data.remote.sprinkler.v4.SprinklerApi;
import com.rainmachine.data.remote.sprinkler.v4.SprinklerApiDelegate;
import com.rainmachine.data.remote.sprinkler.v4.SprinklerApiRequestInterceptor;
import com.rainmachine.data.remote.sprinkler.v4.SprinklerApiUtils;
import com.rainmachine.data.remote.util.BaseUrlSelectionInterceptor;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer;
import com.rainmachine.data.remote.util.SprinklerRemoteErrorTransformer3;
import com.rainmachine.data.remote.util.SprinklerRemoteRetry;
import com.rainmachine.domain.boundary.data.BackupRepository;
import com.rainmachine.domain.boundary.data.RemoteAccessAccountRepository;
import com.rainmachine.domain.boundary.data.SprinklerPrefRepository;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.boundary.infrastructure.Analytics;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.usecases.zoneimage.SyncZoneImages;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.LocationHandler;
import com.rainmachine.infrastructure.SprinklerManager;
import com.rainmachine.infrastructure.SprinklerUtils;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.infrastructure.scheduler.AndroidSchedulerProvider;
import com.rainmachine.presentation.screens.stats.StatsMixer;
import com.rainmachine.presentation.util.ForegroundDetector;
import com.rainmachine.presentation.util.formatter.CalendarFormatter;
import com.rainmachine.presentation.util.formatter.DecimalFormatter;
import com.rainmachine.presentation.util.formatter.HourlyRestrictionFormatter;
import com.rainmachine.presentation.util.formatter.ParserFormatter;
import com.rainmachine.presentation.util.formatter.ProgramFormatter;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(
        complete = false,
        library = true,
        addsTo = AppModule.class,
        includes = {
                SprinklerPrefModule.class,
                SprinklerDomainModule.class,
                SprinklerHandlerModule.class
        },
        injects = {
                SprinklerManager.class,

                UpdateHandler.class,

                SprinklerRepositoryImpl.class,
                SprinklerPrefRepositoryImpl.class
        }
)
public class SprinklerModule {
    private final Device device;

    public SprinklerModule(Device device) {
        this.device = device;
    }

    @Provides
    @Singleton
    public Device provideDevice() {
        return device;
    }

    @Provides
    @Singleton
    SprinklerState provideSprinklerStateManager() {
        return new SprinklerState();
    }

    @Provides
    @Singleton
    SprinklerApi provideSprinklerApi(OkHttpClient okHttpClient, Gson gson,
                                     SprinklerUtils sprinklerUtils, Device device,
                                     BaseUrlSelectionInterceptor baseUrlSelectionInterceptor,
                                     SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        OkHttpClient interceptorClient = okHttpClient.newBuilder()
                .addInterceptor(new SprinklerApiRequestInterceptor(sprinklerUtils,
                        sprinklerPrefsRepository))
                .addInterceptor(baseUrlSelectionInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(retrofitUrl(device.getUrl()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io
                        ()))
                .client(interceptorClient)
                .build();
        return retrofit.create(SprinklerApi.class);
    }

    @Provides
    @Singleton
    SprinklerApiUtils provideSprinklerApiUtils(Gson gson) {
        return new SprinklerApiUtils(gson);
    }

    @Provides
    @Singleton
    BaseUrlSelectionInterceptor provideHostSelectionInterceptor(Device device) {
        return new BaseUrlSelectionInterceptor(device.getUrl());
    }

    @Provides
    @Singleton
    SprinklerApi3 provideSprinklerApi3(OkHttpClient okHttpClient, Gson gson,
                                       Device device, SprinklerUtils sprinklerUtils,
                                       SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        OkHttpClient interceptorClient = okHttpClient.newBuilder()
                .addInterceptor(new SprinklerApiRequestInterceptor3(sprinklerUtils,
                        sprinklerPrefsRepository))
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(retrofitUrl(device.getUrl()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io
                        ()))
                .client(interceptorClient)
                .build();
        return retrofit.create(SprinklerApi3.class);
    }

    @Provides
    @Singleton
    SprinklerApiLogin3 provideSprinklerApiLogin3(OkHttpClient okHttpClient, Gson gson,
                                                 Device device, SprinklerUtils sprinklerUtils,
                                                 SprinklerPrefRepositoryImpl
                                                         sprinklerPrefsRepository) {
        OkHttpClient noRedirectInterceptorClient = okHttpClient.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .addInterceptor(new SprinklerApiRequestInterceptor3(sprinklerUtils,
                        sprinklerPrefsRepository))
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(retrofitUrl(device.getUrl()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io
                        ()))
                .client(noRedirectInterceptorClient)
                .build();
        return retrofit.create(SprinklerApiLogin3.class);
    }

    @Provides
    @Singleton
    SprinklerApiUtils3 provideSprinklerApiUtils3(Gson gson) {
        return new SprinklerApiUtils3(gson);
    }

    @Provides
    @Singleton
    Features provideFeatures(SprinklerPrefRepository sprinklerPrefsRepository) {
        return new Features(sprinklerPrefsRepository);
    }

    @Provides
    @Singleton
    SprinklerUtils provideSprinklerUtils(SprinklerPrefRepository sprinklerPrefsRepository) {
        return new SprinklerUtils(sprinklerPrefsRepository);
    }

    // For Location screen
    @Provides
    @Singleton
    LocationHandler provideLocationHandler(Context context, Bus bus) {
        return new LocationHandler(context, bus);
    }

    @Provides
    @Singleton
    SprinklerRepositoryImpl provideSprinklerRepositoryImpl(SprinklerApiDelegate
                                                                   sprinklerApiDelegate,
                                                           SprinklerApiDelegate3
                                                                   sprinklerApiDelegate3,
                                                           SprinklerApiUtils sprinklerApiUtils,
                                                           DatabaseRepositoryImpl
                                                                   databaseRepository,
                                                           Device device, Features features,
                                                           AppManager appManager,
                                                           SprinklerUtils sprinklerUtils,
                                                           OkHttpClient okHttpClient,
                                                           CacheManager cacheManager,
                                                           SprinklerState sprinklerState,
                                                           SprinklerPrefRepositoryImpl
                                                                   sprinklerPrefsRepository,
                                                           CrashReporter crashReporter,
                                                           Analytics analytics,
                                                           BackupRepositoryImpl backupRepository,
                                                           SprinklerRemoteErrorTransformer
                                                                   sprinklerRemoteErrorTransformer,
                                                           SprinklerRemoteErrorTransformer3
                                                                   sprinklerRemoteErrorTransformer3) {
        return new SprinklerRepositoryImpl(sprinklerApiDelegate, sprinklerApiDelegate3,
                databaseRepository, device, features, sprinklerUtils, cacheManager,
                sprinklerPrefsRepository, crashReporter, analytics, backupRepository,
                sprinklerRemoteErrorTransformer, sprinklerRemoteErrorTransformer3);
    }

    @Provides
    @Singleton
    SprinklerRepository provideSprinklerRepository(SprinklerRepositoryImpl sprinklerRepository) {
        return sprinklerRepository;
    }

    @Provides
    @Singleton
    SprinklerApiDelegate provideSprinklerApiDelegate(SprinklerApi sprinklerApi,
                                                     SprinklerApiUtils sprinklerApiUtils,
                                                     Gson gson, Features features,
                                                     SprinklerApiUtils3 sprinklerApiUtils3,
                                                     SprinklerRemoteRetry sprinklerRemoteRetry,
                                                     SprinklerRemoteErrorTransformer
                                                             sprinklerRemoteErrorTransformer) {
        return new SprinklerApiDelegate(sprinklerApi, sprinklerApiUtils, gson,
                sprinklerRemoteRetry, sprinklerRemoteErrorTransformer);
    }

    @Provides
    @Singleton
    SprinklerRemoteErrorTransformer provideSprinklerRemoteErrorSingleTransformer
            (SprinklerUtils sprinklerUtils, SprinklerApiUtils sprinklerApiUtils) {
        return new SprinklerRemoteErrorTransformer(sprinklerUtils, sprinklerApiUtils);
    }

    @Provides
    @Singleton
    SprinklerRemoteRetry provideSprinklerRemoteRetry(BaseUrlSelectionInterceptor
                                                             baseUrlSelectionInterceptor) {
        return new SprinklerRemoteRetry(device, baseUrlSelectionInterceptor);
    }

    @Provides
    @Singleton
    SprinklerApiDelegate3 provideSprinklerApiDelegate3(SprinklerApi3 sprinklerApi3,
                                                       SprinklerApiUtils sprinklerApiUtils,
                                                       SprinklerApiLogin3 sprinklerApiLogin3,
                                                       SprinklerRemoteRetry sprinklerRemoteRetry,
                                                       SprinklerRemoteErrorTransformer3
                                                               sprinklerRemoteErrorTransformer3) {
        return new SprinklerApiDelegate3(sprinklerApi3, sprinklerApiLogin3, sprinklerRemoteRetry,
                sprinklerRemoteErrorTransformer3);
    }

    @Provides
    @Singleton
    SprinklerRemoteErrorTransformer3 provideSprinklerRemoteErrorSingleTransformer3
            (SprinklerUtils sprinklerUtils,
             SprinklerApiUtils3 sprinklerApiUtils3) {
        return new SprinklerRemoteErrorTransformer3(sprinklerUtils, sprinklerApiUtils3);
    }

    @Provides
    @Singleton
    CacheManager provideCacheManager() {
        return new CacheManager();
    }

    @Provides
    @Singleton
    BackupRepositoryImpl provideBackupManager(DatabaseRepositoryImpl databaseRepository, Gson gson,
                                              Device device, SprinklerState sprinklerState,
                                              Features features) {
        return new BackupRepositoryImpl(databaseRepository, gson, device, sprinklerState, features);
    }

    @Provides
    @Singleton
    BackupRepository provideBackupRepository(BackupRepositoryImpl backupRepository) {
        return backupRepository;
    }

    @Provides
    @Singleton
    RemoteAccessAccountRepository provideRemoteAccessAccountRepository(DatabaseRepositoryImpl
                                                                               databaseRepository) {
        return new RemoteAccessAccountRepositoryImpl(databaseRepository);
    }

    @Provides
    @Singleton
    SprinklerManager provideSprinklerManager(Device device,
                                             ForegroundDetector foregroundDetector,
                                             SprinklerState sprinklerState,
                                             SprinklerUtils sprinklerUtils, AppManager appManager,
                                             SyncZoneImages syncZoneImages,
                                             GetMacAddress getMacAddress,
                                             BaseUrlSelectionInterceptor
                                                     baseUrlSelectionInterceptor,
                                             GetRestrictionsLive getRestrictionsLive) {
        return new SprinklerManager(device, foregroundDetector, sprinklerState,
                sprinklerUtils, syncZoneImages, baseUrlSelectionInterceptor, getRestrictionsLive);
    }

    @Provides
    @Singleton
    CalendarFormatter provideCalendarFormatter() {
        return new CalendarFormatter();
    }

    @Provides
    @Singleton
    ParserFormatter provideParserFormatter() {
        return new ParserFormatter();
    }

    @Provides
    @Singleton
    HourlyRestrictionFormatter provideHourlyRestrictionFormatter() {
        return new HourlyRestrictionFormatter();
    }

    @Provides
    @Singleton
    ProgramFormatter provideProgramFormatter(Context context, CalendarFormatter calendarFormatter) {
        return new ProgramFormatter(context, calendarFormatter);
    }

    @Provides
    @Singleton
    DecimalFormatter provideDecimalFormatter() {
        return new DecimalFormatter();
    }

    @Provides
    @Singleton
    SchedulerProvider provideSchedulerProvider() {
        return new AndroidSchedulerProvider();
    }

    @Provides
    @Singleton
    SprinklerPrefRepositoryImpl provideSprinklerPrefsRepositoryImpl() {
        return new SprinklerPrefRepositoryImpl();
    }

    @Provides
    @Singleton
    SprinklerPrefRepository provideSprinklerPrefsRepository(SprinklerPrefRepositoryImpl
                                                                    sprinklerPrefRepository) {
        return sprinklerPrefRepository;
    }

    @Provides
    @Singleton
    StatsMixer provideStatsMixer(Context context, Device device, Features features,
                                 DatabaseRepositoryImpl databaseRepository,
                                 SprinklerRepositoryImpl sprinklerRepository,
                                 CalendarFormatter formatter) {
        return new StatsMixer(context, device, features, databaseRepository, sprinklerRepository,
                formatter);
    }

    private String retrofitUrl(@NonNull String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }
}
