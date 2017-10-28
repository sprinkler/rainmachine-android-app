package com.rainmachine.injection;

import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.boundary.data.HandPreferenceRepository;
import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.boundary.data.ZoneImageRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.notifiers.HandPreferenceNotifier;
import com.rainmachine.domain.usecases.handpreference.GetHandPreference;
import com.rainmachine.domain.usecases.handpreference.SaveHandPreference;
import com.rainmachine.domain.usecases.pushnotification.GetAllPushNotifications;
import com.rainmachine.domain.usecases.pushnotification.GetPreferencesForPushNotifications;
import com.rainmachine.domain.usecases.pushnotification.TogglePushNotification;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.domain.usecases.zoneimage.DeleteZoneImage;
import com.rainmachine.domain.usecases.zoneimage.UploadZoneImage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class DomainModule {

    @Provides
    @Singleton
    TogglePushNotification provideTogglePushNotification(PushNotificationRepository
                                                                 pushNotificationRepository,
                                                         PrefRepository prefRepository,
                                                         InfrastructureService
                                                                 infrastructureService,
                                                         GetPreferencesForPushNotifications
                                                                 getPreferencesForPushNotifications) {
        return new TogglePushNotification(pushNotificationRepository, prefRepository,
                infrastructureService, getPreferencesForPushNotifications);
    }

    @Provides
    @Singleton
    GetAllPushNotifications provideGetAllPushNotifications(PushNotificationRepository
                                                                   pushNotificationRepository,
                                                           InfrastructureService
                                                                   infrastructureService) {
        return new GetAllPushNotifications(pushNotificationRepository, infrastructureService);
    }

    @Provides
    @Singleton
    UpdatePushNotificationSettings provideUpdatePushNotificationsSettings
            (PushNotificationRepository pushNotificationRepository, PrefRepository
                    prefRepository, InfrastructureService infrastructureService,
             GetPreferencesForPushNotifications getPreferencesForPushNotifications) {
        return new UpdatePushNotificationSettings(pushNotificationRepository, prefRepository,
                infrastructureService, getPreferencesForPushNotifications);
    }

    @Provides
    @Singleton
    GetPreferencesForPushNotifications provideGetPreferencesForPushNotifications(DeviceRepository
                                                                                         deviceRepository, PrefRepository prefRepository) {
        return new GetPreferencesForPushNotifications(deviceRepository, prefRepository);
    }

    @Provides
    @Singleton
    DeleteZoneImage provideDeleteZoneImage(DeviceRepository deviceRepository,
                                           InfrastructureService infrastructureService,
                                           ZoneImageRepository zoneImageRepository) {
        return new DeleteZoneImage(infrastructureService, zoneImageRepository);
    }

    @Provides
    @Singleton
    UploadZoneImage provideUploadZoneImage(DeviceRepository deviceRepository,
                                           InfrastructureService infrastructureService,
                                           ZoneImageRepository zoneImageRepository) {
        return new UploadZoneImage(infrastructureService, zoneImageRepository);
    }

    @Provides
    @Singleton
    GetHandPreference provideGetHandPreference(HandPreferenceRepository
                                                       handPreferenceRepository) {
        return new GetHandPreference(handPreferenceRepository);
    }

    @Provides
    @Singleton
    SaveHandPreference provideSaveHandPreference(HandPreferenceRepository
                                                         handPreferenceRepository) {
        return new SaveHandPreference(handPreferenceRepository);
    }

    // Notifiers

    @Provides
    @Singleton
    HandPreferenceNotifier provideHandPreferenceNotifier() {
        return new HandPreferenceNotifier();
    }
}
