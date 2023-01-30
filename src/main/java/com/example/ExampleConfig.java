package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigItem(
		keyName = "ip",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String ip()
	{
		return "192.168.0.204";
	}
}
