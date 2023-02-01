package com.survivalist;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum StatusEffect {
    HUNGRY("Hungry", "", -1, ItemID.COOKED_MEAT, false),
    STARVING("Starving", "", -5, ItemID.COOKED_MEAT, false),
    COLD("Cold", "You feel cold.", -2,  ItemID.ICE_DIAMOND, false),
    INJURED("Injured", "You've gravely wounded.", -5, ItemID.BLOODY_KNIFE, false),
    OVERWEIGHT("Overweight", "You are carrying too much.", -3, ItemID.THIEVING_BAG, false),
    FULL("Full", "", 0, ItemID.COOKED_MEAT, false),
    WARM("Warm", "You feel warm.", 1, ItemID.FIRE_FEATHER, false),
    PIOUS("Pious", "The gods smile upon you.", 1, ItemID.HOLY_SYMBOL, true),
    PROUD("Proud", "You feel proud.", 1, ItemID.GREEN_BALLOON, true),
    RESTED("Rested", "You feel rested", 1, ItemID.CHILDS_BLANKET, true),
    EATING("Eating", "You are eating.", 0, ItemID.COOKED_MEAT, true);

    private String name;
    private String tooltip;
    private int lpPerTick;
    private int itemIconID;
    private boolean showTicksRemaining;

    StatusEffect(String name, String tooltip, int lpPerTick, int itemIconID, boolean showTicksRemaining) {
        this.name = name;
        this.tooltip = tooltip;
        this.lpPerTick = lpPerTick;
        this.itemIconID = itemIconID;
        this.showTicksRemaining = showTicksRemaining;
    }
}
