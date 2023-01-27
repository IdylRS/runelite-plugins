package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ExampleConfig config;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		NPC npc = e.getMenuEntry().getNpc();
		if(npc != null) {
			MenuEntry newEntry = client.createMenuEntry(-1);
			newEntry.setOption("Copy");
			newEntry.setTarget(npc.getName());
			newEntry.setIdentifier(npc.getId());
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if(e.getMenuOption().equals("Copy")) {
			log.info(e.getMenuEntry().getIdentifier()+"");
			configManager.setConfiguration("example", "npc", e.getMenuEntry().getIdentifier());
			NPC npc = client.getNpcs().stream().filter(n -> n.getId() == e.getMenuEntry().getIdentifier()).collect(Collectors.toList()).get(0);
			client.getLocalPlayer().setIdlePoseAnimation(npc.getIdlePoseAnimation());
			if(npc.getRunAnimation() != -1) {

			}
			client.getLocalPlayer().setWalkRotate180(npc.getWalkRotate180());
			client.getLocalPlayer().setWalkRotateLeft(npc.getWalkRotateLeft());
			client.getLocalPlayer().setWalkRotateRight(npc.getWalkRotateRight());
			client.getLocalPlayer().setRunAnimation(npc.getRunAnimation());
			npc.getAnim
			client.getLocalPlayer().setWalkAnimation(npc.getWalkAnimation());
		}

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		client.getLocalPlayer().getPlayerComposition().setTransformedNpcId(config.npc());
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
