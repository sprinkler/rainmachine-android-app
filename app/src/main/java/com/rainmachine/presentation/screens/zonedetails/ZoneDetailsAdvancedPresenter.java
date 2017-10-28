package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.model.ZoneSimulation;
import com.rainmachine.domain.util.Features;
import com.rainmachine.presentation.util.BasePresenter;
import com.rainmachine.presentation.util.GenericErrorDealer;
import com.rainmachine.presentation.util.RunOnProperThreads;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class ZoneDetailsAdvancedPresenter extends BasePresenter<ZoneDetailsAdvancedView> {

    private ZoneDetailsActivity activity;
    private Features features;
    private ZoneDetailsMixer mixer;

    private ZoneDetailsViewModel viewModel;
    private CompositeDisposable disposables;

    public ZoneDetailsAdvancedPresenter(ZoneDetailsActivity activity, Features features,
                                        ZoneDetailsMixer mixer) {
        this.activity = activity;
        this.features = features;
        this.mixer = mixer;
        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ZoneDetailsAdvancedView view) {
        super.attachView(view);
        view.setup(features.showAllAdvancedZoneSettings(), features.showExtraSoilTypes(),
                features.showOtherVegetationType());
    }

    @Override
    public void destroy() {
        disposables.clear();
    }

    boolean showAllAdvancedZoneSettings() {
        return features.showAllAdvancedZoneSettings();
    }

    void onSelectedItem(ZoneDetailsAdvancedItem item) {
        if (item instanceof ItemVegetationType) {
            viewModel.zoneProperties.vegetationType = ((ItemVegetationType) item).vegetationType;
            view.updateVegetationType(viewModel.zoneProperties.vegetationType,
                    features.showOtherVegetationType());
        }

        if (features.showAllAdvancedZoneSettings()) {
            if (item instanceof ItemVegetationType) {
                // Show custom logic only for devices that offer support for it
                if (viewModel.zoneProperties.vegetationType == ZoneProperties.VegetationType
                        .CUSTOM) {
                    view.showCustomVegetationView(viewModel.zoneProperties, viewModel
                            .isUnitsMetric);
                    activity.hideDefaultsMenuItem();
                }
            } else if (item instanceof ItemSoilType) {
                viewModel.zoneProperties.soilType = ((ItemSoilType) item).soilType;
                view.updateSoilType(viewModel.zoneProperties.soilType);

                if (viewModel.zoneProperties.soilType == ZoneProperties.SoilType.CUSTOM) {
                    view.showCustomSoilView(viewModel.zoneProperties, viewModel.isUnitsMetric);
                    activity.hideDefaultsMenuItem();
                }
            } else if (item instanceof ItemSprinklerHeads) {
                viewModel.zoneProperties.sprinklerHeads = ((ItemSprinklerHeads) item)
                        .sprinklerHeads;
                view.updateSprinklerHeads(viewModel.zoneProperties.sprinklerHeads);

                if (viewModel.zoneProperties.sprinklerHeads == ZoneProperties.SprinklerHeads
                        .CUSTOM) {
                    view.showCustomSprinklerHeadView(viewModel.zoneProperties, viewModel
                            .isUnitsMetric);
                    activity.hideDefaultsMenuItem();
                }
            } else if (item instanceof ItemSlope) {
                viewModel.zoneProperties.slope = ((ItemSlope) item).slope;
                view.updateSlope(viewModel.zoneProperties.slope);

                if (viewModel.zoneProperties.slope == ZoneProperties.Slope.CUSTOM) {
                    view.showCustomSlopeView(viewModel.zoneProperties, viewModel.isUnitsMetric);
                    activity.hideDefaultsMenuItem();
                }
            } else if (item instanceof ItemExposure) {
                viewModel.zoneProperties.exposure = ((ItemExposure) item).exposure;
                view.updateExposure(viewModel.zoneProperties.exposure);
            }

            simulate();
        }
    }

    void onClickFieldCapacity() {
        view.showFieldCapacityDialog(viewModel.zoneProperties, viewModel.isUnitsMetric);
    }

    void onSaveFieldCapacity(int savingsPercentage) {
        viewModel.zoneProperties.savingsPercentage = savingsPercentage;
        view.updateFieldCapacity(viewModel.zoneProperties, viewModel.isUnitsMetric);
    }

    void onSaveExitCustomSlopeView(float surfaceAccumulation) {
        viewModel.zoneProperties.setAllowedSurfaceAcc(surfaceAccumulation, viewModel.isUnitsMetric);
        view.showStandardView();
        simulate();
    }

    void onSaveExitCustomSoilView(float intakeRate, float soilFieldCapacity) {
        viewModel.zoneProperties.setSoilIntakeRate(intakeRate, viewModel.isUnitsMetric);
        viewModel.zoneProperties.fieldCapacity = soilFieldCapacity;
        view.showStandardView();
        simulate();
    }

    void onSaveExitCustomSprinklerHeadView(float precipitationRate, float applicationEfficiency) {
        viewModel.zoneProperties.setPrecipitationRate(precipitationRate, viewModel.isUnitsMetric);
        viewModel.zoneProperties.appEfficiency = applicationEfficiency;
        view.showStandardView();
        simulate();
    }

    void onSaveExitCustomVegetationView(float allowedDepletion, float rootDepth, float
            wilting, boolean isTallPlant, float etCoefficient, boolean isSingleYearValue) {
        viewModel.zoneProperties.maxAllowedDepletion = allowedDepletion;
        viewModel.zoneProperties.setRootDepth(rootDepth, viewModel.isUnitsMetric);
        viewModel.zoneProperties.permWilting = wilting;
        viewModel.zoneProperties.isTallPlant = isTallPlant;
        if (isSingleYearValue) {
            viewModel.zoneProperties.etCoefficient = etCoefficient;
        }
        view.showStandardView();
        simulate();
    }

    void onSaveExitCustomVegetationCropCoefficientView(List<Float> values) {
        viewModel.zoneProperties.detailedMonthsKc = values;
        view.showVegetationView();
    }

    void onClickMonthlyKc() {
        viewModel.zoneProperties.useYearlyKc = false;
        view.showCustomVegetationCropCoefficientView(viewModel.zoneProperties);
    }

    void onClickYearlyKc() {
        viewModel.zoneProperties.useYearlyKc = true;
    }

    void onClickDefaultsAdvanced() {
        viewModel.zoneProperties.soilType = ZoneProperties.SoilType.CLAY_LOAM;
        view.updateSoilType(viewModel.zoneProperties.soilType);
        viewModel.zoneProperties.vegetationType = ZoneProperties.VegetationType.LAWN;
        view.updateVegetationType(viewModel.zoneProperties.vegetationType, features
                .showOtherVegetationType());
        viewModel.zoneProperties.sprinklerHeads = ZoneProperties.SprinklerHeads.POPUP_SPRAY;
        view.updateSprinklerHeads(viewModel.zoneProperties.sprinklerHeads);
        viewModel.zoneProperties.slope = ZoneProperties.Slope.FLAT;
        view.updateSlope(viewModel.zoneProperties.slope);
        viewModel.zoneProperties.exposure = ZoneProperties.Exposure.FULL_SUN;
        view.updateExposure(viewModel.zoneProperties.exposure);
        simulate();
    }

    private void simulate() {
        disposables.add(mixer.simulateZone(viewModel.zoneProperties)
                .doOnError(GenericErrorDealer.INSTANCE)
                .compose(RunOnProperThreads.instance())
                .subscribeWith(new SimulateZoneSubscriber()));
    }

    void updateViewModel(ZoneDetailsViewModel viewModel) {
        this.viewModel = viewModel;
        view.render(viewModel.zoneProperties, features.showAllAdvancedZoneSettings(), viewModel
                .isUnitsMetric, features.showOtherVegetationType());
    }

    void onExtendedVegetationType() {
        if (features.showAllAdvancedZoneSettings()) {
            view.collapseSoilType();
            view.collapseSlope();
            view.collapseSprinklerHeads();
            view.collapseExposure();
        }
    }

    void onSaveExitOptional(float area, float flowRate) {
        viewModel.zoneProperties.setArea(area, viewModel.isUnitsMetric);
        viewModel.zoneProperties.setFlowRate(flowRate, viewModel.isUnitsMetric);
        activity.showMainView();
    }

    void onExitAdvanced() {
        activity.showMainView();
    }

    private final class SimulateZoneSubscriber extends DisposableObserver<ZoneSimulation> {

        @Override
        public void onNext(ZoneSimulation simulateZone) {
            viewModel.zoneProperties.setCurrentFieldCapacity(simulateZone.currentFieldCapacity,
                    viewModel.isUnitsMetric);
            viewModel.zoneProperties.referenceTime = simulateZone.referenceTime;
            view.updateSimulationFields(viewModel.zoneProperties, viewModel.isUnitsMetric);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            // Do nothing
        }

        @Override
        public void onComplete() {
            // Do nothing
        }
    }
}
