package com.rainmachine.domain.usecases.pushnotification;

import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.util.Timberific;
import com.rainmachine.domain.util.usecase.CompletableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class UpdatePushNotificationSettings extends
        CompletableUseCase<UpdatePushNotificationSettings.RequestModel> {

    private final PushNotificationRepository pushNotificationRepository;
    private final PrefRepository prefRepository;
    private final InfrastructureService infrastructureService;
    private final GetPreferencesForPushNotifications getPreferencesForPushNotifications;

    public UpdatePushNotificationSettings(PushNotificationRepository pushNotificationRepository,
                                          PrefRepository prefRepository, InfrastructureService
                                                  infrastructureService,
                                          GetPreferencesForPushNotifications
                                                  getPreferencesForPushNotifications) {
        this.pushNotificationRepository = pushNotificationRepository;
        this.prefRepository = prefRepository;
        this.infrastructureService = infrastructureService;
        this.getPreferencesForPushNotifications = getPreferencesForPushNotifications;
    }

    @NotNull
    @Override
    public Completable execute(final RequestModel requestModel) {
        return Observable
                .fromCallable(() -> {
                    String token = infrastructureService.getUpdatedPushToken();
                    prefRepository.savePushToken(token);
                    Timberific.i("Push token: " + token);
                    return token;
                })
                .concatMap(pushToken -> getPreferencesForPushNotifications.execute(null))
                .flatMapCompletable(responseModel -> pushNotificationRepository
                        .updatePushNotificationSettings(prefRepository.pushToken(),
                                infrastructureService.getSystemId(),
                                prefRepository.isGlobalPushNotificationsEnabled(),
                                responseModel.isUnitsMetric, responseModel.use24HourFormat))
                .doOnError(throwable -> {
                    Timberific.i("Keep trying to update the server in the background");
                    infrastructureService.scheduleUpdatePushNotificationsSettingsRetry();
                })
                .onErrorComplete();
    }

    public static class RequestModel {
    }
}
