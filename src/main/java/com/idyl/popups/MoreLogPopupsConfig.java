package com.idyl.popups;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(MoreLogPopupsPlugin.CONFIG_GROUP)
public interface MoreLogPopupsConfig extends Config
{
	@ConfigItem(
		keyName = "items",
		name = "Items",
		description = "The items to show popups for"
	)
	default String items()
	{
		return "Bones";
	}

	@ConfigItem(
			keyName = "showOnce",
			name = "Show Item Popups Once",
			description = "Once an item's popup has been shown, don't show it for subsequent drops"
	)
	default boolean showOnce()
	{
		return true;
	}

	@ConfigItem(
			keyName = "reset",
			name = "Reset Unlocked Items",
			description = "Click to reset unlocked items, this is a makeshift button"
	)
	default boolean reset()
	{
		return false;
	}

	@ConfigItem(
			keyName = MoreLogPopupsPlugin.UNLOCK_CONFIG_KEY,
			name = "Unlocked Items",
			description = "The items that have been unlocked",
			hidden = true
	)
	default String unlockedItems()
	{
		return "";
	}
}
