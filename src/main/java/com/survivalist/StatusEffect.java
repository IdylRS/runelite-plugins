package com.survivalist;

import net.runelite.api.ItemID;

public enum StatusEffect {
    HUNGRY("Hungry", -1, ItemID.COOKED_MEAT),
    STARVING("Starving", -2, ItemID.COOKED_MEAT),
    COLD("Cold", -1,  ItemID.ICE_DIAMOND),
    INJURED("Injured", -1, ItemID.BLOODY_KNIFE),
    OVERWEIGHT("Overweight", -1, ItemID.THIEVING_BAG),
    FULL("Full", 1, ItemID.COOKED_MEAT),
    WARM("Warm", 1, ItemID.FIRE_FEATHER),
    PIOUS("Pious", 1, ItemID.HOLY_SYMBOL),
    PROUD("Proud", 1, ItemID.GREEN_BALLOON);

    private String name;
    private int lpPerTick;
    private int itemIconID;

    StatusEffect(String name, int lpPerTick, int itemIconID) {
        this.name = name;
        this.lpPerTick = lpPerTick;
        this.itemIconID = itemIconID;
    }
}
