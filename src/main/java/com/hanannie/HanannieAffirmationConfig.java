package com.hanannie;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hanannie")
public interface HanannieAffirmationConfig extends Config
{
    @ConfigItem(
            description = "Play a random Hanannie sound on death",
            name = "Play on Death",
            keyName = "onDeath"
    )
    default boolean onDeath() { return true; }

    @ConfigItem(
            description = "Play a random Hanannie sound at random times",
            name = "Play Randomly",
            keyName = "random"
    )
    default boolean random() { return true; }

    @ConfigItem(
            description = "Play a random Hanannie sound occasionally when you gain xp",
            name = "Play on XP",
            keyName = "onXpDrop"
    )
    default boolean onXpDrop() { return true; }

    @ConfigItem(
            description = "Play a random Hanannie sound when you level up",
            name = "Play on Level Up",
            keyName = "onLevel"
    )
    default boolean onLevel() { return true; }

    @ConfigItem(
            description = "Play a random Hanannie sound when you PK someone",
            name = "Play on PK",
            keyName = "onPK"
    )
    default boolean onPK() { return true; }

    @ConfigItem(
            description = "Play when you get a log slot (must have chat notification on)",
            name = "Play on Log Slot",
            keyName = "onLog"
    )
    default boolean onLog() { return true; }
}
