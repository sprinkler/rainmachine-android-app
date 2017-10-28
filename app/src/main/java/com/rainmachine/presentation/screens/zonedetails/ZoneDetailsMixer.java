package com.rainmachine.presentation.screens.zonedetails;

import com.pacoworks.rxtuples2.RxTuples;
import com.rainmachine.R;
import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.data.local.database.model.Device;
import com.rainmachine.data.local.database.model.DeviceSettings;
import com.rainmachine.data.local.database.model.ZoneSettings;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.LatLong;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.model.ZoneSimulation;
import com.rainmachine.domain.notifiers.ZonePropertiesChange;
import com.rainmachine.domain.notifiers.ZonePropertiesChangeNotifier;
import com.rainmachine.domain.usecases.GetMacAddress;
import com.rainmachine.domain.usecases.zoneimage.UploadZoneImage;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.MetricCalculator;
import com.rainmachine.domain.util.RunToCompletion;
import com.rainmachine.presentation.util.Toasts;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

class ZoneDetailsMixer {

    private Features features;
    private SprinklerRepositoryImpl sprinklerRepository;
    private Device device;
    private UploadZoneImage uploadZoneImage;
    private GetMacAddress getMacAddress;
    private ZonePropertiesChangeNotifier zonePropertiesChangeNotifier;

    ZoneDetailsMixer(Features features, SprinklerRepositoryImpl sprinklerRepository,
                     Device device, UploadZoneImage uploadZoneImage, GetMacAddress getMacAddress,
                     ZonePropertiesChangeNotifier zonePropertiesChangeNotifier) {
        this.features = features;
        this.sprinklerRepository = sprinklerRepository;
        this.device = device;
        this.uploadZoneImage = uploadZoneImage;
        this.getMacAddress = getMacAddress;
        this.zonePropertiesChangeNotifier = zonePropertiesChangeNotifier;
    }

    public Observable<ZoneDetailsViewModel> refresh(final long zoneId) {
        return Observable.combineLatest(
                features.useNewApi() ? sprinklerRepository.zonesProperties().toObservable() :
                        sprinklerRepository.zonesProperties3().toObservable(),
                sprinklerRepository.devicePreferences().toObservable(),
                sprinklerRepository.deviceSettings().toObservable(),
                getMacAddress.execute(new GetMacAddress.RequestModel(device.deviceId, device
                        .isManual())),
                (zonesProperties, devicePreferences, deviceSettings, macAddressResponse) ->
                        convertZoneProperties(zoneId, zonesProperties, devicePreferences,
                                deviceSettings, macAddressResponse.macAddress));
    }

    private ZoneDetailsViewModel convertZoneProperties(long zoneId, List<ZoneProperties>
            zonesProperties, DevicePreferences devicePreferences, DeviceSettings deviceSettings,
                                                       String macAddress) {
        ZoneDetailsViewModel viewModel = new ZoneDetailsViewModel();
        for (ZoneProperties zoneProperties : zonesProperties) {
            if (zoneId == zoneProperties.id) {
                viewModel.zoneProperties = zoneProperties;
                viewModel.zonePropertiesOriginal = zoneProperties.cloneIt();
                break;
            }
        }
        ZoneSettings zoneSettings = deviceSettings.zones.get(zoneId);
        // Create entry if first time and there is no setting for the zone
        if (zoneSettings == null) {
            viewModel.zoneSettings = new ZoneSettings(zoneId);
            viewModel.zoneSettingsOriginal = viewModel.zoneSettings.cloneIt();
        } else {
            viewModel.zoneSettings = zoneSettings;
            viewModel.zoneSettingsOriginal = zoneSettings.cloneIt();
        }

        viewModel.isUnitsMetric = devicePreferences.isUnitsMetric;
        viewModel.showEditImageActions = !device.isDemo();
        viewModel.deviceMacAddress = macAddress;
        return viewModel;
    }

    Observable<Irrelevant> saveZoneProperties(final ZoneProperties zoneProperties, final
    ZoneSettings zoneSettings, final boolean uploadImage) {
        Completable stream;
        if (features.useNewApi()) {
            if (zoneProperties.canBeMasterValve()) {
                boolean useMasterValve = zoneProperties.masterValve;
                stream = sprinklerRepository
                        .setProvisionMasterValve(useMasterValve).toObservable()
                        .flatMapCompletable(irrelevant -> sprinklerRepository.saveZoneProperties
                                (zoneProperties));
            } else {
                stream = sprinklerRepository.saveZoneProperties(zoneProperties);
            }
        } else {
            stream = sprinklerRepository.saveZonesProperties3(zoneProperties);
        }
        return stream
                .andThen(sprinklerRepository.saveZoneSettings(zoneSettings))
                .doOnSuccess(irrelevant -> {
                    if (uploadImage) {
                        Observable.combineLatest(
                                getMacAddress
                                        .execute(new GetMacAddress.RequestModel(device.deviceId,
                                                device.isManual()))
                                        .map(responseModel -> responseModel.macAddress),
                                sprinklerRepository.provision().toObservable()
                                        .map(provision -> new LatLong(provision.location.latitude,
                                                provision.location.longitude)),
                                RxTuples.toPair())
                                .concatMap(pair -> uploadZoneImage.execute(new
                                        UploadZoneImage.RequestModel(pair.getValue0(),
                                        zoneProperties.id, pair.getValue1())))
                                .blockingSubscribe();
                    }
                    zonePropertiesChangeNotifier.publish(new ZonePropertiesChange(zoneProperties.id,
                            zoneProperties.name, zoneProperties.enabled, zoneProperties
                            .masterValve));
                    Toasts.show(R.string.zone_details_success_save_zone);
                })
                .toObservable()
                .compose(RunToCompletion.instance());
    }

    Observable<ZoneSimulation> simulateZone(final ZoneProperties zoneProperties) {
        return Observable.combineLatest(
                sprinklerRepository.simulateZone(zoneProperties).toObservable(),
                sprinklerRepository.devicePreferences().toObservable(),
                (simulateZone, devicePreferences) -> {
                    if (!devicePreferences.isUnitsMetric) {
                        simulateZone.currentFieldCapacity = MetricCalculator.mmToInch(simulateZone
                                .currentFieldCapacity);
                    }
                    return simulateZone;
                });
    }
}
