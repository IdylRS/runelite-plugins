package com.survivalist;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.util.HashMap;

@Slf4j
@PluginDescriptor(
	name = "Survivalist"
)
public class SurvivalistPlugin extends Plugin
{
	private final int TICKS_PER_DAY = 2400;
	private final int MAX_LIFE_POINTS = 1000;
	private final int MAX_HUNGER = 100;
	private final int FIRE_OBJECT_ID = 26185;
	private final double WARMTH_DISTANCE = 10;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SurvivalistConfig config;

	private Widget overlay;

	@Getter
	private int gameTime = 500;
	@Getter
	private int lifePoints = 1000;
	@Getter
	private int hunger = 30;

	@Getter
	private final HashMap<StatusEffect, Integer> statusEffects = new HashMap<>();
	private final HashMap<StatusEffect, StatusEffectInfobox> statusEffectInfoboxs = new HashMap<>();

	private final ItemStatChanges itemStats = new ItemStatChanges();

	private SurvivalistOverlay survivalistOverlay;

	@Override
	protected void startUp() throws Exception
	{
		survivalistOverlay = new SurvivalistOverlay(this);
		overlayManager.add(survivalistOverlay);
		if(client.getGameState() == GameState.LOGGED_IN) {
			if(this.overlay != null) this.overlay.setHidden(false);
			else clientThread.invokeLater(this::createNightTimeOverlay);
		}

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

		overlayManager.remove(survivalistOverlay);
		survivalistOverlay = null;
		if(this.overlay != null) this.overlay.setHidden(true);
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
		decrementStatusEffects();

		this.gameTime = (this.gameTime+1) % TICKS_PER_DAY;
		TimeOfDay tod = TimeOfDay.getTimeOfDay(this.gameTime);

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
				double brightness = 110 - (fireDistance / WARMTH_DISTANCE)*100;
				this.overlay.setOpacity((int) (TimeOfDay.NIGHT.getDarkness()+brightness));
			}
			else {
				statusEffects.put(StatusEffect.COLD, 1);
			}
		}
		else {
			statusEffects.put(StatusEffect.COLD, 0);
		}

		checkWeight();
		updateHunger();
		updateInjury();
		updateLifePoints();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e) {
		if(e.getContainerId() == InventoryID.INVENTORY.getId() || e.getContainerId() == InventoryID.EQUIPMENT.getId()) {
			checkWeight();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if(e.getMenuOption().equals("Eat") && statusEffects.get(StatusEffect.EATING) == 0) {
			addHunger(itemStats.get(e.getItemId()));
		}
		else if(e.getMenuOption().equals("Eat")) {
			e.consume();
		}
	}

	private void createNightTimeOverlay() {
		Widget parent = client.getWidget(548, 26);
		overlay = client.getWidget(548, 26).createChild(WidgetType.RECTANGLE);
		overlay.setFilled(true);
		overlay.setOpacity(TimeOfDay.getTimeOfDay(this.gameTime).getDarkness());
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

	private void checkWeight() {
		int weight = client.getWeight();
		int agilityLevel = client.getRealSkillLevel(Skill.AGILITY);
		int maxWeight = 10 + (int) Math.floor(agilityLevel/2);

		if(weight > maxWeight) {
			statusEffects.put(StatusEffect.OVERWEIGHT, 1);
		}
	}

	private void decrementStatusEffects() {
		for(StatusEffect effect : statusEffects.keySet()) {
			if(statusEffects.get(effect) > 0) {
				statusEffects.put(effect, statusEffects.get(effect)-1);
			}
		}
	}

	private void updateLifePoints() {
		for(StatusEffect effect : statusEffects.keySet()) {
			if(statusEffects.get(effect) > 0) {
				lifePoints = Math.max(0, Math.min(effect.getLpPerTick()+lifePoints, MAX_LIFE_POINTS));
			}
		}
	}

	private void updateHunger() {
		if(this.gameTime % 3 == 0) {
			hunger = Math.max(0, Math.min(MAX_HUNGER, hunger-1));
		}

		if(Hunger.getHunger(this.hunger) == Hunger.FULL) {
			statusEffects.put(StatusEffect.FULL, 1);
		}
		else {
			statusEffects.put(StatusEffect.FULL, 0);
		}

		if(Hunger.getHunger(this.hunger) == Hunger.HUNGRY) {
			statusEffects.put(StatusEffect.STARVING, 1);
		}
		else if(Hunger.getHunger(this.hunger) == Hunger.STARVING) {
			statusEffects.put(StatusEffect.STARVING, 1);
		}
		else {
			statusEffects.put(StatusEffect.HUNGRY, 0);
			statusEffects.put(StatusEffect.STARVING, 0);
		}
	}

	private void addHunger(int amount) {
		this.hunger = Math.max(0, Math.min(MAX_HUNGER, hunger+amount*5));
		this.hunger = Math.max(0, Math.min(MAX_HUNGER, hunger+amount*5));

		if(amount > 0)
			statusEffects.put(StatusEffect.EATING, Math.min(50, amount*2));
	}

	private void updateInjury() {
		double ratio = (double) client.getBoostedSkillLevel(Skill.HITPOINTS) / (double) client.getRealSkillLevel(Skill.HITPOINTS);

		if(ratio <= .2) {
			statusEffects.put(StatusEffect.INJURED, 1);
		}
		else {
			statusEffects.put(StatusEffect.INJURED, 0);
		}
	}

	@Provides
	SurvivalistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SurvivalistConfig.class);
	}
}
