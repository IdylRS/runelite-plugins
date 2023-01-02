package com.pandemicscape;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pandemicscape")
public interface PandemicScapeConfig extends Config
{
	@ConfigItem(
		keyName = "sendChatMessage",
		name = "Notify on Infection",
		description = "Get a notification when you infect a player"
	)
	default boolean sendChatMessage()
	{
		return true;
	}
}
