package com.pandemicscape;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

	private List<Player> playersToInfect = null;
	private PandemicScapeData userData;

	@Override
	protected void startUp() throws Exception
	{

	}

	@Override
	protected void shutDown() throws Exception
	{

	}

	@Schedule(
			period = 20,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void attemptToInfect() {
		if(client.getGameState() != GameState.LOGGED_IN) return;

		List<Player> players = client.getPlayers();
		playersToInfect = players.stream().filter(p ->
			client.getLocalPlayer().getWorldLocation().distanceTo(p.getWorldLocation()) < 10
		).collect(Collectors.toList());
		List<String> playerNames = players.stream().map(p -> p.getName()+"").collect(Collectors.toList());
		pandemicScapeDataManager.getInfectedByUsernames(playerNames);
	}

	public void onPlayerDataReceived(HashMap<String, PandemicScapeData> playerData) {
		List<PandemicScapeData> infectedPlayers = new ArrayList<>();
		List<String> infectedPlayerNames = new ArrayList<>();

		playersToInfect.forEach(p -> {
			boolean isInfected = playerData.get(p.getName()) != null;

			if(!isInfected) {
				// 1 in 10 chance to infect nearby players
				double roll = Math.random()*10;
				if(roll < 1) {
					infectedPlayerNames.add(p.getName());
					infectedPlayers.add(infectPlayer(p));

					if(config.sendChatMessage())
						clientThread.invokeLater(() -> {
							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You have infected "+p.getName()+".", "");
						});
				}
			}
		});

		pandemicScapeDataManager.updatePandemicScapeApi(infectedPlayers);
		playersToInfect = null;
	}

	private PandemicScapeData infectPlayer(Player player) {
		PandemicScapeData data = new PandemicScapeData(
				player.getName(),
				Instant.now().toString(),
				client.getLocalPlayer().getName(),
				player.getWorldLocation()
		);

		return data;
	}

	@Provides
    PandemicScapeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PandemicScapeConfig.class);
	}
}
