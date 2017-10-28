package com.rainmachine.presentation.screens.mini8settings;

import java.util.List;

class Mini8SettingsViewModel {
    boolean touchAdvanced;
    boolean showRestrictionsOnLed;
    int minLedBrightness;
    int maxLedBrightness;
    int touchSleepTimeout;
    int touchLongPressTimeout;
    List<TouchProgramViewModel> programs;
    TouchProgramViewModel touchProgramToRun;
}
