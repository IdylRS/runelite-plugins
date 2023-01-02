package com.pandemicscape;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Pandemic Scape"
)
public class PandemicScapePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PandemicScapeConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PandemicScapeDataManager pandemicScapeDataManager;

	private final int TICK_INTERVAL = 20;

	private List<Player> playersToInfect = null;
	private int tickCount = 0;

	@Override
	protected void startUp() throws Exception
	{

	}

	@Override
	protected void shutDown() throws Exception
	{

	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e) {
		if(e.getGameState() == GameState.LOGGED_IN) {
			Player player = client.getLocalPlayer();
			PandemicScapeData data = new PandemicScapeData(
					player.getName(),
					Instant.now().toString(),
					"Idyl",
					client.getLocalPlayer().getWorldLocation()
			);
			pandemicScapeDataManager.updatePandemicScapeApi(data);
		}
	}

	@Schedule(
			period = 20,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void attemptToInfect() {
//		tickCount++;
		if(client.getGameState() != GameState.LOGGED_IN) return;

		List<Player> players = client.getPlayers();

		playersToInfect = players.stream().filter(p ->
			p != client.getLocalPlayer() && client.getLocalPlayer().getWorldLocation().distanceTo(p.getWorldLocation()) < 10
		).collect(Collectors.toList());
		List<String> playerNames = players.stream().map(p -> p.getName()+"").collect(Collectors.toList());
		pandemicScapeDataManager.getInfectedByUsernames(playerNames);
	}

	public void onPlayerDataReceived(HashMap<String, PandemicScapeData> playerData) {
		playersToInfect.forEach(p -> {
			log.info("Attempting to infect "+p.getName());
			boolean isInfected = playerData.get(p.getName()) != null;

			if(!isInfected) infectPlayer(p);
		});

		playersToInfect = null;
	}

	private void infectPlayer(Player player) {
		log.info("Infecting "+player.getName());
		PandemicScapeData data = new PandemicScapeData(
				player.getName(),
				Instant.now().toString(),
				client.getLocalPlayer().getName(),
				client.getLocalPlayer().getWorldLocation()
		);

		pandemicScapeDataManager.updatePandemicScapeApi(data);
	}

	@Provides
    PandemicScapeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PandemicScapeConfig.class);
	}
}
