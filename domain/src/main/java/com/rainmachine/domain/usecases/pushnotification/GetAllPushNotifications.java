package com.rainmachine.domain.usecases.pushnotification;


import com.rainmachine.domain.boundary.data.PushNotificationRepository;
import com.rainmachine.domain.boundary.infrastructure.InfrastructureService;
import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.util.usecase.ObservableUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;

public class GetAllPushNotifications extends ObservableUseCase<GetAllPushNotifications.RequestModel,
        GetAllPushNotifications.ResponseModel> {

    private final PushNotificationRepository pushNotificationRepository;
    private final InfrastructureService infrastructureService;

    public GetAllPushNotifications(PushNotificationRepository pushNotificationRepository,
                                   InfrastructureService infrastructureService) {
        this.pushNotificationRepository = pushNotificationRepository;
        this.infrastructureService = infrastructureService;
    }

    @NotNull
    @Override
    public Observable<ResponseModel> execute(RequestModel requestModel) {
        return pushNotificationRepository.getPushNotifications(infrastructureService.getSystemId())
                .map(pushNotifications -> {
                    Iterator<PushNotification> it = pushNotifications.iterator();
                    while (it.hasNext()) {
                        PushNotification notification = it.next();
                        if (notification.type == PushNotification.Type.REMOTE_ACCESS ||
                                notification.type == PushNotification.Type.TEXT_MODE) {
                            it.remove();
                        }
                    }
                    return new ResponseModel(pushNotifications);
                })
                .toObservable();
    }

    public static class RequestModel {
    }

    public static class ResponseModel {
        public List<PushNotification> pushNotifications;

        ResponseModel(List<PushNotification> pushNotifications) {
            this.pushNotifications = pushNotifications;
        }
    }
}
