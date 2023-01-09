package com.bad;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bad")
public interface BadConfig extends Config
{
	@ConfigItem(
		keyName = "fartOnMiss",
		name = "Play Fart on Miss",
		description = "Play fart when you hit a 0"
	)
	default boolean fartOnMiss()
	{
		return true;
	}

	@ConfigItem(
			keyName = "songOnKebab",
			name = "Eat Kebab Song",
			description = "Play a song when you eat a kebab"
	)
	default boolean songOnKebab()
	{
		return true;
	}

	@ConfigItem(
			keyName = "fortniteDeathSound",
			name = "Chug Jug on Death",
			description = "Play a Chug Jug when you die"
	)
	default boolean fortniteDeathSound()
	{
		return true;
	}

	@ConfigItem(
			keyName = "playStomp",
			name = "Player Stomp Sound",
			description = "Play the Graador walk when you move"
	)
	default boolean playStomp()
	{
		return true;
	}

	@ConfigItem(
			keyName = "playGoat",
			name = "Goat Sound",
			description = "Play the screaming goat when you gain xp"
	)
	default boolean playGoat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "hideUsefulEntries",
			name = "Disable Useful Entries",
			description = "Hide all useful menu entries"
	)
	default boolean hideUsefulEntries()
	{
		return false;
	}

	@ConfigItem(
			keyName = "smellyFeet",
			name = "Smelly Feet Mode",
			description = "Can't Interact with Friendly NPCs or Players"
	)
	default boolean smellyFeet()
	{
		return false;
	}

	@ConfigItem(
			keyName = "walkWestLogOut",
			name = "Walk West Log Out",
			description = "Log out when you walk west"
	)
	default boolean walkWestLogOut()
	{
		return false;
	}

	@ConfigItem(
			keyName = "dragonBattleAxeRawr",
			name = "Dragon Battle Axe Spec",
			description = "Replace the Rarrrrrgghhh with Rawr XD"
	)
	default boolean dragonBattleAxeRawr()
	{
		return true;
	}

	@ConfigItem(
			keyName = "muteGE",
			name = "Mute GE Chat",
			description = "Mutes overhead chat at the GE"
	)
	default boolean muteGE()
	{
		return true;
	}

	@ConfigItem(
			keyName = "toxicNPCs",
			name = "Toxic NPCs",
			description = "NPCs will be toxic when you die"
	)
	default boolean toxicNPCs()
	{
		return true;
	}

	@ConfigItem(
			keyName = "bobMessages",
			name = "Bob Messages",
			description = "Bob the Cat sends you wholesome messages"
	)
	default boolean bobMessages()
	{
		return true;
	}

	@ConfigItem(
			keyName = "screenDarkener",
			name = "Screen Darkening",
			description = "Screen gets darker until you gain xp"
	)
	default boolean screenDarkener()
	{
		return false;
	}

	@ConfigItem(
			keyName = "healthDarkener",
			name = "Health Darkener",
			description = "Screen gets darker with your HP"
	)
	default boolean healthDarkener()
	{
		return false;
	}

	@ConfigItem(
			keyName = "darkenWintertodt",
			name = "Dark Mode Wintertodt",
			description = "Screen gets darker at Wintertodt"
	)
	default boolean darkenWintertodt()
	{
		return true;
	}

	@ConfigItem(
			keyName = "lagSimulator",
			name = "Lag Simulator",
			description = "Game occasionally disconnects"
	)
	default boolean lagSimulator()
	{
		return false;
	}

	@ConfigItem(
			keyName = "sendTweets",
			name = "Send Chat Messages as Tweets",
			description = "Send Chat Messages as Tweets"
	)
	default boolean sendTweets()
	{
		return false;
	}
}
