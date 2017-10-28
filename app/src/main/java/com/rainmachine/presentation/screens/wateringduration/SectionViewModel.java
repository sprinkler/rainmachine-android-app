package com.rainmachine.presentation.screens.wateringduration;

import java.util.List;

class SectionViewModel {
    Type type;
    List<ZoneViewModel> zones;

    SectionViewModel(Type type, List<ZoneViewModel> zones) {
        this.type = type;
        this.zones = zones;
    }

    enum Type {ACTIVE, INACTIVE}
}
