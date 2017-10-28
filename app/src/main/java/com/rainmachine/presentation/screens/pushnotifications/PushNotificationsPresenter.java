package com.rainmachine.presentation.screens.pushnotifications;


import com.rainmachine.domain.model.PushNotification;
import com.rainmachine.domain.usecases.pushnotification.CheckPushNotificationsPossible;
import com.rainmachine.domain.usecases.pushnotification.GetAllPushNotifications;
import com.rainmachine.domain.usecases.pushnotification.TogglePushNotification;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

class PushNotificationsPresenter extends BasePresenter<PushNotificationsContract.View>
        implements PushNotificationsContract.Presenter {

    private final GetAllPushNotifications getAllPushNotifications;
    private final TogglePushNotification togglePushNotification;
    private final CheckPushNotificationsPossible checkPushNotificationsPossible;
    private CompositeDisposable disposables;

    PushNotificationsPresenter(GetAllPushNotifications getAllPushNotifications,
                               TogglePushNotification togglePushNotification,
                               CheckPushNotificationsPossible checkPushNotificationsPossible) {
        this.getAllPushNotifications = getAllPushNotifications;
        this.togglePushNotification = togglePushNotification;
        this.checkPushNotificationsPossible = checkPushNotificationsPossible;
        disposables = new CompositeDisposable();
    }

    @Override
    public void init() {
        refresh();
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    @Override
    public void onTogglePushNotification(PushNotification pushNotification, boolean enabled) {
        view.showProgress();
        disposables.add(togglePushNotification
                .execute(new TogglePushNotification.RequestModel(pushNotification, enabled))
                .andThen(refreshStream())
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    @Override
    public void onClickRetry() {
        refresh();
    }

    @Override
    public void onClickSetUpRemoteAccess() {
        view.goToRemoteAccessScreen();
    }

    private void refresh() {
        view.showProgress();
        Observable<PushNotificationsViewModel> stream = checkPushNotificationsPossible.execute(null)
                .concatMap(responseModel -> {
                    if (responseModel.isPossible) {
                        return refreshStream();
                    } else {
                        PushNotificationsViewModel viewModel = new PushNotificationsViewModel();
                        viewModel.showSections = false;
                        return Observable.just(viewModel);
                    }
                });
        disposables.add(stream
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new RefreshSubscriber()));
    }

    private Observable<PushNotificationsViewModel> refreshStream() {
        return getAllPushNotifications.execute(new GetAllPushNotifications.RequestModel())
                .map(responseValue -> {
                    PushNotificationsViewModel viewModel = new PushNotificationsViewModel();
                    viewModel.showSections = true;

                    List<PushNotification> pushNotifications = responseValue.pushNotifications;

                    List<SectionViewModel> sections = new ArrayList<>();
                    SectionViewModel section = new SectionViewModel();
                    section.type = SectionViewModel.Type.GLOBAL;
                    section.pushNotifications = new ArrayList<>();

                    boolean globalPushNotificationIsEnabled = false;
                    for (PushNotification pushNotification : pushNotifications) {
                        if (pushNotification.type == PushNotification.Type.GLOBAL) {
                            globalPushNotificationIsEnabled = pushNotification.enabled;
                            section.pushNotifications.add(map(pushNotification,
                                    globalPushNotificationIsEnabled));
                            break;
                        }
                    }

                    sections.add(section);
                    section = new SectionViewModel();
                    section.type = SectionViewModel.Type.AVAILABLE;
                    section.pushNotifications = new ArrayList<>();
                    for (PushNotification pushNotification : pushNotifications) {
                        if (pushNotification.type != PushNotification.Type.GLOBAL) {
                            section.pushNotifications.add(map(pushNotification,
                                    globalPushNotificationIsEnabled));
                        }
                    }
                    Collections.sort(section.pushNotifications, new
                            PushNotificationComparator());
                    sections.add(section);
                    viewModel.sections = sections;
                    return viewModel;
                });
    }

    private PushNotificationViewModel map(PushNotification pushNotification, boolean
            globalPushNotificationIsEnabled) {
        PushNotificationViewModel viewModel = new PushNotificationViewModel();
        viewModel.pushNotification = pushNotification;
        viewModel.showSettingActionable = pushNotification.type ==
                PushNotification.Type.GLOBAL || globalPushNotificationIsEnabled;
        return viewModel;
    }

    private static class PushNotificationComparator implements
            Comparator<PushNotificationViewModel> {
        @Override
        public int compare(PushNotificationViewModel lhs, PushNotificationViewModel rhs) {
            // The order is the order in which the enum values are declared
            return lhs.pushNotification.type.ordinal() - rhs.pushNotification.type.ordinal();
        }
    }

    private final class RefreshSubscriber extends DisposableObserver<PushNotificationsViewModel> {

        @Override
        public void onNext(PushNotificationsViewModel viewModel) {
            if (viewModel.showSections) {
                view.updateContent(viewModel.sections);
                view.showContent();
            } else {
                view.showRemoteAccessNeeded();
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            view.showError();
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
