package com.rainmachine.presentation.screens.rainsensor;

import com.rainmachine.domain.model.Provision;

import java.util.List;

class RainSensorViewModel {
    boolean useRainSensor;
    boolean rainSensorNormallyClosed;
    Provision.RainSensorLastEvent rainSensorLastEvent;
    ItemRainOption rainDetectedOption;
    List<ItemRainOption> options;
    boolean use24HourFormat;
    boolean showExtraFields;
}
