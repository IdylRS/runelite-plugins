package com.survivalist;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("survivalist")
public interface SurvivalistConfig extends Config
{
	@ConfigItem(
		keyName = "age",
		name = "Age",
		description = "The age you are currently in"
	)
	default Age age()
	{
		return Age.STEEL_AGE;
	}
}
