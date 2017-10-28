package com.rainmachine.presentation.screens.zonedetails;

import com.rainmachine.domain.model.ZoneProperties;

public class ItemSprinklerHeads extends ZoneDetailsAdvancedItem {

    public ZoneProperties.SprinklerHeads sprinklerHeads;

    public ItemSprinklerHeads(ZoneProperties.SprinklerHeads sprinklerHeads) {
        this.sprinklerHeads = sprinklerHeads;
    }

    public ItemSprinklerHeads(ZoneProperties.SprinklerHeads sprinklerHeads, String text, int icon) {
        this(sprinklerHeads);
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
        ItemSprinklerHeads item = (ItemSprinklerHeads) o;
        return sprinklerHeads == item.sprinklerHeads;
    }

    @Override
    public int hashCode() {
        return sprinklerHeads.hashCode();
    }
}
