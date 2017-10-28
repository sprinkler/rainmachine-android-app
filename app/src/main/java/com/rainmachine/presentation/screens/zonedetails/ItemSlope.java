package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;

public class ItemSlope extends ZoneDetailsAdvancedItem {

    public ZoneProperties.Slope slope;

    public ItemSlope(ZoneProperties.Slope slope) {
        this.slope = slope;
    }

    public ItemSlope(ZoneProperties.Slope slope, String text, int icon) {
        this(slope);
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
        ItemSlope item = (ItemSlope) o;
        return slope == item.slope;
    }

    @Override
    public int hashCode() {
        return slope.hashCode();
    }
}
