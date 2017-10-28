package com.rainmachine.injection;

import com.google.gson.Gson;
import com.rainmachine.domain.boundary.data.BackupRepository;
import com.rainmachine.domain.boundary.data.CloudRepository;
import com.rainmachine.domain.boundary.data.RemoteAccessAccountRepository;
import com.rainmachine.domain.boundary.data.SprinklerRepository;
import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.notifiers.ManualStopAllWateringNotifier;
import com.rainmachine.domain.notifiers.ProgramChangeNotifier;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.notifiers.ZonePropertiesChangeNotifier;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.usecases.GetRainSensor;
import com.rainmachine.domain.usecases.TriggerUpdateCheck;
import com.rainmachine.domain.usecases.auth.CheckAuthenticationValid;
import com.rainmachine.domain.usecases.auth.LogInDefault;
import com.rainmachine.domain.usecases.backup.GetBackups;
import com.rainmachine.domain.usecases.backup.RestoreBackup;
import com.rainmachine.domain.usecases.program.SaveProgram;
import com.rainmachine.domain.usecases.pushnotification.CheckPushNotificationsPossible;
import com.rainmachine.domain.usecases.remoteaccess.CreateRemoteAccessAccount;
import com.rainmachine.domain.usecases.remoteaccess.EnableRemoteAccessEmail;
import com.rainmachine.domain.usecases.remoteaccess.SendConfirmationEmail;
import com.rainmachine.domain.usecases.remoteaccess.ToggleRemoteAccess;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsDetails;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.usecases.watering.GetWateringLive;
import com.rainmachine.domain.usecases.wateringduration.GetWateringDurationForZones;
import com.rainmachine.domain.usecases.wateringduration.SaveWateringDuration;
import com.rainmachine.domain.usecases.zoneimage.SyncZoneImages;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.SchedulerProvider;
import com.rainmachine.domain.util.SprinklerState;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class SprinklerDomainModule {

    @Provides
    @Singleton
    CheckPushNotificationsPossible provideCheckPushNotificationsPossible(SprinklerRepository
                                                                                 sprinklerRepository) {
        return new CheckPushNotificationsPossible(sprinklerRepository);
    }

    @Provides
    @Singleton
    GetMacAddress provideGetMacAddress(SprinklerRepository sprinklerRepository) {
        return new GetMacAddress(sprinklerRepository);
    }

    @Provides
    @Singleton
    SyncZoneImages provideSyncZoneImages(GetMacAddress getMacAddress,
                                         ZoneImageRepository zoneImageRepository,
                                         SprinklerRepository sprinklerRepository) {
        return new SyncZoneImages(getMacAddress, zoneImageRepository, sprinklerRepository);
    }

    @Provides
    @Singleton
    GetRainSensor provideGetRainSensor(SprinklerRepository sprinklerRepository) {
        return new GetRainSensor(sprinklerRepository);
    }

    @Provides
    @Singleton
    GetRestrictionsDetails provideGetCurrentRestrictions(SprinklerRepository
                                                                 sprinklerRepository,
                                                         GetRainSensor getRainSensor) {
        return new GetRestrictionsDetails(sprinklerRepository, getRainSensor);
    }

    @Provides
    @Singleton
    GetBackups provideGetBackups(BackupRepository backupRepository) {
        return new GetBackups(backupRepository);
    }

    @Provides
    @Singleton
    RestoreBackup provideRestoreBackup(SprinklerRepository sprinklerRepository,
                                       BackupRepository backupRepository,
                                       Gson gson, Features features, SprinklerState sprinklerState,
                                       StatsNeedRefreshNotifier statsNeedRefreshNotifier) {
        return new RestoreBackup(sprinklerRepository, backupRepository, features, sprinklerState,
                statsNeedRefreshNotifier);
    }

    @Provides
    @Singleton
    GetWateringLive provideGetWatering(SprinklerRepository sprinklerRepository,
                                       SprinklerState sprinklerState, Features features) {
        return new GetWateringLive(sprinklerRepository, sprinklerState, features);
    }

    @Provides
    @Singleton
    GetRestrictionsLive provideGetRestrictions(SprinklerRepository sprinklerRepository,
                                               SprinklerState sprinklerState, Features features) {
        return new GetRestrictionsLive(sprinklerRepository, sprinklerState, features);
    }

    @Provides
    @Singleton
    LogInDefault provideLogInDefault(SprinklerRepository sprinklerRepository) {
        return new LogInDefault(sprinklerRepository);
    }

    @Provides
    @Singleton
    CheckAuthenticationValid provideTestAuthenticationValid(SprinklerRepository
                                                                    sprinklerRepository) {
        return new CheckAuthenticationValid(sprinklerRepository);
    }

    @Provides
    @Singleton
    TriggerUpdateCheck provideTriggerUpdateCheck(SprinklerRepository sprinklerRepository) {
        return new TriggerUpdateCheck(sprinklerRepository);
    }

    @Provides
    @Singleton
    SendConfirmationEmail provideSendConfirmationEmail(CloudRepository cloudRepository,
                                                       GetMacAddress getMacAddress) {
        return new SendConfirmationEmail(cloudRepository, getMacAddress);
    }

    @Provides
    @Singleton
    ToggleRemoteAccess provideToggleRemoteAccess(SprinklerRepository sprinklerRepository) {
        return new ToggleRemoteAccess(sprinklerRepository);
    }

    @Provides
    @Singleton
    EnableRemoteAccessEmail provideEnableRemoteAccessEmail(SprinklerRepository sprinklerRepository,
                                                           ToggleRemoteAccess toggleRemoteAccess,
                                                           SendConfirmationEmail
                                                                   sendConfirmationEmail,
                                                           SprinklerState sprinklerState,
                                                           RemoteAccessAccountRepository
                                                                   remoteAccessAccountRepository) {
        return new EnableRemoteAccessEmail(sprinklerRepository, toggleRemoteAccess,
                sendConfirmationEmail, sprinklerState, remoteAccessAccountRepository);
    }

    @Provides
    @Singleton
    CreateRemoteAccessAccount provideCreateRemoteAccessAccount(SprinklerRepository
                                                                       sprinklerRepository,
                                                               RemoteAccessAccountRepository
                                                                       remoteAccessAccountRepository) {
        return new CreateRemoteAccessAccount(sprinklerRepository, remoteAccessAccountRepository);
    }

    @Provides
    @Singleton
    GetWateringDurationForZones provideGetWateringDurationForZones(SprinklerRepository
                                                                           sprinklerRepository,
                                                                   SchedulerProvider
                                                                           schedulerProvider) {
        return new GetWateringDurationForZones(sprinklerRepository, schedulerProvider);
    }

    @Provides
    @Singleton
    SaveWateringDuration provideSaveWateringDuration(SprinklerRepository sprinklerRepository,
                                                     SchedulerProvider schedulerProvider) {
        return new SaveWateringDuration(sprinklerRepository, schedulerProvider);
    }

    @Provides
    @Singleton
    SaveProgram provideUpdateProgram(SprinklerRepository sprinklerRepository,
                                     Features features, ProgramChangeNotifier programChangeNotifier,
                                     StatsNeedRefreshNotifier statsNeedRefreshNotifier) {
        return new SaveProgram(sprinklerRepository, features, programChangeNotifier,
                statsNeedRefreshNotifier);
    }

    // Notifiers

    @Provides
    @Singleton
    DeviceNameStore provideDeviceNameStore() {
        return new DeviceNameStore();
    }

    @Provides
    @Singleton
    StatsNeedRefreshNotifier provideStatsNeedRefreshNotifier() {
        return new StatsNeedRefreshNotifier();
    }

    @Provides
    @Singleton
    ProgramChangeNotifier provideProgramChangeNotifier() {
        return new ProgramChangeNotifier();
    }

    @Provides
    @Singleton
    ZonePropertiesChangeNotifier provideZonePropertiesChangeNotifier() {
        return new ZonePropertiesChangeNotifier();
    }

    @Provides
    @Singleton
    ManualStopAllWateringNotifier provideManualStopAllWateringNotifier() {
        return new ManualStopAllWateringNotifier();
    }
}
