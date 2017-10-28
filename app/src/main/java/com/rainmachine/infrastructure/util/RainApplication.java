package com.rainmachine.infrastructure.util;

import com.google.firebase.FirebaseApp;
import com.rainmachine.BuildConfig;
import com.rainmachine.R;
import com.rainmachine.domain.boundary.infrastructure.CrashReporter;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.ProgramFrequency;
import com.rainmachine.domain.model.ProgramStartTime;
import com.rainmachine.domain.model.ProgramWateringTimes;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.Timberific;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.util.log.LumberYard;
import com.rainmachine.injection.Injector;
import com.rainmachine.presentation.util.ForegroundDetector;
import com.rainmachine.presentation.util.parcel.LocalDateParcelConverter;
import com.rainmachine.presentation.util.parcel.LocalDateTimeParcelConverter;
import com.squareup.leakcanary.LeakCanary;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.parceler.Parcel;
import org.parceler.ParcelClass;
import org.parceler.ParcelClasses;

import javax.inject.Inject;

import io.reactivex.plugins.RxJavaPlugins;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

@ParcelClasses({
        @ParcelClass(HourlyRestriction.class),
        @ParcelClass(ZoneProperties.class),
        @ParcelClass(Program.class),
        @ParcelClass(ProgramFrequency.class),
        @ParcelClass(ProgramStartTime.class),
        @ParcelClass(ProgramWateringTimes.class),
        @ParcelClass(value = LocalDate.class, annotation = @Parcel(converter =
                LocalDateParcelConverter.class)),
        @ParcelClass(value = LocalDateTime.class, annotation = @Parcel(converter =
                LocalDateTimeParcelConverter.class)),
        @ParcelClass(Parser.class),
        @ParcelClass(Parser.WUndergroundParams.class),
        @ParcelClass(Parser.WeatherStation.class),
        @ParcelClass(Parser.NetatmoParams.class),
        @ParcelClass(Parser.NetatmoModule.class)
})
public class RainApplication extends BaseApplication {

    @Inject
    protected AppManager appManager;
    @Inject
    CrashReporter crashReporter;
    // Keep this to have lifecycle callbacks always on
    @Inject
    ForegroundDetector foregroundDetector;

    private static Timber.Tree consoleLogTree;
    private static Timber.Tree memoryLogTree;

    @Override
    public void onCreate() {
        super.onCreate();

        setDebugLogging(isDebug());

        Injector.initAndInjectApp(this);

        if (isDebug()) {
            LeakCanary.install(this);
        } else {
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new
                    RainUncaughtExceptionHandler(getContext());
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
            crashReporter.init();
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath
                ("fonts/Roboto-Regular.ttf").setFontAttrId(R.attr.fontPath).build());
        try {
            EasyImage.configuration(this).setImagesFolderName("images").saveInAppExternalFilesDir();
        } catch (NullPointerException npe) {
            Timber.w("Saving zone images to external directory does not work");
        }

        FirebaseApp.initializeApp(this);
        RxJavaPlugins.setErrorHandler(new RxGlobalErrorHandler());
        appManager.initializeEveryColdStart();
    }

    public static RainApplication get() {
        return (RainApplication) getContext();
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static boolean isDebugLogging() {
        return consoleLogTree != null;
    }

    public static void setDebugLogging(boolean enabled) {
        if (enabled) {
            if (isDebugLogging()) {
                return;
            }
            LumberYard lumberYard = LumberYard.getInstance(BaseApplication.getContext());
            lumberYard.cleanUp();
            memoryLogTree = lumberYard.tree();
            Timber.plant(memoryLogTree);

            consoleLogTree = new Timber.DebugTree();
            Timber.plant(consoleLogTree);

            Timberific.init(true);
        } else {
            if (!isDebugLogging()) {
                return;
            }
            LumberYard lumberYard = LumberYard.getInstance(BaseApplication.getContext());
            lumberYard.cleanUp();
            Timber.uproot(memoryLogTree);
            memoryLogTree = null;
            Timber.uproot(consoleLogTree);
            consoleLogTree = null;

            Timberific.init(false);
        }
    }
}
