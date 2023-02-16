package com.survivalist;

import net.runelite.api.ItemID;

public enum LightSource {
    TORCH(ItemID.LIT_TORCH, 1),
    CANDLE(ItemID.LIT_CANDLE, 1),
    CANDLE_LANTERN(ItemID.CANDLE_LANTERN_4531, 1),
    BLACK_CANDLE_LANTERN(ItemID.CANDLE_LANTERN_4534, 1),
    BLACK_CANDLE(ItemID.LIT_BLACK_CANDLE, 1),
    OIL_LAMP(ItemID.OIL_LAMP_4524, 1.5),
    OIL_LANTERN(ItemID.OIL_LANTERN_4539, 1.5),
    MINING_HELMET(ItemID.MINING_HELMET_5014, 1.5),
    KANDARIN_HEADGEAR_1(ItemID.KANDARIN_HEADGEAR_1, 1),
    KANDARIN_HEADGEAR_2(ItemID.KANDARIN_HEADGEAR_2, 1.5),
    KANDARIN_HEADGEAR_3(ItemID.KANDARIN_HEADGEAR_3, 1.5),
    KANDARIN_HEADGEAR_4(ItemID.KANDARIN_HEADGEAR_4, 2),
    BULLSEYE_LANTERN(ItemID.BULLSEYE_LANTERN_4550, 2),
    SAPPHIRE_LANTERN(ItemID.SAPPHIRE_LANTERN_4702, 2),
    EMERALD_LANTERN(ItemID.EMERALD_LANTERN_9065, 2),
    FIREMAKING_CAPE(ItemID.FIREMAKING_CAPE, 2),
    BRUMA_TORCH(ItemID.BRUMA_TORCH, 2);


    public int itemID;
    public double brightnessFactor;

    LightSource(int itemID, double brightnessFactor) {
        this.itemID = itemID;
        this.brightnessFactor = brightnessFactor;
    }
}
