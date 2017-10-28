package com.rainmachine.domain.usecases.pushnotification;

import com.rainmachine.domain.boundary.data.PrefRepository;
import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.util.usecase.CompletableUseCase;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;

public class TogglePushNotification extends CompletableUseCase<TogglePushNotification
        .RequestModel> {

    private final PushNotificationRepository pushNotificationRepository;
    private final PrefRepository prefRepository;
    private final InfrastructureService infrastructureService;
    private final GetPreferencesForPushNotifications getPreferencesForPushNotifications;

    public TogglePushNotification(PushNotificationRepository pushNotificationRepository,
                                  PrefRepository prefRepository,
                                  InfrastructureService infrastructureService,
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
        return getPreferencesForPushNotifications.execute(null)
                .flatMapCompletable(responseModel -> pushNotificationRepository
                        .togglePushNotification(
                                requestModel.pushNotification, prefRepository.pushToken(),
                                requestModel.enable, infrastructureService.getSystemId(),
                                responseModel.isUnitsMetric, responseModel.use24HourFormat))
                .doOnComplete(() -> {
                    if (requestModel.pushNotification.type == PushNotification.Type.GLOBAL) {
                        prefRepository.saveGlobalPushNotificationsEnabled(requestModel.enable);
                    }
                });
    }

    public static class RequestModel {
        public PushNotification pushNotification;
        public boolean enable;

        public RequestModel(PushNotification pushNotification, boolean enable) {
            this.pushNotification = pushNotification;
            this.enable = enable;
        }
    }
}
