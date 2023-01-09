package com.bad;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class BadPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BadConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	private WorldPoint lastPoint;
	private HashMap<Skill, Integer> skillXP;

	private Widget overlay;

	List<String> BLACKLIST = Arrays.asList("Eat", "Mine", "Chop", "Attack", "Use", "Talk", "Fish", "Pickpocket", "Steal", "Lay");
	List<String> SMELLY_BLACKLIST = Arrays.asList("Talk", "Shop", "Buy", "Follow", "Trade");

	List<String> TOXIC_LINES = Arrays.asList(
			"LMFAOOOOO no way you just died like that bro!!",
			"Uninstall this game right now you gosh darn loser!",
			"I hate you and you are an idiot",
			"lol",
			"Have you tried keeping your HP above 0??? lmaoooo",
			"Idiot",
			"Moron",
			"Sit"
	);

	List<String> BOB_WHOLESOME_LINES = Arrays.asList(
			"Keep going, you're doing great *meow*",
			"I'm proud of everything you're doing *meow*",
			"You're life is valid, don't let anyone tell you different *meow*"
	);

	List<String> BOB_WHOLESOME_LINES_DEAD = Arrays.asList(
			"You're the reason I'm dead *meow*"
	);

	private int MAX_TICKS_TIL_POOP = 20;
	private int ticksTilPoop = -1;

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
		if(e.getActor().equals(client.getLocalPlayer())) {
			if(config.fortniteDeathSound()) soundEngine.playClip(Sound.FORTNITE);

			if(config.toxicNPCs()) {
				client.getNpcs().forEach(npc -> {
					int roll = (int) Math.floor(Math.random()*TOXIC_LINES.size());
					npc.setOverheadText(TOXIC_LINES.get(roll));
					npc.setOverheadCycle(600);
				});
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e) {
		if(
			e.getHitsplat().getAmount() == 0 &&
			e.getHitsplat().getHitsplatType() == HitsplatID.BLOCK_ME &&
			!e.getActor().equals(client.getLocalPlayer()) &&
			config.fartOnMiss()
		) {
			soundEngine.playClip(Sound.FART);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if(config.smellyFeet()) {
			SMELLY_BLACKLIST.forEach(option -> {
				if (e.getMenuOption().contains(option)) {
					e.consume();
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Your feet are too smelly for anyone to want to interact with you.", ""));
				}
			});
		}

		if(config.hideUsefulEntries()) {
			BLACKLIST.forEach(option -> {
				if (e.getMenuOption().contains(option)) {
					e.consume();
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No.", ""));
				}
			});
		}

		if(config.songOnKebab() && e.getMenuOption().contains("Eat")) {
			ticksTilPoop = MAX_TICKS_TIL_POOP;
		}

		if(config.songOnKebab() && e.getMenuOption().contains("Eat") && e.getMenuTarget().toLowerCase().contains("kebab")) {
			soundEngine.playClip(Sound.KEBAB);
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged e) {
		if(e.getOverheadText().equals("Raarrrrrgggggghhhhhhh!") && config.dragonBattleAxeRawr()) {
			e.getActor().setOverheadText("Rawr X[)");
			soundEngine.playClip(Sound.UWU);
		}

		if(e.getActor().equals(client.getLocalPlayer()) && config.sendTweets()) {
			sendTweet(e.getOverheadText());
		}

		if(client.getLocalPlayer().getWorldLocation().getRegionID() == 12598 && config.muteGE()) {
			client.getPlayers().forEach(p -> p.setOverheadText(""));
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e) {
		if(e.getActor().getAnimation() == AnimationID.TZTOK_JAD_MAGIC_ATTACK) {
			e.getActor().setAnimation(AnimationID.TZTOK_JAD_RANGE_ATTACK);
		}
		else if(e.getActor().getAnimation() == AnimationID.TZTOK_JAD_RANGE_ATTACK) {
			 e.getActor().setAnimation(AnimationID.TZTOK_JAD_MAGIC_ATTACK);
		}
	}

	@Subscribe
	public void onGameTick(GameTick e){
		if(client.getLocalPlayer() == null) return;

		if(config.lagSimulator()) {
			double roll = Math.random()*50;

			if(roll < 1) {
				client.setGameState(GameState.CONNECTION_LOST);
			}
		}

		if(ticksTilPoop > 0) {
			ticksTilPoop--;

			if(ticksTilPoop == 6) {
				client.getLocalPlayer().setOverheadText("Uh oh!! I have think I have to make a poopy!!");
			}
		}
		else if(ticksTilPoop == 0) {
			soundEngine.playClip(Sound.WET_FART);
			client.getLocalPlayer().setOverheadText("");
			addPoop(client.getLocalPlayer().getWorldLocation());
			ticksTilPoop = -1;
		}

		if(lastPoint == null) lastPoint = client.getLocalPlayer().getWorldLocation();

		if(lastPoint.distanceTo(client.getLocalPlayer().getWorldLocation()) > 0 && config.playStomp()) {
			client.playSoundEffect(3834);

			if(lastPoint.getX() > client.getLocalPlayer().getWorldLocation().getX() && config.walkWestLogOut()) {
				client.setGameState(GameState.LOGIN_SCREEN);
			}
		}

		if(config.bobMessages()) {
			double roll = Math.random()*100;
			if(roll < 1) {
				int index = (int) Math.floor(Math.random()*BOB_WHOLESOME_LINES.size());
				clientThread.invokeLater(() -> {
					client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							"",
							"Bob the cat says: "+BOB_WHOLESOME_LINES.get(index),
							"Bob the cat"
					);
				});
			}
		}

		if(config.darkenWintertodt() && client.getLocalPlayer().getWorldLocation().getRegionID() == 6462 && this.overlay != null) {
			this.overlay.setOpacity(165);
		}
		else if(!config.screenDarkener() && !config.healthDarkener()) {
			this.overlay.setOpacity(255);
		}
		lastPoint = client.getLocalPlayer().getWorldLocation();
	}

	@Subscribe
	public void onStatChanged(StatChanged e) {
		if(skillXP == null) {
			skillXP = new HashMap<>();
		}

		if(skillXP.get(e.getSkill()) == null) {
			skillXP.put(e.getSkill(), e.getXp());
			return;
		}

		if(skillXP.get(e.getSkill()) < e.getXp() && config.playGoat()) {
			soundEngine.playClip(Sound.GOAT);
			if(config.screenDarkener()) overlay.setOpacity(255);
		}

		if(e.getSkill().equals(Skill.AGILITY) && skillXP.get(e.getSkill()) < e.getXp()) {
			client.setGameState(GameState.LOGIN_SCREEN);
		}

		if(e.getSkill().equals(Skill.HITPOINTS) && config.healthDarkener()) {
			double maxLevel = e.getLevel();
			double maxOpacity = 255;
			double currentLevel = e.getBoostedLevel();
			int opacity = (int) ((currentLevel / maxLevel) * maxOpacity);

			log.info("hp: "+currentLevel+" maxHp: "+maxLevel+" current opacity: "+opacity);

			this.overlay.setOpacity(opacity);
		}

		skillXP.put(e.getSkill(), e.getXp());
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == 548) {
			Widget parent = client.getWidget(548, 26);
			overlay = client.getWidget(548, 26).createChild(WidgetType.RECTANGLE);
			overlay.setFilled(true);
			overlay.setOpacity(255);
			overlay.setWidthMode(1);
			overlay.setHeightMode(1);
			overlay.setXPositionMode(1);
			overlay.setYPositionMode(1);
			overlay.setModelType(1);
			overlay.setModelZoom(100);
			overlay.revalidate();
		}
		if(e.getGroupId() == WidgetInfo.QUESTLIST_BOX.getGroupId()) {
			Arrays.asList(client.getWidget(399, 7).getChildren()).forEach(w -> {
				if(w.getText().contains("Recipe for Disaster")) {
					log.info(w.getText());
					w.setHidden(true);
					w.revalidate();
				}
			});
		}
	}

	private void sendTweet(String tweet) {
		String url = "http://localhost:3000";
		String toSend = urlifyString(tweet);

		try {
			Request r = new Request.Builder()
					.url(url.concat("?tweet="+toSend))
					.get()
					.build();

			okHttpClient.newCall(r).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					log.info("Error getting prop hunt data by username", e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					response.close();
				}
			});
		}
		catch(IllegalArgumentException e) {
			log.error("Bad URL given: " + e.getLocalizedMessage());
		}
	}

	private void addPoop(WorldPoint point) {
		Model model = client.loadModel(5690);

		RuneLiteObject poo = client.createRuneLiteObject();
		poo.setModel(model);
		poo.setActive(true);
		poo.setLocation(LocalPoint.fromWorld(client, point), point.getPlane());
	}

	@Provides
	BadConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BadConfig.class);
	}

	private String urlifyString(String str) {
		return str.trim().replaceAll("\\s", "%20");
	}
}

class Tweet {
	String tweet;
	String sender;

	public Tweet(String tweet, String sender) {
		this.tweet = tweet;
		this.sender = sender;
	}
}
