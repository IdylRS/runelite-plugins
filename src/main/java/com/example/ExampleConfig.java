package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigItem(
		keyName = "veganFood",
		name = "Vegan Food",
		description = "The food you are allowed to eat as a vegan, comma separated"
	)
	default String veganFood()
	{
		return "Cabbage";
	}

	@ConfigItem(
			keyName = "veganNPCs",
			name = "Vegan NPCs",
			description = "The NPCs you are allowed to attack as a vegan, comma separated"
	)
	default String veganNPCs()
	{
		return "Goblin";
	}
}
