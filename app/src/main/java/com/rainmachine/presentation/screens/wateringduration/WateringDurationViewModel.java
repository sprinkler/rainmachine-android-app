package com.rainmachine.presentation.screens.wateringduration;

import java.util.List;

class WateringDurationViewModel {
    boolean initialize;
    boolean isProgress;
    boolean isError;

    boolean isContent;
    List<SectionViewModel> sections;

    boolean showZoneDialog;
    ZoneViewModel zoneForDialog;

    private WateringDurationViewModel() {
    }

    static WateringDurationViewModel justInitialize() {
        WateringDurationViewModel viewModel = new WateringDurationViewModel();
        viewModel.initialize = true;
        return viewModel;
    }

    static WateringDurationViewModel progress() {
        WateringDurationViewModel viewModel = new WateringDurationViewModel();
        viewModel.isProgress = true;
        return viewModel;
    }

    static WateringDurationViewModel error() {
        WateringDurationViewModel viewModel = new WateringDurationViewModel();
        viewModel.isError = true;
        return viewModel;
    }

    static WateringDurationViewModel content(List<SectionViewModel> sections) {
        WateringDurationViewModel viewModel = new WateringDurationViewModel();
        viewModel.isContent = true;
        viewModel.sections = sections;
        return viewModel;
    }
}
