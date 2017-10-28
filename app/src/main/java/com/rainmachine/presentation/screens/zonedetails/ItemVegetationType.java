package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;

public class ItemVegetationType extends ZoneDetailsAdvancedItem {

    public ZoneProperties.VegetationType vegetationType;

    public ItemVegetationType(ZoneProperties.VegetationType vegetationType) {
        this.vegetationType = vegetationType;
    }

    public ItemVegetationType(ZoneProperties.VegetationType vegetationType, String text, int icon) {
        this(vegetationType);
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
        ItemVegetationType item = (ItemVegetationType) o;
        return vegetationType == item.vegetationType;
    }

    @Override
    public int hashCode() {
        return vegetationType.hashCode();
    }
}
