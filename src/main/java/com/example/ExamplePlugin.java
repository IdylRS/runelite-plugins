package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e) {
		if(e.getType() == MenuAction.EXAMINE_NPC.getId() && e.getMenuEntry().getNpc() != null && e.getMenuEntry().getNpc().getId() == NpcID.COW_CALF) {
			client.createMenuEntry(-1)
					.setTarget(e.getTarget())
					.setIdentifier(e.getMenuEntry().getNpc().getId())
					.setType(MenuAction.RUNELITE)
					.setOption("Tame")
					.onClick(this::tameAnimal)
		}
	}

	private void tameAnimal(MenuEntry e) {
		log.info(e.getNpc());
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
