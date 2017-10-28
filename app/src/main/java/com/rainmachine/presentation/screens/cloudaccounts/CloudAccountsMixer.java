package com.rainmachine.presentation.screens.cloudaccounts;

import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.CloudInfo;
import com.rainmachine.data.remote.cloud.CloudSprinklersApiDelegate;
import com.rainmachine.domain.boundary.data.DeviceRepository;
import com.rainmachine.domain.model.CheckCloudAccountStatus;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class CloudAccountsMixer {

    private DatabaseRepositoryImpl databaseRepository;
    private CloudSprinklersApiDelegate cloudSprinklersApiDelegate;
    private DeviceRepository deviceRepository;

    CloudAccountsMixer(DatabaseRepositoryImpl databaseRepository,
                       CloudSprinklersApiDelegate cloudSprinklersApiDelegate,
                       DeviceRepository deviceRepository) {
        this.databaseRepository = databaseRepository;
        this.cloudSprinklersApiDelegate = cloudSprinklersApiDelegate;
        this.deviceRepository = deviceRepository;
    }

    Observable<CloudAccountsViewModel> refresh() {
        return Observable.fromCallable(() -> databaseRepository.getCloudInfoList())
                .map(cloudInfoList -> {
                    CloudAccountsViewModel viewModel = new CloudAccountsViewModel();
                    viewModel.cloudInfoList = cloudInfoList;
                    return viewModel;
                });
    }

    Observable<CheckCloudAccountViewModel> createCloudInfo(final CloudInfo
                                                                   initialCloudInfo) {
        Observable<CheckCloudAccountViewModel> observable = Observable
                .fromCallable(() -> {
                    CloudInfo cloudInfo = databaseRepository.getCloudInfo(initialCloudInfo.email);
                    if (cloudInfo == null) {
                        return CloudInfo.NOT_FOUND;
                    }
                    return cloudInfo;
                })
                .flatMap(cloudInfo -> {
                    if (cloudInfo != CloudInfo.NOT_FOUND)
                    // Already have cloud account for this email
                    {
                        return Observable.just(new CheckCloudAccountViewModel
                                (CheckCloudAccountStatus.ERROR_TYPE_ALREADY_EXISTING,
                                        cloudInfo));
                    } else {
                        return checkSaveCloudAccount(initialCloudInfo);
                    }
                });
        observable
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return observable;
    }

    Observable<CheckCloudAccountViewModel> updateCloudInfo(final CloudInfo
                                                                   initialCloudInfo) {
        Observable<CheckCloudAccountViewModel> observable = Observable
                .fromCallable(() -> {
                    CloudInfo cloudInfo = databaseRepository.getCloudInfo(initialCloudInfo.email);
                    if (cloudInfo == null) {
                        return CloudInfo.NOT_FOUND;
                    }
                    return cloudInfo;
                })
                .flatMap(cloudInfo -> {
                    if (cloudInfo != CloudInfo.NOT_FOUND && !cloudInfo._id.equals
                            (initialCloudInfo._id)) {
                        // Already have other cloud account for this email
                        return Observable.just(new CheckCloudAccountViewModel
                                (CheckCloudAccountStatus
                                        .ERROR_TYPE_ALREADY_EXISTING, cloudInfo));
                    } else {
                        return checkSaveCloudAccount(initialCloudInfo);
                    }
                });
        observable
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return observable;
    }

    Observable<CloudAccountsViewModel> removeCloudInfo(final CloudInfo cloudInfo) {
        Observable<CloudAccountsViewModel> observable = Observable
                .just(0)
                .doOnNext(integer -> {
                    databaseRepository.removeCloudInfo(cloudInfo._id);
                    deviceRepository.deleteCloudDevices(cloudInfo.email);
                })
                .flatMap(integer -> refresh());
        observable
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
        return observable;
    }

    private Observable<CheckCloudAccountViewModel> checkSaveCloudAccount(final CloudInfo
                                                                                 cloudInfo) {
        return cloudSprinklersApiDelegate
                .checkCloudAccount(cloudInfo)
                .flatMap(status -> Single.just(new CheckCloudAccountViewModel(status, cloudInfo)))
                .doOnSuccess(data -> {
                    if (data.status == CheckCloudAccountStatus.ERROR_TYPE_NO_DEVICE || data
                            .status == CheckCloudAccountStatus
                            .ERROR_TYPE_NOT_CONNECTED_INTERNET || data.status ==
                            CheckCloudAccountStatus.SUCCESS) {
                        databaseRepository.saveCloudInfo(cloudInfo);
                    }
                })
                .toObservable();
    }
}
