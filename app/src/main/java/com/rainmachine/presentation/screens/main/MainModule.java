package com.rainmachine.presentation.screens.main;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.pref.SprinklerPrefRepositoryImpl;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.notifiers.HandPreferenceNotifier;
import com.rainmachine.domain.notifiers.ManualStopAllWateringNotifier;
import com.rainmachine.domain.notifiers.ProgramChangeNotifier;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.notifiers.ZonePropertiesChangeNotifier;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.usecases.handpreference.GetHandPreference;
import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.domain.usecases.remoteaccess.SendConfirmationEmail;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.usecases.watering.GetWateringLive;
import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.infrastructure.AppManager;
import com.rainmachine.infrastructure.SprinklerManager;
import com.rainmachine.infrastructure.UpdateHandler;
import com.rainmachine.injection.SprinklerModule;
import com.rainmachine.presentation.dialogs.ActionMessageDialogFragment;
import com.rainmachine.presentation.dialogs.ActionMessageParcelableDialogFragment;
import com.rainmachine.presentation.dialogs.InfoMessageDialogFragment;
import com.rainmachine.presentation.screens.programdetailsold.ZoneDurationDialogFragment;
import com.rainmachine.presentation.screens.programs.MoreProgramActionsDialogFragment;
import com.rainmachine.presentation.screens.programs.ProgramsContract;
import com.rainmachine.presentation.screens.programs.ProgramsPresenter;
import com.rainmachine.presentation.screens.programs.ProgramsView;
import com.rainmachine.presentation.screens.settings.SettingsPresenter;
import com.rainmachine.presentation.screens.settings.SettingsView;
import com.rainmachine.presentation.screens.stats.StatsContract;
import com.rainmachine.presentation.screens.stats.StatsGraphDialogFragment;
import com.rainmachine.presentation.screens.stats.StatsMixer;
import com.rainmachine.presentation.screens.stats.StatsPresenter;
import com.rainmachine.presentation.screens.stats.StatsView;
import com.rainmachine.presentation.screens.waternow.WaterNowContract;
import com.rainmachine.presentation.screens.waternow.WaterNowPresenter;
import com.rainmachine.presentation.screens.waternow.WaterNowView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        addsTo = SprinklerModule.class,
        complete = false,
        library = true,
        injects = {
                MainActivity.class,
                ActionMessageDialogFragment.class,
                ActionMessageParcelableDialogFragment.class,
                InfoMessageDialogFragment.class,
                StatsGraphDialogFragment.class,
                ZoneDurationDialogFragment.class,
                StatsView.class,
                WaterNowView.class,
                SettingsView.class,
                ProgramsView.class,
                MoreProgramActionsDialogFragment.class,
                NewUpdateDialogFragment.class
        }
)
class MainModule {

    private MainActivity activity;

    MainModule(MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    MainActivity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    ActionMessageDialogFragment.Callback provideActionMessageDialogCallback(MainPresenter
                                                                                    mainPresenter) {
        return mainPresenter;
    }

    @Provides
    @Singleton
    ActionMessageParcelableDialogFragment.Callback provideActionMessageParcelableDialogCallback
            (MainPresenter mainPresenter) {
        return mainPresenter;
    }

    @Provides
    @Singleton
    InfoMessageDialogFragment.Callback provideInfoMessageDialogCallback(MainPresenter
                                                                                mainPresenter) {
        return mainPresenter;
    }

    @Provides
    @Singleton
    StatsGraphDialogFragment.Callback provideStatsGraphDialogCallback(StatsContract.Presenter
                                                                              statsPresenter) {
        return statsPresenter;
    }

    @Provides
    @Singleton
    ZoneDurationDialogFragment.Callback provideZoneDurationDialogCallback(WaterNowContract.Presenter
                                                                                  waterNowPresenter) {
        return waterNowPresenter;
    }

    @Provides
    @Singleton
    MainPresenter providePresenter(Device device, MainActivity activity,
                                   Features features, MainMixer mixer,
                                   UpdateHandler updateHandler,
                                   GetRestrictionsLive getRestrictionsLive,
                                   DeviceNameStore deviceNameStore,
                                   GetWateringLive getWateringLive,
                                   SprinklerPrefRepositoryImpl sprinklerPrefsRepository) {
        return new MainPresenter(activity, features, mixer, updateHandler, getRestrictionsLive,
                deviceNameStore, getWateringLive, sprinklerPrefsRepository);
    }

    @Provides
    @Singleton
    StatsContract.Presenter providePresenter(MainActivity activity, StatsMixer mixer,
                                             StatsNeedRefreshNotifier statsNeedRefreshNotifier,
                                             Features features) {
        return new StatsPresenter(activity, mixer, statsNeedRefreshNotifier, features);
    }

    @Provides
    @Singleton
    WaterNowContract.Presenter providePresenter(Features features, Device device,
                                                SprinklerRepositoryImpl sprinklerRepository,
                                                DatabaseRepositoryImpl databaseRepository,
                                                GetHandPreference getHandPreference,
                                                SaveWateringDuration saveWateringDuration,
                                                ProgramChangeNotifier programChangeNotifier,
                                                ZonePropertiesChangeNotifier
                                                        zonePropertiesChangeNotifier,
                                                ManualStopAllWateringNotifier
                                                        manualStopAllWateringNotifier,
                                                HandPreferenceNotifier handPreferenceNotifier,
                                                SchedulerProvider schedulerProvider) {
        return new WaterNowPresenter(activity, features, device, sprinklerRepository,
                databaseRepository, getHandPreference, saveWateringDuration, programChangeNotifier,
                zonePropertiesChangeNotifier, manualStopAllWateringNotifier,
                handPreferenceNotifier, schedulerProvider);
    }

    @Provides
    @Singleton
    ProgramsContract.Presenter providePresenter(MainActivity activity,
                                                SprinklerRepositoryImpl sprinklerRepository,
                                                Features features,
                                                GetHandPreference getHandPreference,
                                                ManualStopAllWateringNotifier
                                                        manualStopAllWateringNotifier,
                                                ProgramChangeNotifier programChangeNotifier,
                                                StatsNeedRefreshNotifier statsNeedRefreshNotifier,
                                                HandPreferenceNotifier handPreferenceNotifier,
                                                SaveProgram saveProgram,
                                                SchedulerProvider schedulerProvider) {
        return new ProgramsPresenter(features, sprinklerRepository, getHandPreference,
                programChangeNotifier, statsNeedRefreshNotifier, saveProgram,
                manualStopAllWateringNotifier, handPreferenceNotifier, schedulerProvider);
    }

    @Provides
    @Singleton
    SettingsPresenter providePresenter(MainActivity activity, Features features) {
        return new SettingsPresenter(activity, features);
    }

    @Provides
    @Singleton
    MainMixer provideMainMixer(Device device, Features features,
                               AppManager appManager, TriggerUpdateCheck triggerUpdateCheck,
                               UpdateHandler updateHandler,
                               SprinklerRepositoryImpl sprinklerRepository,
                               SprinklerPrefRepositoryImpl sprinklerPrefsRepository,
                               SprinklerManager sprinklerManager,
                               SendConfirmationEmail sendConfirmationEmail) {
        return new MainMixer(device, features, updateHandler, triggerUpdateCheck,
                sprinklerRepository, sprinklerPrefsRepository, sprinklerManager,
                sendConfirmationEmail);
    }
}
