package com.hanannie;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Hanannie Affirmation"
)
public class HanannieAffirmationPlugin extends Plugin
{
	private final List<String> ACCOMPLISHMENT_SOUNDS = Arrays.asList("1.wav","14.wav","18.wav","19.wav","22.wav","23.wav","24.wav","29.wav","32.wav","35.wav","36.wav","38.wav","42.wav","43.wav","47.wav","53.wav","71.wav","72.wav","75.wav","76.wav","81.wav","82.wav","86.wav","93.wav","94.wav","96.wav","97.wav","98.wav","99.wav","100.wav","103.wav","104.wav","106.wav","111.wav","112.wav","115.wav","121.wav","122.wav","124.wav","128.wav","129.wav","130.wav","131.wav","132.wav","134.wav","136.wav");
	private final List<String> DEATH_SOUNDS = Arrays.asList("3.wav","9.wav","12.wav","13.wav","26.wav","27.wav","61.wav","91.wav","109.wav","110.wav","114.wav","116.wav","117.wav","119.wav","125.wav","126.wav");
	private final List<String> RANDOM_SOUNDS = Arrays.asList("2.wav","5.wav","6.wav","7.wav","8.wav","10.wav","11.wav","20.wav","21.wav","25.wav","28.wav","30.wav","31.wav","33.wav","34.wav","37.wav","39.wav","40.wav","44.wav","45.wav","46.wav","48.wav","49.wav","50.wav","51.wav","52.wav","54.wav","55.wav","56.wav","57.wav","58.wav","59.wav","60.wav","61.wav","62.wav","65.wav","66.wav","67.wav","68.wav","69.wav","70.wav","73.wav","74.wav","77.wav","78.wav","79.wav","80.wav","87.wav","88.wav","89.wav","90.wav","92.wav","95.wav","101.wav","102.wav","105.wav","113.wav","118.wav","120.wav","123.wav","127.wav","133.wav","137.wav","138.wav","139.wav","140.wav");
	private final List<String> PKING_SOUNDS = Arrays.asList("107.wav","108.wav","135.wav");
	private final List<String> LOG_SOUNDS = Arrays.asList("41.wav","60.wav","85.wav");
	private final List<String> LEVEL_SOUNDS = Arrays.asList("15.wav","16.wav","17.wav");

	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log:.*");
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private HanannieAffirmationConfig config;

	@Inject
	private SoundEngine soundEngine;

	private HashMap<Skill, Integer> skillLevels;
	private HashMap<Skill, Integer> skillXP;

	private Instant lastTime;

	private boolean soundPlaying = false;
	private long duration = -1;

	@Override
	protected void startUp() throws Exception
	{

	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onActorDeath(ActorDeath e) {
		if(e.getActor().equals(client.getLocalPlayer()) && config.onDeath()) {
			playRandomSound("death", DEATH_SOUNDS);
		}
		else if(e.getActor() instanceof Player && client.getLocalPlayer().getInteracting().equals(e.getActor())) {
			if(config.onPK()) playRandomSound("pking", PKING_SOUNDS);
		}
	}

	@Subscribe
	public void onGameTick(GameTick e){
		if(client.getLocalPlayer() == null) return;

		// Plays a sound ~30mins
		if(config.random() && Math.random()*3000 < 1) {
			playRandomSound("random", RANDOM_SOUNDS);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged e) {
		if(skillLevels == null) {
			skillLevels = new HashMap<>();
		}
		if(skillXP == null) {
			skillXP = new HashMap<>();
		}

		if(skillLevels.get(e.getSkill()) == null) {
			skillLevels.put(e.getSkill(), e.getLevel());
			return;
		}
		if(skillXP.get(e.getSkill()) == null) {
			skillXP.put(e.getSkill(), e.getXp());
			return;
		}

		if(e.getLevel() > skillLevels.get(e.getSkill())) {
			skillLevels.put(e.getSkill(), e.getLevel());
			if(config.onLevel()) playRandomSound("level", LEVEL_SOUNDS);

		}
		else if(e.getXp() != skillXP.get(e.getSkill())) {
			int difference = e.getXp() - skillXP.get(e.getSkill());
			skillXP.put(e.getSkill(), e.getXp());

			// 1 in 3k scaled linearly by xp gained
			if(config.onXpDrop() && Math.random()*3000 < difference) {
				playRandomSound("accomplishment", ACCOMPLISHMENT_SOUNDS);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
			return;
		}

		if (config.onLog() && COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage()).matches()) {
			playRandomSound("log", LOG_SOUNDS);
		}
	}

	@Subscribe
	public void onClientTick(ClientTick tick) {
		if(lastTime == null) lastTime = Instant.now();

		Duration deltaTime = Duration.between(Instant.now(), lastTime);
		long microseconds = deltaTime.getNano() / 1000;

		if(soundPlaying) {
			duration -= microseconds;

			if(duration <= 0) {
				log.info("Sound stopped.");
				soundPlaying = false;
			}
		}
	}

	private void playRandomSound(String directory, List<String> sounds) {
		if(soundPlaying) return;

		duration = soundEngine.playClip(directory+"/"+sounds.get(getRandom(sounds.size())));
		if(duration > -1) {
			log.info("Sound started.");
			soundPlaying = true;
		}
	}

	private int getRandom(int size) {
		return (int) Math.floor(Math.random()*size);
	}

	@Provides
	HanannieAffirmationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HanannieAffirmationConfig.class);
	}
}
