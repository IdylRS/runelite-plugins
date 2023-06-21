package com.example;

import lombok.Getter;
import net.runelite.api.Prayer;

@Getter
public enum Prayers {
    THICK_SKIN(Prayer.THICK_SKIN, "Thick Skin", 35454981, 115, 135),
    BURST_OF_STRENGTH(Prayer.BURST_OF_STRENGTH, "Burst of Strength", 35454982,  116, 136),
    CLARITY_OF_THOUGH(Prayer.CLARITY_OF_THOUGHT, "Clarity of Thought", 35454983,  117, 137),
    SHARP_EYE(Prayer.SHARP_EYE, "Sharp Eye",  35454999, 133, 153),
    MYSTIC_WILL(Prayer.MYSTIC_WILL, "Mystic Will", 35455000, 134, 154),
    ROCK_SKIN(Prayer.ROCK_SKIN,  "Rock Skin",35454984, 118, 138),
    SUPERHUMAN_STRENGTH(Prayer.SUPERHUMAN_STRENGTH, "Superhuman Strength", 35454985,  119, 139),
    IMPROVED_REFLEXES(Prayer.IMPROVED_REFLEXES, "Improved Reflexes", 35454986,  120, 140),
    RAPID_HEAL(Prayer.RAPID_HEAL, "Rapid Heal", 35454987, 121, 141),
    RAPID_RESTORE(Prayer.RAPID_RESTORE, "Rapid Restore", 35454988, 122, 142),
    PROTECT_ITEM(Prayer.PROTECT_ITEM, "Protect Item", 35454989, 123, 143),
    STEEL_SKIN(Prayer.STEEL_SKIN, "Steel Skin", 35454990, 124, 144),
    ULTIMATE_STRENGTH(Prayer.ULTIMATE_STRENGTH, "Ultimate Strength", 35454991,  125, 145),
    INCREDIBLE_REFLEXES(Prayer.INCREDIBLE_REFLEXES, "Incredible Reflexes", 35454992,  126, 146),
    PROTECT_FROM_MAGIC(Prayer.PROTECT_FROM_MAGIC, "Protect from Magic", 35454993,  127, 147),
    PROTECT_FROM_MISSILES(Prayer.PROTECT_FROM_MISSILES, "Protect from Missiles", 35454994,  128, 148),
    PROTECT_FROM_MELEE(Prayer.PROTECT_FROM_MELEE, "Protect from Melee", 35454995,  129, 149),
    RETRIBUTION(Prayer.RETRIBUTION, "Retribution", 35454996,  131, 151),
    REDEMPTION(Prayer.REDEMPTION, "Redemption", 35454997,  130, 150),
    SMITE(Prayer.SMITE, "Smite", 35454998,  132, 152),
    HAWK_EYE(Prayer.HAWK_EYE, "Hawk Eye", 35455001,  502, 506),
    MYSTIC_LORE(Prayer.MYSTIC_LORE, "Mystic Lore", 35455002,  503, 507),
    EAGLE_EYE(Prayer.EAGLE_EYE, "Eagle Eye", 35455003,  504, 508),
    MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT, "Mystic Might", 35455004,  505, 509),
    CHIVALRY(Prayer.CHIVALRY, "Chivalry", 35455005,  945, 949),
    PIETY(Prayer.PIETY, "Piety", 35455006,  946, 950),
    RIGOUR(Prayer.RIGOUR, "Rigour", 35455007,  1420, 1424),
    AUGURY(Prayer.AUGURY, "Augury", 35455008,  1421, 1425),
    PRESERVE(Prayer.PRESERVE, "Preserve", 35455009,  947, 951);

    private Prayer prayer;
    private String name;
    private int packedID;
    private int unlockedSpriteID;
    private int lockedSpriteID;

    Prayers(Prayer prayer, String name, int packedID, int unlockedSpriteID, int lockedSpriteID) {
        this.prayer = prayer;
        this.name = name;
        this.packedID = packedID;
        this.unlockedSpriteID = unlockedSpriteID;
        this.lockedSpriteID = lockedSpriteID;
    }

    public static int[] getSpriteIDs() {
        int[] result = new int[29];
        int i = 0;
        for(Prayers p : Prayers.values()) {
            result[i] = p.unlockedSpriteID;
            i++;
        }

        return result;
    }
}
