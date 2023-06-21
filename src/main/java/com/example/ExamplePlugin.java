package com.example;

import com.example.ui.UIButton;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	private Gson gson;

	private List<Integer> dogs = Arrays.asList(111, 112, 113, 114, 131, 2802, 2902, 2922, 2829, 2820, 4228, 6473, 6474, 7025, 7209, 7771, 8041, 10439, 10675, 10760);
	private List<Integer> cats = Arrays.asList(3498, 5591, 5592, 5593, 5594, 5595, 5596, 5597, 4780, 395, 1619, 1620, 1621, 1622, 1623, 1624, 3831, 3832, 6662, 6663, 6664, 6665, 6666, 6667, 7380, 8594, 2474, 2475, 2476, 4455, 540, 541, 2782, 6661, 2346, 1010, 3497, 1625, 6668, 1626, 1627, 1628, 1629, 1630, 1631, 6683, 6684, 6685, 6686, 6687, 6688, 1632, 6689, 2644, 4229, 5598, 5599, 5600);

	private int[] allStar = {73, 70, 68, 66, 70, 71, 70, 68, 66, 63, 61};

	private int allergyCD = 15;
	private int allergyCDTimer = 0;

	private List<String> insults = Arrays.asList("LOLOLOL okay kid whatever you're dog shit you're frickin dogshit", "you think i care that you killed me? mom made pizza rolls and your mom is too busy being fat!!!", "lol ok who even are you {p}? never heard of you. i have 100k tiktok followers idiot.");

	private int seqNum = 0;

	SoundEngine engine;
	Synthesizer synth;
	MidiChannel[] mChannels;

	private int[] itemIDs;

	@Override
	protected void startUp() throws Exception
	{
		engine = new SoundEngine();
		synth = MidiSystem.getSynthesizer();
		synth.open();

		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
		mChannels = synth.getChannels();
		synth.loadInstrument(instr[6]);//load an instrument

		InputStream stream = getClass().getResourceAsStream("itemIDs.json");
		InputStreamReader reader = new InputStreamReader(stream);

		itemIDs = gson.fromJson(reader, int[].class);
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameTick(GameTick e) {
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

				log.info("put " + prayer.getName() + " in slot "+slot);

				slots.put(slot, true);
			}
		}
		else if(e.getGroupId() == WidgetInfo.FIXED_VIEWPORT_INVENTORY_CONTAINER.getGroupId()) {
			int itemID = itemIDs[(int) Math.floor(Math.random()*itemIDs.length)];
			ItemComposition item = client.getItemDefinition(itemID);

			if(item != null) {
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

				log.info("put " + prayer.getName() + " in slot "+slot);

				slots.put(slot, true);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e) {
		if(e.getType().equals(ChatMessageType.DIALOG)) {
			return;
		}
		else {
			e.getMessageNode().setValue(Owoify.convert(e.getMessage()));
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

			if(interacting != null) {
				String insult = insults.get((int) Math.floor(Math.random()*insults.size()));
				e.getActor().setOverheadText(insult.replace("{p}", interacting.getName()));
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

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
