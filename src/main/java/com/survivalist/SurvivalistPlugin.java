package com.survivalist;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.lwjgl.system.linux.Stat;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;

@Slf4j
@PluginDescriptor(
	name = "Survivalist"
)
public class SurvivalistPlugin extends Plugin
{
	private final int TICKS_PER_DAY = 2400;
	private final int FIRE_OBJECT_ID = 26185;
	private final int WARMTH_DISTANCE = 10;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SurvivalistConfig config;

	private Widget overlay;

	@Getter
	private int gameTime = 0;

	@Getter
	private final HashMap<StatusEffect, Integer> statusEffects = new HashMap<>();
	private final HashMap<StatusEffect, StatusEffectInfobox> statusEffectInfoboxs = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		for(StatusEffect effect : StatusEffect.values()) {
			statusEffects.put(effect, 0);
			statusEffectInfoboxs.put(effect, new StatusEffectInfobox(effect, itemManager.getImage(effect.getItemIconID()), this));
			infoBoxManager.addInfoBox(statusEffectInfoboxs.get(effect));
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		for(StatusEffectInfobox effectInfobox : statusEffectInfoboxs.values()) {
			infoBoxManager.removeInfoBox(effectInfobox);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == 548) {
			createNightTimeOverlay();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
		}
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		this.gameTime = (this.gameTime+1) % TICKS_PER_DAY;
		TimeOfDay tod = TimeOfDay.getTimeOfDay(this.gameTime);
		decrementStatusEffects();

		if(this.overlay != null) {
			int targetDarkness = tod.getDarkness();
			int currentDarkness = this.overlay.getOpacity();
			if(currentDarkness > targetDarkness) {
				this.overlay.setOpacity(Math.max(targetDarkness, currentDarkness-3));
			}
			else {
				this.overlay.setOpacity(Math.min(targetDarkness, currentDarkness+3));
			}
		}

		if(tod == TimeOfDay.NIGHT) {
			int fireDistance = checkForFire();
			if(fireDistance >= 0) {
				statusEffects.put(StatusEffect.WARM, 1);
				int brightness = 100 - fireDistance * 3;
				this.overlay.setOpacity(TimeOfDay.NIGHT.getDarkness()+brightness);
			}
			else {
				statusEffects.put(StatusEffect.COLD, 1);
			}
		}
		else {
			statusEffects.put(StatusEffect.COLD, 0);
		}
	}

	@Subscribe
	public

	private void createNightTimeOverlay() {
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

	private int checkForFire() {
		Tile[][][] tiles = client.getScene().getTiles();
		int plane = client.getPlane();

		for(int x=0;x<Constants.SCENE_SIZE;++x) {
			for(int y=0;y<Constants.SCENE_SIZE;++y) {
				Tile tile = tiles[plane][x][y];

				for(GameObject go : tile.getGameObjects()) {
					if(go != null && go.getId() == FIRE_OBJECT_ID) {
						int distance = client.getLocalPlayer().getWorldLocation().distanceTo(tile.getWorldLocation());
						if(distance <= WARMTH_DISTANCE) {
							return distance;
						}
					}
				}
			}
		}

		return -1;
	}

	private void decrementStatusEffects() {
		for(StatusEffect effect : statusEffects.keySet()) {
			if(statusEffects.get(effect) > 0) {
				statusEffects.put(effect, statusEffects.get(effect)-1);
			}
		}
	}

	@Provides
	SurvivalistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SurvivalistConfig.class);
	}
}
