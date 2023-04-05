package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
    @ConfigItem(
            keyName = "opponent",
            name = "Opponent",
            description = "The username of your opponent"
    )
    default String opponent() { return ""; }
}
