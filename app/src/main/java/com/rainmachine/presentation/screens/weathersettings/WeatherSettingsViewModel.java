package com.rainmachine.presentation.screens.weathersettings;

import com.rainmachine.presentation.screens.weathersources.WeatherSource;

import java.util.List;

class WeatherSettingsViewModel {
    List<WeatherSource> enabledSources;
    WeatherSource defaultSource;
    int numDisabledSources;
    boolean isRainSensitivityChanged;
    float rainSensitivity;
    boolean isFieldCapacityChanged;
    int fieldCapacity;
    boolean isWindSensitivityChanged;
    float windSensitivity;
}
