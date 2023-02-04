package com.survivalist;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("survivalist")
public interface SurvivalistConfig extends Config
{
	@ConfigItem(
			keyName = "showAgeBoss",
			description = "Show the age's boss in the overlay",
			name = "Show Age Boss"
	)
	default boolean showAgeBoss() {
		return true;
	}

	@ConfigItem(
			keyName = "pause",
			description = "Pause all status effects and time",
			name = "Pause"
	)
	default boolean pause() {
		return false;
	}
}
