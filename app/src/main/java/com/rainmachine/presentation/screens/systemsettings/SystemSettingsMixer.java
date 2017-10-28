package com.rainmachine.presentation.screens.systemsettings;

import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.DatabaseRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.domain.model.CloudSettings;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.notifiers.DeviceNameStore;
import com.rainmachine.domain.notifiers.StatsNeedRefreshNotifier;
import com.rainmachine.domain.usecases.pushnotification.UpdatePushNotificationSettings;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.domain.util.SprinklerState;
import com.rainmachine.infrastructure.Sleeper;
import com.rainmachine.infrastructure.SprinklerUtils;
import com.rainmachine.presentation.util.CustomDataException;
import com.rainmachine.presentation.util.Toasts;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class SystemSettingsMixer {

    private static final int WAIT_AFTER_RESET_DEFAULT_MILLIS = 30 * DateTimeConstants
            .MILLIS_PER_SECOND;

    private Features features;
    private SprinklerUtils sprinklerUtils;
    private Device device;
    private DatabaseRepositoryImpl databaseRepository;
    private StatsNeedRefreshNotifier statsNeedRefreshNotifier;
    private SprinklerState sprinklerState;
    private SprinklerRepositoryImpl sprinklerRepository;
    private DeviceNameStore deviceNameStore;
    private UpdatePushNotificationSettings updatePushNotificationSettings;

    SystemSettingsMixer(Device device, SprinklerUtils sprinklerUtils,
                        SprinklerState sprinklerState, Features features,
                        DatabaseRepositoryImpl databaseRepository, StatsNeedRefreshNotifier
                                statsNeedRefreshNotifier, SprinklerRepositoryImpl
                                sprinklerRepository, DeviceNameStore
                                deviceNameStore, UpdatePushNotificationSettings
                                updatePushNotificationSettings) {
        this.features = features;
        this.sprinklerUtils = sprinklerUtils;
        this.device = device;
        this.databaseRepository = databaseRepository;
        this.statsNeedRefreshNotifier = statsNeedRefreshNotifier;
        this.sprinklerState = sprinklerState;
        this.sprinklerRepository = sprinklerRepository;
        this.deviceNameStore = deviceNameStore;
        this.updatePushNotificationSettings = updatePushNotificationSettings;
    }

    public Observable<SystemSettingsViewModel> refresh() {
        Single<SystemSettingsViewModel> stream;
        if (features.useNewApi()) {
            stream = deviceSettings();
        } else {
            stream = deviceSettings3();
        }
        return stream.toObservable();
    }

    private Single<SystemSettingsViewModel> deviceSettings() {
        return Single.zip(
                sprinklerRepository.devicePreferences(),
                sprinklerRepository.timeDate(),
                sprinklerRepository
                        .deviceName()
                        .doOnSuccess(deviceName -> {
                            device.name = deviceName;
                            deviceNameStore.publish(device.name);
                        }),
                sprinklerRepository.provision(),
                sprinklerRepository.cloudSettings(),
                (devicePreferences, sprinklerLocalDateTime, deviceName, provision, cloudSettings) ->
                        buildViewModel(sprinklerLocalDateTime, devicePreferences, deviceName,
                                provision, cloudSettings));
    }

    private Single<SystemSettingsViewModel> deviceSettings3() {
        return Single.zip(
                sprinklerRepository.units3(),
                sprinklerRepository.timeDate3(),
                (isUnitsMetric, timeDate) -> {
                    SystemSettingsViewModel deviceSettings = new SystemSettingsViewModel();
                    deviceSettings.isUnitsMetric = isUnitsMetric;
                    deviceSettings.sprinklerLocalDateTime = timeDate.sprinklerLocalDateTime;
                    deviceSettings.use24HourFormat = timeDate.use24HourFormat;
                    return deviceSettings;
                });
    }

    private SystemSettingsViewModel buildViewModel(LocalDateTime sprinklerLocalDateTime,
                                                   DevicePreferences devicePreferences,
                                                   String deviceName, Provision provision,
                                                   CloudSettings cloudSettings) {
        SystemSettingsViewModel deviceSettings = new SystemSettingsViewModel();
        deviceSettings.isUnitsMetric = devicePreferences.isUnitsMetric;
        deviceSettings.sprinklerLocalDateTime = sprinklerLocalDateTime;
        deviceSettings.use24HourFormat = devicePreferences.use24HourFormat;
        deviceSettings.deviceName = deviceName;
        deviceSettings.timezone = provision.location.timezone;
        deviceSettings.cloudSettings = cloudSettings;
        deviceSettings.address = provision.location.name;
        deviceSettings.enabledWifiSettings = !device.isCloud() && !features.isSpk3();
        return deviceSettings;
    }

    Observable<SystemSettingsViewModel> saveUnits(final boolean isUnitsMetric) {
        Observable<Irrelevant> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository
                    .saveUnits(isUnitsMetric).toObservable()
                    .doOnNext(irrelevant -> updatePushNotificationSettings());
        } else {
            observable = sprinklerRepository.saveUnits3(isUnitsMetric).toObservable();
        }
        return observable
                .doOnNext(irrelevant -> statsNeedRefreshNotifier.publish(new Object()))
                .flatMap(irrelevant -> refresh())
                .compose(RunToCompletion.instance());
    }

    Observable<SystemSettingsViewModel> saveTimeDate(SystemSettingsViewModel deviceSettings) {
        Observable<Irrelevant> observable;
        if (features.useNewApi()) {
            observable = sprinklerRepository
                    .saveTimeDate(deviceSettings.sprinklerLocalDateTime.toLocalDate(),
                            deviceSettings.sprinklerLocalDateTime.toLocalTime()).toObservable();
        } else {
            observable = sprinklerRepository.saveTimeDate3(deviceSettings.sprinklerLocalDateTime,
                    deviceSettings.use24HourFormat).toObservable();
        }
        return observable
                .flatMap(irrelevant -> refresh())
                .compose(RunToCompletion.instance());
    }

    Observable<SystemSettingsViewModel> saveDeviceName(final String deviceName) {
        return sprinklerRepository
                .saveDeviceName(deviceName).toObservable()
                .doOnNext(irrelevant -> {
                    device.name = deviceName;
                    deviceNameStore.publish(device.name);
                })
                .flatMap(irrelevant -> refresh())
                .compose(RunToCompletion.instance());
    }

    Observable<SystemSettingsViewModel> saveTimezone(final String timezone) {
        return sprinklerRepository
                .saveTimezone(timezone).toObservable()
                .flatMap(irrelevant -> refresh())
                .compose(RunToCompletion.instance());
    }

    Observable<String> generateSupportPin() {
        return sprinklerRepository
                .generateSupportPin().toObservable();
    }

    Observable<Irrelevant> resetToDefault() {
        return sprinklerRepository
                .resetProvision().toObservable()
                .doOnSubscribe(disposable -> setRefreshersBlocked(true))
                .doOnNext(irrelevant -> {
                    // The password is reset so we remove old authentication
                    sprinklerUtils.logout();
                    // Remove sprinkler from list and let UDP work in rediscovering after reboot
                    databaseRepository.removeDevice(device._id);

                    Toasts.show(R.string.system_settings_success_reset_defaults);

                    Sleeper.sleep(WAIT_AFTER_RESET_DEFAULT_MILLIS);
                })
                .doAfterTerminate(() -> setRefreshersBlocked(false))
                .compose(RunToCompletion.instance());
    }

    private void setRefreshersBlocked(boolean isBlocked) {
        sprinklerState.setRefreshersBlocked(isBlocked);
    }

    void saveUse24HourFormat(final boolean use24HourFormat) {
        Completable
                .fromAction(() -> {
                    databaseRepository.updateSprinklerSettings24HourFormat(device, use24HourFormat);
                })
                .doOnComplete(() -> updatePushNotificationSettings())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public Observable<SystemSettingsViewModel> reboot() {
        return sprinklerRepository
                .reboot().toObservable()
                .doOnSubscribe(disposable -> setRefreshersBlocked(true))
                .flatMap(irrelevant -> Observable
                        .interval(2, 7, TimeUnit.SECONDS)
                        .take(20)
                        .switchMap(aLong -> sprinklerRepository.testApiFunctional().toObservable())
                        .filter(isSuccess -> isSuccess)
                        .firstOrError()
                        .toObservable())
                .onErrorReturn(throwable -> {
                    throw new CustomDataException(CustomDataException.CustomStatus
                            .REBOOT_ERROR);
                })
                .flatMap(ignored -> refresh())
                .doAfterTerminate(() -> setRefreshersBlocked(false))
                .compose(RunToCompletion.instance());
    }

    private void updatePushNotificationSettings() {
        updatePushNotificationSettings.execute(new UpdatePushNotificationSettings.RequestModel())
                .subscribe();
    }
}
