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

	private final int FIRE_OBJECT_ID = 26185;
	private final double WARMTH_DISTANCE = 10;
	private static final int PRAYER_TAB = 35454980;
	private static final int PRAYER_ORB = 10485777;
	private static final int QUICK_PRAYER = 10485779;
	private static final int MAGIC_TAB = 14286849;

	private final List<String> PREFIX_WHITELIST = Arrays.asList("necklace", "ring", "amulet", "tiara", "bracelet", "boots of lightness", "graceful");

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

	@Override
	protected void startUp() throws Exception
	{
		survivalistOverlay = new SurvivalistOverlay(this, config);
		itemOverlay = new SurvivalistItemOverlay(itemManager, this);
		lifePointsBarOverlay = new LifePointsBarOverlay(client, this, spriteManager);
		overlayManager.add(itemOverlay);
		overlayManager.add(survivalistOverlay);
		overlayManager.add(lifePointsBarOverlay);
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
			statusEffectInfoboxs.put(effect, new StatusEffectInfobox(effect, itemManager.getImage(effect.getItemIconID()), this));
			infoBoxManager.addInfoBox(statusEffectInfoboxs.get(effect));
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
		}
		else if(gameStateChanged.getGameState().equals(GameState.LOGGED_IN)) {
			if(unlockData == null) setupPlayerFile();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e) {
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

		if(fireDistance >= 0) {
			unlockData.getStatusEffects().put(StatusEffect.WARM, 1);
			unlockData.getStatusEffects().put(StatusEffect.COLD, 0);
			double brightness = 110 - (fireDistance / WARMTH_DISTANCE) * 100;
			if(tod == TimeOfDay.NIGHT) this.overlay.setOpacity((int) (TimeOfDay.NIGHT.getDarkness() + brightness));
		}
		else if(tod == TimeOfDay.NIGHT) {
			unlockData.getStatusEffects().put(StatusEffect.COLD, 1);
		}
		else {
			unlockData.getStatusEffects().put(StatusEffect.COLD, 0);
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
		if(skillLevels.get(e.getSkill()) != null) {
			if(e.getLevel() > skillLevels.get(e.getSkill())) {
				unlockData.getStatusEffects().put(StatusEffect.PROUD, 200);
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
		if(e.getSkill() == Skill.PRAYER) {
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
		if(e.getContainerId() == InventoryID.INVENTORY.getId() || e.getContainerId() == InventoryID.EQUIPMENT.getId()) {
			checkWeight();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e) {
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
		if(client.getVarbitValue(Varbits.IN_WILDERNESS) == 1) {
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

		for(int x=0;x<Constants.SCENE_SIZE;++x) {
			for(int y=0;y<Constants.SCENE_SIZE;++y) {
				Tile tile = tiles[plane][x][y];

				if(tile == null) continue;

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

				if(numLogs < 2 || numWool < 2) {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You need 2 logs of the same type and 2 wool to sleep.", ""));
				}
				else if(checkForFire() > -1){
					unlockData.sleep();
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You craft a makeshift bed and fall fast asleep.", ""));
					unlockData.getStatusEffects().put(StatusEffect.RESTED, 100);
				}
				else {
					clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You must sleep near a fire.", ""));
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

		if(!valid) return true;

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
