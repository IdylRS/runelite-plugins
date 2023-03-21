package com.idyl.popups;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.text.WordUtils;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "More Log Popups"
)
public class MoreLogPopupsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "MoreLogPopups";
	public static final String UNLOCK_CONFIG_KEY = "unlockedItems";

	// Chest loot handling
	private static final String CHEST_LOOTED_MESSAGE = "You find some treasure in the chest!";
	private static final Pattern ROGUES_CHEST_PATTERN = Pattern.compile("You find (a|some)([a-z\\s]*) inside.");
	private static final Pattern LARRAN_LOOTED_PATTERN = Pattern.compile("You have opened Larran's (big|small) chest .*");
	// Used by Stone Chest, Isle of Souls chest, Dark Chest
	private static final String OTHER_CHEST_LOOTED_MESSAGE = "You steal some loot from the chest.";
	private static final String DORGESH_KAAN_CHEST_LOOTED_MESSAGE = "You find treasure inside!";
	private static final String GRUBBY_CHEST_LOOTED_MESSAGE = "You have opened the Grubby Chest";
	private static final Pattern HAM_CHEST_LOOTED_PATTERN = Pattern.compile("Your (?<key>[a-z]+) key breaks in the lock.*");
	private static final int HAM_STOREROOM_REGION = 10321;
	private static final Map<Integer, String> CHEST_EVENT_TYPES = new ImmutableMap.Builder<Integer, String>().
			put(5179, "Brimstone Chest").
			put(11573, "Crystal Chest").
			put(12093, "Larran's big chest").
			put(12127, "The Gauntlet").
			put(13113, "Larran's small chest").
			put(13151, "Elven Crystal Chest").
			put(5277, "Stone chest").
			put(10835, "Dorgesh-Kaan Chest").
			put(10834, "Dorgesh-Kaan Chest").
			put(7323, "Grubby Chest").
			put(8593, "Isle of Souls Chest").
			put(7827, "Dark Chest").
			put(13117, "Rogues' Chest").
			build();

	private static final Map<Integer, String> SHADE_CHEST_OBJECTS = new ImmutableMap.Builder<Integer, String>().
			put(ObjectID.BRONZE_CHEST, "Bronze key red").
			put(ObjectID.BRONZE_CHEST_4112, "Bronze key brown").
			put(ObjectID.BRONZE_CHEST_4113, "Bronze key crimson").
			put(ObjectID.BRONZE_CHEST_4114, "Bronze key black").
			put(ObjectID.BRONZE_CHEST_4115, "Bronze key purple").
			put(ObjectID.STEEL_CHEST, "Steel key red").
			put(ObjectID.STEEL_CHEST_4117, "Steel key brown").
			put(ObjectID.STEEL_CHEST_4118, "Steel key crimson").
			put(ObjectID.STEEL_CHEST_4119, "Steel key black").
			put(ObjectID.STEEL_CHEST_4120, "Steel key purple").
			put(ObjectID.BLACK_CHEST, "Black key red").
			put(ObjectID.BLACK_CHEST_4122, "Black key brown").
			put(ObjectID.BLACK_CHEST_4123, "Black key crimson").
			put(ObjectID.BLACK_CHEST_4124, "Black key black").
			put(ObjectID.BLACK_CHEST_4125, "Black key purple").
			put(ObjectID.SILVER_CHEST, "Silver key red").
			put(ObjectID.SILVER_CHEST_4127, "Silver key brown").
			put(ObjectID.SILVER_CHEST_4128, "Silver key crimson").
			put(ObjectID.SILVER_CHEST_4129, "Silver key black").
			put(ObjectID.SILVER_CHEST_4130, "Silver key purple").
			put(ObjectID.GOLD_CHEST, "Gold key red").
			put(ObjectID.GOLD_CHEST_41213, "Gold key brown").
			put(ObjectID.GOLD_CHEST_41214, "Gold key crimson").
			put(ObjectID.GOLD_CHEST_41215, "Gold key black").
			put(ObjectID.GOLD_CHEST_41216, "Gold key purple").
			build();

	private static final String COFFIN_LOOTED_MESSAGE = "You push the coffin lid aside.";
	private static final Set<Integer> HALLOWED_SEPULCHRE_MAP_REGIONS = ImmutableSet.of(8797, 10077, 9308, 10074, 9050); // one map region per floor

	private static final Pattern PICKPOCKET_REGEX = Pattern.compile("You pick (the )?(?<target>.+)'s? pocket.*");

	private static final String TEMPOROSS_LOOT_STRING = "You found some loot: ";
	private static final int TEMPOROSS_REGION = 12588;

	// Guardians of the Rift
	private static final String GUARDIANS_OF_THE_RIFT_LOOT_STRING = "You found some loot: ";
	private static final int GUARDIANS_OF_THE_RIFT_REGION = 14484;

	// Implings
	private static final Set<Integer> IMPLING_JARS = ImmutableSet.of(
			ItemID.BABY_IMPLING_JAR,
			ItemID.YOUNG_IMPLING_JAR,
			ItemID.GOURMET_IMPLING_JAR,
			ItemID.EARTH_IMPLING_JAR,
			ItemID.ESSENCE_IMPLING_JAR,
			ItemID.ECLECTIC_IMPLING_JAR,
			ItemID.NATURE_IMPLING_JAR,
			ItemID.MAGPIE_IMPLING_JAR,
			ItemID.NINJA_IMPLING_JAR,
			ItemID.CRYSTAL_IMPLING_JAR,
			ItemID.DRAGON_IMPLING_JAR,
			ItemID.LUCKY_IMPLING_JAR
	);
	private static final String IMPLING_CATCH_MESSAGE = "You manage to catch the impling and acquire some loot.";

	private static final List<Integer> CONTAINERS = Arrays.asList(InventoryID.BARROWS_REWARD.getId(), InventoryID.CHAMBERS_OF_XERIC_CHEST.getId(), InventoryID.FISHING_TRAWLER_REWARD.getId(),
			InventoryID.DRIFT_NET_FISHING_REWARD.getId(), InventoryID.KINGDOM_OF_MISCELLANIA.getId(), InventoryID.THEATRE_OF_BLOOD_CHEST.getId(), InventoryID.TOA_REWARD_CHEST.getId(), InventoryID.WILDERNESS_LOOT_CHEST.getId());

	private final int ANIM_DURATION = 500;

	@Inject
	private Client client;

	@Inject
	private MoreLogPopupsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	Gson gson;

	@Inject
	SpriteManager spriteManager;

	private SpriteDefinition[] spriteDefinitions = new SpriteDefinition[]{new SpriteDefinition(-13037, "bait.png")};

	private Widget widget;
	private Widget textWidget;

	private int animFrame = 0;

	private List<String> items = new ArrayList<>();
	private List<String> unlockedItems = new ArrayList<>();
	private List<String> popupQueue = new ArrayList<>();
	private String currentPopup = null;

	private Multiset<Integer> inventorySnapshot;

	@Override
	protected void startUp() throws Exception
	{
		this.spriteDefinitions = loadDefinitionResource(SpriteDefinition[].class, "SpriteDef.json", gson);
		this.spriteManager.addSpriteOverrides(spriteDefinitions);

		trackItems();
		updateUnlockedItems();
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.spriteManager.removeSpriteOverrides(spriteDefinitions);
	}

	private <T> T loadDefinitionResource(Class<T> classType, String resource, Gson gson) {
		// Load the resource as a stream and wrap it in a reader
		InputStream resourceStream = classType.getResourceAsStream(resource);
		assert resourceStream != null;
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);

		// Load the objects from the JSON file
		return gson.fromJson(definitionReader, classType);
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived e) {
		for(ItemStack i : e.getItems()) {
			String name = client.getItemDefinition(i.getId()).getName().toLowerCase();
			if(items.contains(name) && !(unlockedItems.contains(name) && config.showOnce())) addToQueue(name);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e) {
		if(CONTAINERS.contains(e.getContainerId())) {
			for(Item i : e.getItemContainer().getItems()) {
				String name = client.getItemDefinition(i.getId()).getName().toLowerCase();
				if(items.contains(name) && !(unlockedItems.contains(name) && config.showOnce())) addToQueue(name);
			}
		}
		else if(e.getContainerId() == InventoryID.INVENTORY.getId() && inventorySnapshot != null) {
			final ItemContainer inventoryContainer = e.getItemContainer();
			Multiset<Integer> currentInventory = HashMultiset.create();
			Arrays.stream(inventoryContainer.getItems())
					.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

			final Multiset<Integer> diff = Multisets.difference(currentInventory, inventorySnapshot);
			final List<ItemStack> itemStacks = diff.entrySet().stream()
					.map(i -> new ItemStack(i.getElement(), i.getCount(), client.getLocalPlayer().getLocalLocation()))
					.collect(Collectors.toList());

			for(ItemStack stack : itemStacks) {
				String name = client.getItemDefinition(stack.getId()).getName().toLowerCase();
				if(items.contains(name) && !(unlockedItems.contains(name) && config.showOnce())) addToQueue(name);
			}

			inventorySnapshot = null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		final String message = event.getMessage();

		if (message.equals(CHEST_LOOTED_MESSAGE) || message.equals(OTHER_CHEST_LOOTED_MESSAGE)
				|| message.equals(DORGESH_KAAN_CHEST_LOOTED_MESSAGE) || message.startsWith(GRUBBY_CHEST_LOOTED_MESSAGE)
				|| LARRAN_LOOTED_PATTERN.matcher(message).matches() || ROGUES_CHEST_PATTERN.matcher(message).matches())
		{
			final int regionID = client.getLocalPlayer().getWorldLocation().getRegionID();
			if (!CHEST_EVENT_TYPES.containsKey(regionID))
			{
				return;
			}

			createSnapshot();
			return;
		}

		if (message.equals(COFFIN_LOOTED_MESSAGE) &&
				isPlayerWithinMapRegion(HALLOWED_SEPULCHRE_MAP_REGIONS))
		{
			createSnapshot();
			return;
		}

		final int regionID = client.getLocalPlayer().getWorldLocation().getRegionID();

		final Matcher hamStoreroomMatcher = HAM_CHEST_LOOTED_PATTERN.matcher(message);
		if (hamStoreroomMatcher.matches() && regionID == HAM_STOREROOM_REGION)
		{
			createSnapshot();
			return;
		}

		final Matcher pickpocketMatcher = PICKPOCKET_REGEX.matcher(message);
		if (pickpocketMatcher.matches())
		{
			createSnapshot();
			return;
		}

		if (regionID == TEMPOROSS_REGION && message.startsWith(TEMPOROSS_LOOT_STRING))
		{
			createSnapshot();
			return;
		}

		if (regionID == GUARDIANS_OF_THE_RIFT_REGION && message.startsWith(GUARDIANS_OF_THE_RIFT_LOOT_STRING))
		{
			createSnapshot();
			return;
		}

		if (message.equals(IMPLING_CATCH_MESSAGE))
		{
			createSnapshot();
			return;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (isObjectOp(event.getMenuAction()) && event.getMenuOption().equals("Open") && SHADE_CHEST_OBJECTS.containsKey(event.getId())) {
			createSnapshot();
		}
		else if (event.getMenuOption().equals("Loot") && IMPLING_JARS.contains(event.getItemId()))
		{
			createSnapshot();
		}
	}

	private static boolean isObjectOp(MenuAction menuAction)
	{
		final int id = menuAction.getId();
		return (id >= MenuAction.GAME_OBJECT_FIRST_OPTION.getId() && id <= MenuAction.GAME_OBJECT_FOURTH_OPTION.getId())
				|| id == MenuAction.GAME_OBJECT_FIFTH_OPTION.getId();
	}

	@Subscribe
	public void onClientTick(ClientTick e) {
		if(widget == null || textWidget == null) return;

		if(popupQueue.size() > 0) {
			if (currentPopup == null) {
				String name = popupQueue.remove(0);
				if(name.length() == 0) return;

				currentPopup = WordUtils.capitalize(name);
				textWidget.setText(currentPopup);

				addUnlockedItem(name);
			}
		}

		if(currentPopup != null && animFrame < ANIM_DURATION) {
			animFrame++;
			widget.setHidden(false);
			textWidget.setHidden(false);

			if(animFrame <= ANIM_DURATION-100) {
				if(widget.getRelativeY() != 0) {
					int y = Math.min(widget.getRelativeY()+3, 0);
					widget.setPos(0, y);
					textWidget.setPos(0, y+15);
				}
			}
			else {
				if(widget.getRelativeY() != -200) {
					int y = Math.max(widget.getRelativeY()-3, -200);
					widget.setPos(0, y);
					textWidget.setPos(0, y+15);
				}
			}

			widget.revalidate();
			textWidget.revalidate();
		}
		else if(currentPopup != null) {
			widget.setHidden(true);
			textWidget.setHidden(true);
			animFrame = 0;
			currentPopup = null;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == 548) {
			widget = client.getWidget(35913770).createChild(-1, WidgetType.GRAPHIC);
			textWidget = client.getWidget(35913770).createChild(-1, WidgetType.TEXT);
			widget.setSize(178, 100);
			widget.setPos(0, -200);
			widget.setSpriteId(-13037);
			widget.setHidden(true);
			textWidget.setHidden(true);
			textWidget.setSize(178, 100);
			textWidget.setFontId(494);
			textWidget.setTextShadowed(true);
			textWidget.setTextColor(Color.WHITE.getRGB());
			textWidget.setXTextAlignment(WidgetTextAlignment.CENTER);
			textWidget.setYTextAlignment(WidgetTextAlignment.CENTER);
			textWidget.setPos(0, -200);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		trackItems();

		if(e.getKey().equals("reset")) {
			unlockedItems.clear();
			configManager.setConfiguration(CONFIG_GROUP, UNLOCK_CONFIG_KEY, "");
		}
	}

	private void addToQueue(String itemName) {
		if(popupQueue.contains(itemName) || currentPopup == itemName) return;

		popupQueue.add(itemName);
	}

	private void trackItems() {
		items.clear();
		String[] itemList = config.items().split(",");

		for(String item : itemList) {
			String name = item.trim().toLowerCase();
			items.add(name);
		}
	}

	private void updateUnlockedItems() {
		unlockedItems.clear();
		String[] itemList = config.unlockedItems().split(",");

		for(String item : itemList) {
			String name = item.trim().toLowerCase();
			unlockedItems.add(name);
		}
	}

	private void addUnlockedItem(String item) {
		unlockedItems.add(item);

		configManager.setConfiguration(MoreLogPopupsPlugin.CONFIG_GROUP, MoreLogPopupsPlugin.UNLOCK_CONFIG_KEY, String.join(",", unlockedItems));
	}

	/**
	 * Is player currently within the provided map regions
	 */
	private boolean isPlayerWithinMapRegion(Set<Integer> definedMapRegions)
	{
		final int[] mapRegions = client.getMapRegions();

		for (int region : mapRegions)
		{
			if (definedMapRegions.contains(region))
			{
				return true;
			}
		}

		return false;
	}

	private void createSnapshot() {
		inventorySnapshot = HashMultiset.create();

		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer != null)
		{
			Arrays.stream(itemContainer.getItems())
					.forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
		}
	}

	@Provides
	MoreLogPopupsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MoreLogPopupsConfig.class);
	}
}
