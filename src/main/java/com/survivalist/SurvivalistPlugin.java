package com.survivalist;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.survivalist.ui.UIButton;
import com.survivalist.ui.UILabel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.*;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
	name = "Survivalist"
)
public class SurvivalistPlugin extends Plugin
{
	public static final String CONFIG_KEY = "survivalist";
	public static final String DATA_FOLDER_NAME = "survivalist";

	private final double WARMTH_DISTANCE = 5;
	private final double LIGHT_DISTANCE = 10;
	private static final int PRAYER_TAB = 35454980;
	private static final int PRAYER_ORB = 10485777;
	private static final int QUICK_PRAYER = 10485779;
	private static final int MAGIC_TAB = 14286849;
	private static final List<Integer> VALID_FIRES = Arrays.asList(
		ObjectID.STANDING_TORCH,ObjectID.FIRE_WALL,ObjectID.FIRE_WALL_2909,ObjectID.FIRE,ObjectID.FIRE_3775,ObjectID.FLAMING_FIRE_ALTAR,
		ObjectID.FIRE_4265,ObjectID.FIRE_4266,ObjectID.FIREPLACE,ObjectID.FIREPLACE_4650,ObjectID.FIREPLACE_5165,
		ObjectID.FIRE_5249,ObjectID.STANDING_TORCH_5494,ObjectID.FIRE_5499,ObjectID.STANDING_TORCH_5881,ObjectID.FIRE_5981,ObjectID.BLUE_FIRE,ObjectID.FIREPLACE_6093,ObjectID.FIREPLACE_6094,
		ObjectID.FIREPLACE_6095,ObjectID.FIREPLACE_6096,ObjectID.STANDING_TORCH_6404,ObjectID.STANDING_TORCH_6406,ObjectID.STANDING_TORCH_6408,ObjectID.STANDING_TORCH_6410,ObjectID.STANDING_TORCH_6412,ObjectID.STANDING_TORCH_6413,ObjectID.STANDING_TORCH_6414,ObjectID.STANDING_TORCH_6415,ObjectID.STANDING_TORCH_6416,
		ObjectID.OGRE_FIRE,ObjectID.STANDING_TORCH_6896,
		ObjectID.FIREPLACE_7185,ObjectID.FIREPLACE_8712,ObjectID.FIREPLACE_9439,ObjectID.FIREPLACE_9440,ObjectID.FIREPLACE_9441,ObjectID.FIRE_9735,ObjectID.FIREPLACE_10058,ObjectID.STANDING_TORCH_10178,
		ObjectID.STANDING_TORCH_10179,ObjectID.FIRE_10433,ObjectID.TORCH,ObjectID.FIRE_10660,ObjectID.FIREPLACE_10824,ObjectID.STANDING_TORCH_11606,ObjectID.MUSHROOM_TORCH,ObjectID.MUSHROOM_TORCH_12006,
		ObjectID.DRAGONFIRE,ObjectID.FIRE_12796,ObjectID.TORCH_13200,ObjectID.TORCH_13201,ObjectID.TORCH_13202,ObjectID.TORCH_13203,ObjectID.TORCH_13204,ObjectID.TORCH_13205,ObjectID.TORCH_13206,ObjectID.TORCH_13207,ObjectID.FIRE_13337,
		ObjectID.TORCH_13341,ObjectID.FIREPIT,ObjectID.FIREPIT_WITH_HOOK,ObjectID.FIREPIT_WITH_HOOK_13530,ObjectID.FIREPIT_WITH_POT,ObjectID.FIREPIT_WITH_POT_13532,ObjectID.FIREBOX,
		ObjectID.FIREBOX_13595,ObjectID.FIREBOX_13596,ObjectID.FIRE_13881,ObjectID.FIRE_14169,ObjectID.FIRE_15156,ObjectID.STANDING_TORCH_15158,
		ObjectID.TORCH_17237,ObjectID.LIMESTONE_FIREPLACE_17325,ObjectID.FIREPLACE_17640,
		ObjectID.FIREPLACE_17641,ObjectID.FIREPLACE_17642,ObjectID.FIREPLACE_17643,ObjectID.THE_MIDDLE_OF_A_FIREPLACE,ObjectID.FIREPLACE_18039,ObjectID.CAMPFIRE,ObjectID.CAMPFIRE_19884,
		ObjectID.FIRE_20000,ObjectID.FIRE_20001,ObjectID.STANDING_TORCH_20716,ObjectID.TORCH_21465,ObjectID.FIRE_21620,ObjectID.TORCH_21667,ObjectID.FIREPLACE_21795,ObjectID.FIRE_23046,
		ObjectID.CAMP_FIRE, ObjectID.CAMPING_FIRE, ObjectID.FIREPLACE_24969, ObjectID.FIREPLACE_24970,
		ObjectID.FIRE_25155, ObjectID.FIRE_25156, ObjectID.CAMPFIRE_25374,ObjectID.FIRE_25465,
		ObjectID.FIREPLACE_26179, ObjectID.FIRE_26185, ObjectID.FIRE_26186,ObjectID.FIRE_26575, ObjectID.FIRE_26576, ObjectID.FIRE_26577, ObjectID.FIRE_26578,
		ObjectID.FIRE_28791, ObjectID.CAMPFIRE_29085,
		ObjectID.BONFIRE, ObjectID.MAGICAL_FIRE, ObjectID.FIRE_30021, ObjectID.FIREPLACE_30136, ObjectID.FIREPLACE_30137,
		ObjectID.FIREPLACE_30138, ObjectID.FIRE_PIT, ObjectID.FIRE_31798, ObjectID.TORCH_32118, ObjectID.FIRE_32297,
		ObjectID.FIRE_TRAP, ObjectID.FIRE_PIT_33310, ObjectID.FIRE_33311, ObjectID.FIRE_OF_DOMINATION,
		ObjectID.FIRE_OF_NOURISHMENT, ObjectID.FIRE_OF_ETERNAL_LIGHT, ObjectID.FIRE_OF_UNSEASONAL_WARMTH, ObjectID.FIRE_OF_DEHUMIDIFICATION, ObjectID.TORCH_34345,
		ObjectID.STANDING_TORCH_34346, ObjectID.FIRE_34682, ObjectID.ANCIENT_FIRE, ObjectID.TORCH_34856, ObjectID.FIRE_35810,
		ObjectID.FIRE_35811, ObjectID.FIRE_35812, ObjectID.FIRE_35912, ObjectID.FIRE_35913, ObjectID.MAGICAL_FIRE_37994,
		ObjectID.MAGICAL_FIRE_37995, ObjectID.MAGICAL_FIRE_37996, ObjectID.FIRE_38427, ObjectID.TORCH_38512, ObjectID.TORCH_38513,
		ObjectID.TORCH_38514, ObjectID.TORCH_38555, ObjectID.TORCH_38562, ObjectID.TORCH_40550, ObjectID.TORCH_40551, ObjectID.FIRE_40728,ObjectID.FIRE_41316, ObjectID.FIREPLACE_41654, ObjectID.FIREPLACE_42159,
		ObjectID.FIRE_43146, ObjectID.FIRE_43475,ObjectID.FIRE_ENERGY, ObjectID.FIRE_ENERGY_43779,
		ObjectID.FIRE_44021, ObjectID.FIRE_44022, ObjectID.FIRE_44023,
		ObjectID.FIRE_44024, ObjectID.FIRE_44025, ObjectID.FIRE_44026, ObjectID.FIRE_44027, ObjectID.FIRE_44028, ObjectID.TORCH_44553,
		ObjectID.FIRE_45334, ObjectID.TORCH_46180, ObjectID.CAMPFIRE_46405, ObjectID.DECORATED_FESTIVE_FIREPLACE, ObjectID.CAMPFIRE_46809
	);

	private List<Integer> WARMING_FIRES = Arrays.asList(
			ObjectID.FIRE_WALL,ObjectID.FIRE_WALL_2909,ObjectID.FIRE,ObjectID.FIRE_3775,ObjectID.FLAMING_FIRE_ALTAR,
			ObjectID.FIRE_4265,ObjectID.FIRE_4266,ObjectID.FIREPLACE,ObjectID.FIREPLACE_4650,ObjectID.FIREPLACE_5165,ObjectID.FIRE_5249,
			ObjectID.FIRE_5499,ObjectID.FIRE_5981,ObjectID.BLUE_FIRE,ObjectID.FIREPLACE_6093,ObjectID.FIREPLACE_6094,ObjectID.FIREPLACE_6095,ObjectID.FIREPLACE_6096,ObjectID.OGRE_FIRE,
			ObjectID.FIREPLACE_7185,ObjectID.FIREPLACE_8712,ObjectID.FIREPLACE_9439,ObjectID.FIREPLACE_9440,ObjectID.FIREPLACE_9441,ObjectID.FIRE_9735,ObjectID.FIREPLACE_10058,
			ObjectID.FIRE_10433,ObjectID.FIRE_10660,ObjectID.FIREPLACE_10824,ObjectID.DRAGONFIRE,ObjectID.FIRE_12796,ObjectID.FIRE_13337,
			ObjectID.FIREPIT,ObjectID.FIREPIT_WITH_HOOK,ObjectID.FIREPIT_WITH_HOOK_13530,ObjectID.FIREPIT_WITH_POT,ObjectID.FIREPIT_WITH_POT_13532,ObjectID.FIREBOX,
			ObjectID.FIREBOX_13595,ObjectID.FIREBOX_13596,ObjectID.FIRE_13881,ObjectID.FIRE_14169,ObjectID.FIRE_15156,ObjectID.LIMESTONE_FIREPLACE_17325,ObjectID.FIREPLACE_17640,
			ObjectID.FIREPLACE_17641,ObjectID.FIREPLACE_17642,ObjectID.FIREPLACE_17643,ObjectID.THE_MIDDLE_OF_A_FIREPLACE,ObjectID.FIREPLACE_18039,ObjectID.CAMPFIRE,ObjectID.CAMPFIRE_19884,
			ObjectID.FIRE_20000,ObjectID.FIRE_20001,ObjectID.FIRE_21620,ObjectID.FIREPLACE_21795,ObjectID.FIRE_23046,
			ObjectID.CAMP_FIRE, ObjectID.CAMPING_FIRE, ObjectID.FIREPLACE_24969, ObjectID.FIREPLACE_24970,
			ObjectID.FIRE_25155, ObjectID.FIRE_25156, ObjectID.CAMPFIRE_25374,ObjectID.FIRE_25465,
			ObjectID.FIREPLACE_26179, ObjectID.FIRE_26185, ObjectID.FIRE_26186, ObjectID.FIRE_26575,
			ObjectID.FIRE_26576, ObjectID.FIRE_26577, ObjectID.FIRE_26578,
			ObjectID.FIRE_28791, ObjectID.CAMPFIRE_29085,
			ObjectID.BONFIRE, ObjectID.FIRE_30021, ObjectID.FIREPLACE_30136, ObjectID.FIREPLACE_30137,
			ObjectID.FIREPLACE_30138, ObjectID.FIRE_PIT, ObjectID.FIRE_31798,  ObjectID.FIRE_32297,
			ObjectID.FIRE_TRAP, ObjectID.FIRE_PIT_33310, ObjectID.FIRE_33311, ObjectID.FIRE_OF_DOMINATION,
			ObjectID.FIRE_OF_NOURISHMENT, ObjectID.FIRE_OF_ETERNAL_LIGHT, ObjectID.FIRE_OF_UNSEASONAL_WARMTH, ObjectID.FIRE_OF_DEHUMIDIFICATION,
			ObjectID.FIRE_34682, ObjectID.ANCIENT_FIRE,  ObjectID.FIRE_35810,
			ObjectID.FIRE_35811, ObjectID.FIRE_35812, ObjectID.FIRE_35912, ObjectID.FIRE_35913, ObjectID.MAGICAL_FIRE_37994,
			ObjectID.MAGICAL_FIRE_37995, ObjectID.MAGICAL_FIRE_37996, ObjectID.FIRE_38427, ObjectID.FIRE_40728,
			ObjectID.FIRE_41316, ObjectID.FIREPLACE_41654, ObjectID.FIREPLACE_42159,ObjectID.FIRE_43146, ObjectID.FIRE_43475,
			ObjectID.FIRE_44021, ObjectID.FIRE_44022, ObjectID.FIRE_44023,
			ObjectID.FIRE_44024, ObjectID.FIRE_44025, ObjectID.FIRE_44026, ObjectID.FIRE_44027, ObjectID.FIRE_44028,
			ObjectID.FIRE_45334,  ObjectID.CAMPFIRE_46405, ObjectID.CAMPFIRE_46809
	);

	private List<Integer> VALID_LIGHT_SOURCE = Arrays.asList(
		ItemID.LIT_CANDLE, ItemID.LIT_BLACK_CANDLE, ItemID.LIT_TORCH, ItemID.BULLSEYE_LANTERN_4550, ItemID.BRUMA_TORCH,
		ItemID.OIL_LANTERN_4539, ItemID.CANDLE_LANTERN_4531, ItemID.OIL_LAMP, ItemID.OIL_LAMP_4524,
		ItemID.SAPPHIRE_LANTERN_4702, ItemID.EMERALD_LANTERN_9065, ItemID.FIREMAKING_CAPE, ItemID.MINING_HELMET_5014,
		ItemID.FIREMAKING_CAPET
	);

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
	private SpriteManager spriteManager;

	@Inject
	private SurvivalistConfig config;

	@Inject
	private SurvivalistTileOverlay tileOverlay;

	private LifePointsBarOverlay lifePointsBarOverlay;

	private Widget overlay;
	private UILabel prayerLocked;
	private UILabel magicLocked;
	private UIButton quickPrayer;

	@Getter
	private UnlockData unlockData;
	private File playerFile;

	private final HashMap<StatusEffect, StatusEffectInfobox> statusEffectInfoboxs = new HashMap<>();

	private final ItemStatChanges itemStats = new ItemStatChanges();

	private SurvivalistOverlay survivalistOverlay;
	private SurvivalistItemOverlay itemOverlay;

	private int usingID = -1;

	private Map<Skill, Integer> skillLevels = new HashMap<>();
	private int prayerPoints = -1;

	private Map<WorldPoint, Lootbeam> lootbeams = new HashMap<>();
	private Map<WorldPoint, Scroll> scrolls = new HashMap<>();

	private int ticks = 1;
	private boolean sentGameOver = false;
	private boolean nearFire = false;

	public List<Tile> nearbyFires = new ArrayList<>();
	public List<Tile> nearbyWarmingFires = new ArrayList<>();
	public Tile closestFire;
	public Tile closestWarmingFire;

	@Override
	protected void startUp() throws Exception
	{
		survivalistOverlay = new SurvivalistOverlay(this, config);
		itemOverlay = new SurvivalistItemOverlay(itemManager, this);
		lifePointsBarOverlay = new LifePointsBarOverlay(client, this, spriteManager);
		overlayManager.add(itemOverlay);
		overlayManager.add(survivalistOverlay);
		overlayManager.add(lifePointsBarOverlay);
		overlayManager.add(tileOverlay);
		if(client.getGameState() == GameState.LOGGED_IN) {
			if(this.overlay != null) this.overlay.setHidden(false);
			else clientThread.invokeLater(this::createNightTimeOverlay);
			setupPlayerFile();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		savePlayerData();
		for(StatusEffectInfobox effectInfobox : statusEffectInfoboxs.values()) {
			infoBoxManager.removeInfoBox(effectInfobox);
		}
		statusEffectInfoboxs.clear();

		overlayManager.remove(survivalistOverlay);
		overlayManager.remove(itemOverlay);
		overlayManager.remove(lifePointsBarOverlay);
		showPrayers();
		showMagic();
		itemOverlay = null;
		survivalistOverlay = null;
		lifePointsBarOverlay = null;
		if(this.overlay != null) this.overlay.setHidden(true);

		for(WorldPoint point : scrolls.keySet()) {
			removeGroundItem(point);
			removeLootbeam(point);
		}

		unlockData = null;
	}

	/**
	 * Sets up the playerFile variable, and makes the player file if needed.
	 */
	private void setupPlayerFile() {
		unlockData = new UnlockData();

		for(StatusEffect effect : StatusEffect.values()) {
			unlockData.getStatusEffects().put(effect, 0);
			if(statusEffectInfoboxs.get(effect) == null) {
				statusEffectInfoboxs.put(effect, new StatusEffectInfobox(effect, itemManager.getImage(effect.getItemIconID()), this));
				infoBoxManager.addInfoBox(statusEffectInfoboxs.get(effect));
			}
		}

		File playerFolder = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
		if (!playerFolder.exists()) {
			playerFolder.mkdirs();
		}
		playerFile = new File(playerFolder, client.getAccountHash() + ".txt");
		if (!playerFile.exists()) {
			try {
				playerFile.createNewFile();
				unlockDefaults();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			loadPlayerData();
		}
	}

	private void loadPlayerData() {
		unlockData = null;
		try {
			String json = new Scanner(playerFile).useDelimiter("\\Z").next();
			unlockData = GSON.fromJson(json, new TypeToken<UnlockData>() {}.getType());

			if(!unlockData.isMagicUnlocked() && magicLocked != null) hideMagic();
			if(!unlockData.isPrayerUnlocked() && prayerLocked != null) hideMagic();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void savePlayerData() {
		try {
			PrintWriter w = new PrintWriter(playerFile);
			String json = GSON.toJson(unlockData);
			w.println(json);
			w.close();
			log.debug("Saving player data to "+playerFile.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unlockDefaults() {
		unlockData.setDefaults();
		savePlayerData();
	}


	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == 548) {
			createNightTimeOverlay();
		}
		// Prayer menu loaded
		else if(e.getGroupId() == 160 && quickPrayer == null) {
			Widget prayerOrb = client.getWidget(PRAYER_ORB);
			Widget orbWidget = prayerOrb.createChild(-1, WidgetType.GRAPHIC);

			quickPrayer = new UIButton(orbWidget);
			quickPrayer.setSize(prayerOrb.getWidth(), prayerOrb.getHeight());
			quickPrayer.setVisibility(false);
		}
		else if(e.getGroupId() == 541) {
			Widget container = client.getWidget(35454976);
			Widget prayerLabel = container.createChild(-1, WidgetType.TEXT);
			prayerLocked = new UILabel(prayerLabel);
			prayerLocked.setText("You must unlock the Prayer skill to use your prayers.");
			prayerLocked.setColour(ColorScheme.BRAND_ORANGE.getRGB());
			prayerLocked.setSize(150, 75);
			prayerLocked.setPosition(getCenterX(container, 150), getCenterY(container, 75));
			prayerLocked.setVisibility(false);

			if(unlockData.isPrayerUnlocked()) return;
			hidePrayers();
		}
		// Magic tab
		else if(e.getGroupId() == 218) {
			Widget container = client.getWidget(14286848);
			Widget magicLabel = container.createChild(-1, WidgetType.TEXT);
			magicLocked = new UILabel(magicLabel);
			magicLocked.setText("You must unlock the Magic skill to use your spells.");
			magicLocked.setColour(ColorScheme.BRAND_ORANGE.getRGB());
			magicLocked.setSize(150, 75);
			magicLocked.setPosition(getCenterX(container, 150), getCenterY(container, 75));
			magicLocked.setVisibility(false);

			if(unlockData.isMagicUnlocked()) return;
			hideMagic();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			if(unlockData != null) savePlayerData();
			skillLevels = new HashMap<>();
			unlockData = null;

			statusEffectInfoboxs.keySet().forEach(s -> infoBoxManager.removeInfoBox(statusEffectInfoboxs.get(s)));
			statusEffectInfoboxs.clear();
		}
		else if(gameStateChanged.getGameState().equals(GameState.LOGGED_IN)) {
			if(unlockData == null) setupPlayerFile();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		if(config.pause()) return;

		decrementStatusEffects();

		unlockData.updateGameTime();
		TimeOfDay tod = TimeOfDay.getTimeOfDay(unlockData.getGameTime());

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

		int fireDistance = checkForFire();

		if(tod == TimeOfDay.NIGHT) {
			unlockData.getStatusEffects().put(StatusEffect.COLD, 1);
		}

		if(fireDistance > -1) {
			nearFire = true;

			if(closestWarmingFire != null) {
				unlockData.getStatusEffects().put(StatusEffect.WARM, 1);
				unlockData.getStatusEffects().put(StatusEffect.COLD, 0);
			}

			if (tod == TimeOfDay.NIGHT) {
				double brightness = 110 - (fireDistance / LIGHT_DISTANCE) * 100;
				if (tod == TimeOfDay.NIGHT) this.overlay.setOpacity((int) (TimeOfDay.NIGHT.getDarkness() + brightness));
			}
		}
		else if(nearFire) {
			if(tod == TimeOfDay.NIGHT) this.overlay.setOpacity(TimeOfDay.NIGHT.getDarkness());
			nearFire = false;
		}


		checkWeight();
		unlockData.updateHunger();
		unlockData.updateInjury((double) client.getBoostedSkillLevel(Skill.HITPOINTS) / (double) client.getRealSkillLevel(Skill.HITPOINTS));
		unlockData.updateLifePoints();

		if(unlockData.getLifePoints() == 0 && !sentGameOver) {
			clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.CONSOLE, "", "Your life points have hit 0. You have failed to survive. Game over.", ""));
			sentGameOver = true;
		}

		if(ticks % 100 == 0 && unlockData != null) {
			savePlayerData();
		}
		ticks++;
	}

	@Subscribe
	public void onActorDeath(ActorDeath e) {
		if(e.getActor().equals(client.getLocalPlayer())) {
			clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.CONSOLE, "", "You have failed to survive. Game over.", ""));
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged e) {
		if(skillLevels.get(e.getSkill()) != null && !config.pause()) {
			if(e.getLevel() > skillLevels.get(e.getSkill())) {
				unlockData.getStatusEffects().put(StatusEffect.PROUD, 100);
			}
		}

		if(e.getSkill() == Skill.CRAFTING) {
			int level = client.getRealSkillLevel(Skill.CRAFTING);
			if(skillLevels.get(Skill.CRAFTING) != null) {
				if(level == 15) {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can now craft a bed to sleep at night by using 2 logs and 2 wool on each other.", ""));
				}

			}
		}
		if(e.getSkill() == Skill.PRAYER && !config.pause()) {
			int level = client.getRealSkillLevel(Skill.PRAYER);
			int boosted = client.getBoostedSkillLevel(Skill.PRAYER);

			if(skillLevels.get(Skill.PRAYER) != null && boosted >= prayerPoints) {
				if(level >= 15) {
					unlockData.getStatusEffects().put(StatusEffect.PIOUS, 2*level);
				}
			}

			prayerPoints = boosted;
		}

		skillLevels.put(e.getSkill(), e.getLevel());
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e) {
		if(e.getContainerId() == InventoryID.INVENTORY.getId() || e.getContainerId() == InventoryID.EQUIPMENT.getId() && !config.pause()) {
			checkWeight();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e) {
		if(config.pause()) return;

		if(e.getOption().startsWith("Walk here")) {
			Tile tile = client.getSelectedSceneTile();
			if(tile == null) return;

			Scroll scroll = scrolls.get(tile.getWorldLocation());
			if(scroll == null) return;

			Color color = getLootbeamColor(scroll.getSkill());
			String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

			client.createMenuEntry(-1)
					.setTarget("<col="+hex+">Ancient Scroll ("+scroll.getSkill().getName()+")</col>")
					.setOption("Take")
					.setType(MenuAction.GROUND_ITEM_FIRST_OPTION)
					.onClick(ev -> pickUpScroll(tile.getWorldLocation()));
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if(config.pause()) return;

		if(e.getMenuOption().equals("Eat") && unlockData.getStatusEffects().get(StatusEffect.EATING) == 0) {
			unlockData.addHunger(itemStats.get(e.getItemId()));
		}
		else if(e.getMenuOption().equals("Eat")) {
			e.consume();
		}
		else if(e.getMenuOption().equals("Equip") || e.getMenuOption().equals("Wear") || e.getMenuOption().equals("Wield")) {
			if(!isItemUnlocked(e.getItemId())) e.consume();
		}
		else if(e.getMenuOption().equals("Use")) {
			checkForBed(e.getItemId());
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived e) {
		checkForAgeCompletion(e.getNpc().getId());
		if(client.getVarbitValue(Varbits.IN_WILDERNESS) == 1 && !config.pause()) {
			double roll = Math.random()*1000;
			if(roll < e.getNpc().getCombatLevel()) {
				addGroundItem(e.getNpc().getLocalLocation(), Skill.PRAYER);
			}
		}
	}

	private void pickUpScroll(WorldPoint point) {
		Skill skill = scrolls.get(point).getSkill();

		if(skill == Skill.MAGIC && !unlockData.isMagicUnlocked()) {
			unlockData.setMagicUnlocked(true);
			showMagic();
		}
		else if(skill == Skill.PRAYER && !unlockData.isPrayerUnlocked()) {
			unlockData.setPrayerUnlocked(true);
			showPrayers();
		}

		client.playSoundEffect(SoundEffectID.ITEM_PICKUP);
		removeGroundItem(point);
		removeLootbeam(point);

		savePlayerData();
	}

	private void createNightTimeOverlay() {
		Widget parent = client.getWidget(548, 26);
		overlay = parent.createChild(WidgetType.RECTANGLE);
		overlay.setFilled(true);
		overlay.setOpacity(TimeOfDay.getTimeOfDay(unlockData.getGameTime()).getDarkness());
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

		int closest = hasLightSource() ? 0 : -1;
		int closestWarming = (int) WARMTH_DISTANCE+1;
		nearbyFires.clear();
		nearbyWarmingFires.clear();
		closestFire = null;
		closestWarmingFire = null;

		for(int x=0;x<Constants.SCENE_SIZE;++x) {
			for(int y=0;y<Constants.SCENE_SIZE;++y) {
				Tile tile = tiles[plane][x][y];

				if(tile == null) continue;

				for(GameObject go : tile.getGameObjects()) {
					if (go != null && VALID_FIRES.contains(go.getId())) {
						boolean isWarming = WARMING_FIRES.contains(go.getId());
						nearbyFires.add(tile);
						if(isWarming) nearbyWarmingFires.add(tile);
						int distance = client.getLocalPlayer().getWorldLocation().distanceTo(tile.getWorldLocation());

						if(distance <= LIGHT_DISTANCE) {
							if(distance < closest || closest == -1){
								closest = distance;
								closestFire = tile;
							}

							if(isWarming && distance < closestWarming) {
								closestWarming = distance;
								closestWarmingFire = tile;
							}
						}
					}
				}
			}
		}

		return closest;
	}

	private boolean hasLightSource() {
		if (client.getItemContainer(InventoryID.INVENTORY) != null) {
			for (Item item : client.getItemContainer(InventoryID.INVENTORY).getItems()) {
				if (VALID_LIGHT_SOURCE.contains(item.getId())) return true;
			}
		}

		if (client.getItemContainer(InventoryID.EQUIPMENT) != null) {
			for (Item item : client.getItemContainer(InventoryID.EQUIPMENT).getItems()) {
				if (VALID_LIGHT_SOURCE.contains(item.getId())) return true;
			}
		}

		return false;
	}

	private void checkWeight() {
		int weight = client.getWeight();
		int agilityLevel = client.getRealSkillLevel(Skill.AGILITY);
		int maxWeight = 10 + (int) Math.floor(agilityLevel/2);

		if(weight > maxWeight) {
			unlockData.getStatusEffects().put(StatusEffect.OVERWEIGHT, 1);
		}
	}

	private void checkForBed(int itemID) {
		String name = client.getItemDefinition(itemID).getName();
		TimeOfDay tod = TimeOfDay.getTimeOfDay(unlockData.getGameTime());

		if((itemID == ItemID.WOOL || name.toLowerCase().contains("logs")) && client.getRealSkillLevel(Skill.CRAFTING) >= 15) {
			if(usingID == -1) {
				usingID = itemID;
				return;
			}
			else {
				usingID = -1;

				if(tod != TimeOfDay.DUSK && tod != TimeOfDay.NIGHT) {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can't sleep at this hour.", ""));
					return;
				}

				ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);

				int numLogs = 0;
				int numWool = 0;

				for(Item item : inv.getItems()) {
					if(item.getId() == ItemID.WOOL) numWool++;
					else if(item.getId() == ItemID.LOGS) numLogs++;
				}

				checkForFire();
				if(numLogs < 2 || numWool < 2) {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You need 2 logs of the same type and 2 wool to sleep.", ""));
				}
				else if(closestWarmingFire != null){
					unlockData.sleep();
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You craft a makeshift bed and fall fast asleep.", ""));
					unlockData.getStatusEffects().put(StatusEffect.RESTED, 100);
				}
				else {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You must sleep near a warm fire.", ""));
				}
			}
		}
	}

	private void checkForAgeCompletion(int npcID) {
		if(this.unlockData.getAge().getBossID() == npcID) {
			Age lastAge = unlockData.getAge();
			clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You have completed the "+lastAge.getName()+"!", ""));
			this.unlockData.setAge(Age.getNextAge(lastAge));

			if(this.unlockData.getAge() == Age.MAGIC_AGE) {
				clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "The secrets of your forgotten skills lie to the north...", ""));
			}
			if(lastAge == Age.DRAGON_AGE) {
				clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You win?", ""));
			}
		}
	}

	private void decrementStatusEffects() {
		for(StatusEffect effect : unlockData.getStatusEffects().keySet()) {
			if(unlockData.getStatusEffects().get(effect) > 0) {
				unlockData.getStatusEffects().put(effect, unlockData.getStatusEffects().get(effect)-1);
			}
		}
	}

	private void hidePrayers() {
		client.getWidget(QUICK_PRAYER).setHidden(true);
		client.getWidget(PRAYER_TAB).setHidden(true);
		prayerLocked.setVisibility(true);
		quickPrayer.setVisibility(true);
	}

	private void showPrayers() {
		client.getWidget(QUICK_PRAYER).setHidden(false);
		client.getWidget(PRAYER_TAB).setHidden(false);
		prayerLocked.setVisibility(false);
		quickPrayer.setVisibility(false);
	}

	private void hideMagic() {
		client.getWidget(MAGIC_TAB).setHidden(true);
		magicLocked.setVisibility(true);
	}

	private void showMagic() {
		client.getWidget(MAGIC_TAB).setHidden(false);
		magicLocked.setVisibility(false);
	}

	boolean isItemUnlocked(int itemID) {
		List<String> prefixes = Age.getIllegalItemPrefixes(unlockData.getAge());

		ItemComposition itemComposition = client.getItemDefinition(itemID);
		String name = itemComposition.getName().toLowerCase();

		boolean valid = false;
		for(String action : itemComposition.getInventoryActions()) {
			if(action == null) continue;

			if (action.equals("Wield") || action.equals("Equip") || action.equals("Wear")) {
				valid = true;
				break;
			}
		}

		if(!valid || name.contains("cape")) return true;

		for(String prefix : prefixes) {
			if(name.startsWith(prefix.toLowerCase())) return false;
		}

		return true;
	}

	String getAgeBossName() {
		if(unlockData == null) return null;

		return client.getNpcDefinition(unlockData.getAge().getBossID()).getName();
	}

	private void addLootbeam(WorldPoint worldPoint, Color color)
	{
		Lootbeam lootbeam = lootbeams.get(worldPoint);
		if (lootbeam == null)
		{
			lootbeam = new Lootbeam(client, clientThread, worldPoint, color, Lootbeam.Style.MODERN);
			lootbeams.put(worldPoint, lootbeam);
		}
		else
		{
			lootbeam.setColor(color);
			lootbeam.setStyle(Lootbeam.Style.MODERN);
		}
	}

	private void removeLootbeam(WorldPoint worldPoint)
	{
		Lootbeam lootbeam = lootbeams.remove(worldPoint);
		if (lootbeam != null)
		{
			lootbeam.remove();
		}
	}

	private void addGroundItem(LocalPoint point, Skill skill) {
		WorldPoint loc = WorldPoint.fromLocal(client, point);
		Scroll scroll = new Scroll(client, point, Skill.PRAYER);

		scrolls.put(loc, scroll);
		addLootbeam(loc, getLootbeamColor(Skill.PRAYER));
	}

	private void removeGroundItem(WorldPoint point) {
		Scroll item = scrolls.remove(point);
		if(item != null) {
			item.setActive(false);
		}
	}

	private void handleLootbeams() {
		HashMap<WorldPoint, Lootbeam> beamsToDelete = new HashMap<>(lootbeams);
		beamsToDelete.keySet().forEach(this::removeLootbeam);

		scrolls.keySet().forEach(point -> {
			Skill skill = scrolls.get(point).getSkill();
			this.addLootbeam(point, getLootbeamColor(skill));
		});
	}

	private Color getLootbeamColor(Skill skill) {
		return skill.equals(Skill.MAGIC) ? Color.CYAN : Color.YELLOW;
	}

	public int getLifePointsRestoreValue() {
		int value = 0;

		for(StatusEffect effect : unlockData.getStatusEffects().keySet()) {
			if(unlockData.getStatusEffects().get(effect) > 0) {
				value += effect.getLpPerTick();
			}
		}

		return value;
	}

	public static int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	public static int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	@Provides
	SurvivalistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SurvivalistConfig.class);
	}
}
