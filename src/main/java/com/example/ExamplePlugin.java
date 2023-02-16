package com.example;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	ClientThread clientThread;

	@Inject
	private ExampleConfig config;

	@Inject
	private Hooks hooks;

	private HashMap<WorldPoint, RuneLiteObject> objects = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		for(WorldPoint p : objects.keySet()) {
			clientThread.invoke(() -> objects.get(p).setActive(false));
		}
		objects.clear();
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		for(NPC npc : client.getNpcs()) {
			if(npc.getName().contains("Fishing spot")) {
				if(objects.get(npc.getWorldLocation()) == null) {
					RuneLiteObject obj = client.createRuneLiteObject();
					npc.setDead(true);
					client.getNpcDefinition(NpcID.FISHING_SPOT_10565);
					transmogPlayer(npc, 41967);
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e) {
		if(objects.get(e.getNpc().getWorldLocation()) != null) {
			objects.get(e.getNpc().getWorldLocation()).setActive(false);
			objects.remove(e.getNpc().getWorldLocation());
		}
	}

	private void transmogPlayer(NPC npc, int modelID) {
		if(client.getLocalPlayer() == null) return;

		RuneLiteObject disguise = client.createRuneLiteObject();

		LocalPoint loc = LocalPoint.fromWorld(client, npc.getWorldLocation());
		if (loc == null)
		{
			return;
		}

		Model model = client.loadModel(modelID);

		if (model == null)
		{
			final Instant loadTimeOutInstant = Instant.now().plus(Duration.ofSeconds(5));

			clientThread.invoke(() ->
			{
				if (Instant.now().isAfter(loadTimeOutInstant))
				{
					return true;
				}

				Model reloadedModel = client.loadModel(modelID);

				if (reloadedModel == null)
				{
					return false;
				}

				return true;
			});
		}
		else {
			disguise.setModel(model);
		}

		disguise.setShouldLoop(true);
		disguise.setAnimation(client.loadAnimation(7634));
		disguise.setLocation(npc.getLocalLocation(), npc.getWorldLocation().getPlane());
		disguise.setActive(true);

		objects.put(npc.getWorldLocation(), disguise);
	}


//	@VisibleForTesting
//	boolean shouldDraw(Renderable renderable, boolean drawingUI)
//	{
//		if (renderable instanceof NPC)
//		{
//			NPC npc = (NPC) renderable;
//
//			return !npc.getName().contains("Fishing spot");
//		}
//
//		return true;
//	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
