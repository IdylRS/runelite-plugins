package com.survivalist;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum StatusEffect {
    HUNGRY("Hungry", -1, ItemID.COOKED_MEAT, false),
    STARVING("Starving", -2, ItemID.COOKED_MEAT, false),
    COLD("Cold", -1,  ItemID.ICE_DIAMOND, false),
    INJURED("Injured", -1, ItemID.BLOODY_KNIFE, false),
    OVERWEIGHT("Overweight", -1, ItemID.THIEVING_BAG, false),
    FULL("Full", 1, ItemID.COOKED_MEAT, false),
    WARM("Warm", 1, ItemID.FIRE_FEATHER, false),
    PIOUS("Pious", 1, ItemID.HOLY_SYMBOL, true),
    PROUD("Proud", 1, ItemID.GREEN_BALLOON, true);

    private String name;
    private int lpPerTick;
    private int itemIconID;
    private boolean showTicksRemaining;

    StatusEffect(String name, int lpPerTick, int itemIconID, boolean showTicksRemaining) {
        this.name = name;
        this.lpPerTick = lpPerTick;
        this.itemIconID = itemIconID;
        this.showTicksRemaining = showTicksRemaining;
    }
}
