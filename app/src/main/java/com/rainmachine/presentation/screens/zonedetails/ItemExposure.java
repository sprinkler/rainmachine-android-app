package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;

public class ItemExposure extends ZoneDetailsAdvancedItem {

    public ZoneProperties.Exposure exposure;

    public ItemExposure(ZoneProperties.Exposure exposure) {
        this.exposure = exposure;
    }

    public ItemExposure(ZoneProperties.Exposure exposure, String text, int icon) {
        this(exposure);
        this.text = text;
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemExposure item = (ItemExposure) o;
        return exposure == item.exposure;
    }

    @Override
    public int hashCode() {
        return exposure.hashCode();
    }
}
