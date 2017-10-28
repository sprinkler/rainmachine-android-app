package com.rainmachine.presentation.screens.rainsensor;

import com.rainmachine.data.boundary.SprinklerRepositoryImpl;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.usecases.restriction.GetRestrictionsLive;
import com.rainmachine.domain.util.Features;
import com.rainmachine.domain.util.Irrelevant;
import com.rainmachine.domain.util.RunToCompletion;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

class RainSensorMixer {

    private SprinklerRepositoryImpl sprinklerRepository;
    private GetRestrictionsLive getRestrictionsLive;
    private Features features;

    RainSensorMixer(SprinklerRepositoryImpl sprinklerRepository,
                    GetRestrictionsLive getRestrictionsLive, Features features) {
        this.sprinklerRepository = sprinklerRepository;
        this.getRestrictionsLive = getRestrictionsLive;
        this.features = features;
    }

    Observable<RainSensorViewModel> refresh() {
        return Observable.combineLatest(
                sprinklerRepository.provision().toObservable(),
                sprinklerRepository.devicePreferences().toObservable(),
                ((provision, devicePreferences) -> buildViewModel(provision, devicePreferences)));
    }

    private RainSensorViewModel buildViewModel(Provision provision, DevicePreferences
            devicePreferences) {
        RainSensorViewModel viewModel = new RainSensorViewModel();
        viewModel.useRainSensor = provision.system.useRainSensor;
        viewModel.rainSensorNormallyClosed = provision.system.rainSensorNormallyClosed;
        viewModel.rainSensorLastEvent = provision.system.rainSensorLastEvent;
        viewModel.options = options();
        Provision.RainSensorSnoozeDuration selectedOption = provision.system
                .rainSensorSnoozeDuration;
        for (ItemRainOption option : viewModel.options) {
            if (option.snoozeDuration == selectedOption) {
                viewModel.rainDetectedOption = option;
                break;
            }
        }
        viewModel.use24HourFormat = devicePreferences.use24HourFormat;
        viewModel.showExtraFields = features.showExtraRainSensorFields();
        return viewModel;
    }

    private List<ItemRainOption> options() {
        List<ItemRainOption> items = new ArrayList<>();
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.RESUME, 0));
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.UNTIL_MIDNIGHT, 1));
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.SNOOZE_6_HOURS, 2));
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.SNOOZE_12_HOURS, 3));
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.SNOOZE_24_HOURS, 4));
        items.add(new ItemRainOption(Provision.RainSensorSnoozeDuration.SNOOZE_48_HOURS, 5));
        return items;
    }

    Observable<Irrelevant> saveRainSensor(final boolean enabled) {
        return sprinklerRepository
                .saveRainSensor(enabled).toObservable()
                .doOnNext(irrelevant -> getRestrictionsLive.forceRefresh())
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveRainSensorNormallyClosed(boolean isEnabled) {
        return sprinklerRepository
                .saveRainSensorNormallyClosed(isEnabled).toObservable()
                .doOnNext(irrelevant -> getRestrictionsLive.forceRefresh())
                .compose(RunToCompletion.instance());
    }

    Observable<Irrelevant> saveRainSensorSnoozeDuration(Provision.RainSensorSnoozeDuration
                                                                snoozeDuration) {
        return sprinklerRepository
                .saveRainSensorSnoozeDuration(snoozeDuration).toObservable()
                .doOnNext(irrelevant -> getRestrictionsLive.forceRefresh())
                .compose(RunToCompletion.instance());
    }
}