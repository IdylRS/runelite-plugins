package com.example;

import net.runelite.api.NpcID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigItem(
		keyName = "npc",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default int npc()
	{
		return NpcID.KNIGHT_OF_ARDOUGNE;
	}
}
