package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{
	@ConfigSection(
			name = "Allergies",
			description = "Allergies",
			closedByDefault = true,
			position = 0
	)
	String allergies = "allergies";

	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
			keyName = "allergyDogs",
			name = "Allergic to dogs",
			description = "Allergic to dogs",
			section = allergies
	)
	default boolean allergyDogs() { return false; }

	@ConfigItem(
			keyName = "allergyCats",
			name = "Allergic to cats",
			description = "Allergic to cats",
			section = allergies
	)
	default boolean allergyCats() { return false; }

	@ConfigItem(
			keyName = "allergyWomen",
			name = "Allergic to women",
			description = "Allergic to women",
			section = allergies
	)
	default boolean allergyWomen() { return true; }
}
