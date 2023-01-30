package com.survivalist;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum Age {
    STEEL_AGE("Steel Age", NpcID.COUNT_DRAYNOR, "Leather", "Dramen", "Beekeeper", "Iron", "Bronze", "Steel", "Wizard", "Blue wizard", "Training", "Black robe", "Black skirt", "Blue skirt", "Studded", "Hardleather"),
    MITHRIL_AGE("Mithril Age", NpcID.MELZAR_THE_MAD, "Mithril", "Mith", "Black", "White", "Initiate", "Frog-leather", "Xerician"),
    MAGIC_AGE("Magic Age", NpcID.CRAZY_ARCHAEOLOGIST, "Adamant", "Shayzien", "Staff", "Samurai", "Proselyte", "Ranger", "Green d'hide"),
    RUNITE_AGE("Runite Age", NpcID.BRYOPHYTA, "Rune", "Runite", "Rock-shell", "Blue d'hide", "Mystic", "Elder chaos", "Infinity", "Void", "Spined"),
    DRAGON_AGE("Dragon Age", NpcID.KING_BLACK_DRAGON, "Dragon", "Swampbark", "Bloodbark", "Red d'hide", "Granite", "Obsidian");

    private String name;
    private int bossID;
    private String[] itemPrefixes;

    Age(String name, int bossID, String ...itemPrefixes) {
        this.name = name;
        this.bossID = bossID;
        this.itemPrefixes = itemPrefixes;
    }

    public static List<String> getIllegalItemPrefixes(Age age) {
        List<String> prefixes = new ArrayList<>();
        boolean start = false;

        for(Age a : Age.values()) {
            if(start) {
                prefixes.addAll(Arrays.asList(a.itemPrefixes));
            }

            if(a.equals(age)) start = true;
        }

        return prefixes;
    }
}
