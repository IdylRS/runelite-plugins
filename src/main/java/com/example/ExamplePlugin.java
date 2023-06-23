package com.example;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.sound.midi.*;

@Slf4j
@PluginDescriptor(
	name = "Bad Stuff"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ExampleConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Hooks hooks;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SpriteManager spriteManager;

	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log:.*");

	private List<Integer> dogs = Arrays.asList(111, 112, 113, 114, 131, 2802, 2902, 2922, 2829, 2820, 4228, 6473, 6474, 7025, 7209, 7771, 8041, 10439, 10675, 10760);
	private List<Integer> cats = Arrays.asList(3498, 5591, 5592, 5593, 5594, 5595, 5596, 5597, 4780, 395, 1619, 1620, 1621, 1622, 1623, 1624, 3831, 3832, 6662, 6663, 6664, 6665, 6666, 6667, 7380, 8594, 2474, 2475, 2476, 4455, 540, 541, 2782, 6661, 2346, 1010, 3497, 1625, 6668, 1626, 1627, 1628, 1629, 1630, 1631, 6683, 6684, 6685, 6686, 6687, 6688, 1632, 6689, 2644, 4229, 5598, 5599, 5600);

	private int[] allStar = {73, 70, 68, 66, 70, 71, 70, 68, 66, 63, 61};

	private int allergyCD = 15;
	private int allergyCDTimer = 0;

	private List<String> insults = Arrays.asList("LOLOLOL okay kid whatever you're dog shit you're frickin dogshit", "you think i care that you killed me? mom made pizza rolls and your mom is too busy being fat!!!", "lol ok who even are you {p}? never heard of you. i have 100k tiktok followers idiot.");
	private List<String> playerInsults = Arrays.asList("log out you idiot you're almost 30 and bald!!",
			"hey you! yeah you! you suck!",
			"{p} is a total moron!",
			"get outta here {p} no one wants you!",
			"you have a tiny pp unles you are a woman in which case you have a tiny vagine.");

	private int seqNum = 0;

	SoundEngine engine;
	Synthesizer synth;
	MidiChannel[] mChannels;

	private int[] itemIDs;
	private List<Integer> hiddenIDs = new ArrayList<>();
	private WorldPoint lastPos;

	private HashMap<Skill, Integer> exp = new HashMap<>();

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private int ticksTilLogout = -1;
	private int ticksTilDisease = 0;

	private MenuEntry lastAction;

	private FunBarOverlay funBarOverlay;

	@Getter
	private int fun = 100;

	@Inject
	private OkHttpClient httpClient;

	@Override
	protected void startUp() throws Exception
	{
		engine = new SoundEngine();
		synth = MidiSystem.getSynthesizer();
		synth.open();

		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
		mChannels = synth.getChannels();
		synth.loadInstrument(instr[6]);//load an instrument

		itemIDs = loadDefinitionResource(int[].class, "itemIDs.json");

		hooks.registerRenderableDrawListener(drawListener);

		SpriteDefinition[] overrides = loadDefinitionResource(SpriteDefinition[].class, "SpriteDef.json");
		spriteManager.addSpriteOverrides(overrides);

		funBarOverlay = new FunBarOverlay(client, this, spriteManager);
		overlayManager.add(funBarOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		hooks.unregisterRenderableDrawListener(drawListener);
	}

	private <T> T loadDefinitionResource(Class<T> classType, String resource) {
		// Load the resource as a stream and wrap it in a reader
		InputStream resourceStream = getClass().getResourceAsStream(resource);
		assert resourceStream != null;
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);

		// Load the objects from the JSON file
		return gson.fromJson(definitionReader, classType);
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		if(client.getGameState() != GameState.LOGGED_IN) return;

		if(ticksTilLogout < 0) {
			ticksTilLogout = config.logoutTicks();
		}
		else if(ticksTilLogout == 0 && config.logoutTicks() > 0) {
			client.setGameState(GameState.LOGIN_SCREEN);
		}

		if(config.statistics() && ticksTilDisease % 100 == 0) {
			int num = (int) Math.floor(Math.random()*5)+13;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "In the last 5 minutes, about "+num+" have died of tuberculosis. Thanks for playing Runescape!", "");
		}

		if(ticksTilDisease % 5 == 0) {
			fun = Math.max(0, fun-1);
		}

		ticksTilLogout--;
		ticksTilDisease++;

		if(lastPos == null) {
			lastPos = client.getLocalPlayer().getWorldLocation();
		}

		Calendar cal = Calendar.getInstance(config.timezone().timezone);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if(hour >= 19 || hour < 6) {
			client.setSkyboxColor(new Color(10, 17, 28).getRGB());
		}
		else {
			client.setSkyboxColor(new Color(140, 221, 255).getRGB());
		}

		if(lastPos.distanceTo(client.getLocalPlayer().getWorldLocation()) > 0) {
//			engine.playClip("walk.wav");
			lastPos = client.getLocalPlayer().getWorldLocation();
		}

		if(config.sub()) {
			List<Player> players = client.getPlayers();

			for(Player p : players) {
				if(p.getAnimation() != -1 || p == client.getLocalPlayer()) continue;

				p.setOverheadText(playerInsults.get((int) Math.floor(Math.random()*playerInsults.size())).replace("{p}", client.getLocalPlayer().getName()));
				p.setAnimation(861);
				p.setAnimationFrame(0);
			}
		}

		if(client.getGameState().equals(GameState.LOGGED_IN) && config.metroStar()) {
			mChannels[0].noteOn(allStar[seqNum%allStar.length], 127);
			seqNum++;
		}

		if(config.badMetronome()) {
			int roll = (int) Math.floor(Math.random()*2);

			if(roll == 0) {
				client.playSoundEffect(SoundEffectID.GE_INCREMENT_PLOP, 100);
			}
		}

		if(allergyCDTimer > 0) allergyCDTimer--;

		if(config.allergyWomen() && allergyCDTimer == 0) {
			List<Player> players = client.getPlayers();

			for (Player p : players) {
				if(p.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 5) return;

				if (p.getPlayerComposition().getGender() == 1 && client.getLocalPlayer().getOverheadText() == null) {
					client.getLocalPlayer().setOverheadText("*sniff* ugh i hate women.");
					client.getLocalPlayer().setOverheadCycle(200);
					allergyCDTimer = allergyCD;
				}
			}
		}

		if(config.allergyCats() || config.allergyDogs() && allergyCDTimer == 0) {
			List<NPC> npcs = client.getNpcs();

			for(NPC npc : npcs) {
				if(client.getLocalPlayer().getWorldLocation().distanceTo(npc.getWorldLocation()) > 5) return;

				if(config.allergyDogs() && dogs.contains(npc.getId()) ) {
					client.getLocalPlayer().setOverheadText("Achoo!! *sniff* i hate being allergic to doggy.");
					client.getLocalPlayer().setOverheadCycle(200);
					allergyCDTimer = allergyCD;
				}
				else if(config.allergyCats() && cats.contains(npc.getId())) {
					client.getLocalPlayer().setOverheadText("*sneeze*!!!! Wow, i am SO allergic to pussy.");
					client.getLocalPlayer().setOverheadCycle(200);
					allergyCDTimer = allergyCD;
				}
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged e) throws IOException {
		exp.computeIfAbsent(e.getSkill(), k -> e.getXp());

		if(e.getXp() != exp.get(e.getSkill())) {
			int wooNum = (int) Math.ceil(Math.random()*9);
			engine.playClip("woo"+wooNum+".wav");
			ticksTilLogout = config.logoutTicks();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e) {
		if(e.getNpc().getName().contains("Banker")) {
			e.getNpc().setIdlePoseAnimation(3040);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID && config.owoify()) {
			Widget widget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT.getPackedId());
			clientThread.invokeLater(() -> {
				String text = Owoify.convert(widget.getText());
				widget.setText(text);
			});
		}
		else if(e.getGroupId() == WidgetID.DIALOG_PLAYER_GROUP_ID && config.owoify()) {
			Widget widget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT.getPackedId());
			clientThread.invokeLater(() -> {
				String text = Owoify.convert(widget.getText());
				widget.setText(text);
			});
		}
		else if(e.getGroupId() == 541) {
			HashMap<Integer, Boolean> slots = new HashMap();

			for(int i=0;i<29;i++) {
				Widget prayer = client.getWidget(541, i+9);
				int slot = (int) Math.floor(Math.random()*29);

				while(slots.get(slot) != null) {
					slot = (int) Math.floor(Math.random()*29);
				}

				int x = (slot % 5) * 37;
				int y = (slot/5) * 37;
				prayer.setPos(x, y);
				slots.put(slot, true);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e) {
		if(e.getScriptId() == 914 && !client.getWidget(35454976).isHidden()) {
			HashMap<Integer, Boolean> slots = new HashMap();
			int[] spriteIds = Prayers.getSpriteIDs();

			for(int i=0;i<29;i++) {
				Widget prayer = client.getWidget(541, i+9);
				int slot = (int) Math.floor(Math.random()*29);

				while(slots.get(slot) != null) {
					slot = (int) Math.floor(Math.random()*29);
				}

				int x = (slot % 5) * 37;
				int y = (slot/5) * 37;
				prayer.setPos(x, y);

				prayer.getChild(1).setSpriteId(spriteIds[(int) Math.floor(Math.random()*spriteIds.length)]);
				slots.put(slot, true);
			}
		}
		if(e.getScriptId() == 2396 && !client.getWidget(9764864).isHidden() && config.mayoize()) {
			Widget[] items = client.getWidget(9764864).getDynamicChildren();

			for(Widget itemSlot : items) {
//				int itemID = itemIDs[(int) Math.floor(Math.random()*itemIDs.length)];
//				ItemComposition item = client.getItemDefinition(itemID);

				itemSlot.setItemId(-1);
				itemSlot.setName("Mayo");
				itemSlot.setSpriteId(-20000);
			}
		}
		if(e.getScriptId() == 490 && !client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER.getPackedId()).isHidden() && config.mayoize()) {
			Widget[] items = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER.getPackedId()).getDynamicChildren();

			for(Widget itemSlot : items) {
				//Randomize code commented out
//				int itemID = itemIDs[(int) Math.floor(Math.random()*itemIDs.length)];
//				ItemComposition item = client.getItemDefinition(itemID);

				itemSlot.setItemId(-1);
				itemSlot.setName("Mayo");
				itemSlot.setSpriteId(-20000);
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		ticksTilLogout = config.logoutTicks();

		if(lastAction == null || lastAction.getIdentifier() != e.getMenuEntry().getIdentifier()) {
			e.consume();
			lastAction = e.getMenuEntry();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Oops! Looks like you want to " + e.getMenuOption() + "! Do it again to confirm.", "");
		}
		else {
			lastAction = null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e) throws IOException {
		if(e.getType().equals(ChatMessageType.DIALOG)) {
			return;
		}
		else if(config.owoify()){
			e.getMessageNode().setValue(Owoify.convert(e.getMessage()));
		}

		if(config.snapshot()) {
			if(COLLECTION_LOG_ITEM_REGEX.matcher(e.getMessage()).matches()) {
				sendReq();
			}
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged e) {
		String text = Owoify.convert(e.getOverheadText());
		e.getActor().setOverheadText(text);
		e.getActor().setOverheadCycle(30);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath e) {
		if(e.getActor() instanceof NPC) {
			Player interacting = (Player) e.getActor().getInteracting();

			if(interacting != null || client.getLocalPlayer().getInteracting().equals(e.getActor())) {
				String insult = insults.get((int) Math.floor(Math.random()*insults.size()));
				String name = client.getLocalPlayer().getName();
				e.getActor().setOverheadText(insult.replace("{p}", name));
			}

			if(config.hideNPCs()) hiddenIDs.add(((NPC) e.getActor()).getId());
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged e) {
		if(e.getSource().equals(client.getLocalPlayer()) && e.getTarget() instanceof NPC) {
			NPC npc = (NPC) e.getTarget();

			if(npc.getCombatLevel() <= 0) {
				if(config.hideNPCs()) hiddenIDs.add(npc.getId());
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e) {
		if(e.getHitsplat().getAmount() == 0 || !config.idylHurt()) return;

		if(e.getActor().equals(client.getLocalPlayer())) {
			if(e.getHitsplat().getAmount() <= 5) {
				engine.playClip("0.wav");
			}
			else if(e.getHitsplat().getAmount() <= 10) {
				engine.playClip("1.wav");
			}
			else if(e.getHitsplat().getAmount() <= 15) {
				engine.playClip("2.wav");
			}
			else if(e.getHitsplat().getAmount() <= 25) {
				engine.playClip("3.wav");
			}
			else if(e.getHitsplat().getAmount() <= 40) {
				engine.playClip("4.wav");
			}
			else if(e.getHitsplat().getAmount() <= 50) {
				engine.playClip("5.wav");
			}
			else if(e.getHitsplat().getAmount() <= 60) {
				engine.playClip("6.wav");
			}
			else {
				engine.playClip("7.wav");
			}
		}
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if(!config.hideNPCs()) return true;

		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;

			return !hiddenIDs.contains(npc.getId());
		}

		return true;
	}

	private void sendReq() throws IOException {
		URL url = new URL("http://localhost:8080");
		Request req = new Request.Builder().url(url).build();
		Response res = httpClient.newCall(req).execute();
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
