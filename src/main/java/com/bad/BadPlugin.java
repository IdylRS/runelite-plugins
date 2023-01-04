package com.bad;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class BadPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BadConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e) {
		if(
			e.getHitsplat().getAmount() == 0 &&
			e.getHitsplat().getHitsplatType() == HitsplatID.BLOCK_ME &&
			!e.getActor().equals(client.getLocalPlayer())
		) {
			soundEngine.playClip(Sound.FART);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {

	}

	@Provides
	BadConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BadConfig.class);
	}
}
