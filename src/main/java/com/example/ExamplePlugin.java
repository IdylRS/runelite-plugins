package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	private final int AUTOCAST_INDEX_VARBIT_ID = 276;

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

	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if(e.getMenuOption().equals("Eat")) {
			String itemName = client.getItemDefinition(e.getItemId()).getName();
			List<String> veganFoodItems = getVeganFoodItems();

			if(!veganFoodItems.contains(itemName.toLowerCase())) {
				e.consume();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", itemName+" is not vegan.", "");
			}
		}
		else if(e.getMenuOption().equals("Attack") || e.getMenuOption().equals("Cast")) {
			String npcName = e.getMenuEntry().getNpc().getName();
			List<String> veganNPCs = getVeganNPCs();

			if(!veganNPCs.contains(npcName.toLowerCase())) {
				e.consume();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attacking "+npcName+" would not be very vegan of you.", "");
			}

			boolean isAutoCasting = client.getVarbitValue(AUTOCAST_INDEX_VARBIT_ID) > 0;
			if(!isAutoCasting || !e.getMenuOption().equals("Cast")) {
				e.consume();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can only attack using magic.", "");
			}
		}
	}

	public List<String> getVeganFoodItems() {
		List<String> list = Arrays.asList(config.veganFood().split(","));
		list = list.stream().map(v -> v.trim().toLowerCase()).collect(Collectors.toList());

		return list;
	}

	public List<String> getVeganNPCs() {
		List<String> list = Arrays.asList(config.veganNPCs().split(","));
		list = list.stream().map(v -> v.trim().toLowerCase()).collect(Collectors.toList());

		return list;
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
