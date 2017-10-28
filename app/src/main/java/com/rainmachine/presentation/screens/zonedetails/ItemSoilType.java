package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;

public class ItemSoilType extends ZoneDetailsAdvancedItem {

    public ZoneProperties.SoilType soilType;

    public ItemSoilType(ZoneProperties.SoilType soilType) {
        this.soilType = soilType;
    }

    public ItemSoilType(ZoneProperties.SoilType soilType, String text, int icon) {
        this(soilType);
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
        ItemSoilType item = (ItemSoilType) o;
        return soilType == item.soilType;
    }

    @Override
    public int hashCode() {
        return soilType.hashCode();
    }
}
