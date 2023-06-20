package com.example;

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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ExampleConfig config;

	private List<Integer> dogs = Arrays.asList(111, 112, 113, 114, 131, 2802, 2902, 2922, 2829, 2820, 4228, 6473, 6474, 7025, 7209, 7771, 8041, 10439, 10675, 10760);
	private List<Integer> cats = Arrays.asList(3498, 5591, 5592, 5593, 5594, 5595, 5596, 5597, 4780, 395, 1619, 1620, 1621, 1622, 1623, 1624, 3831, 3832, 6662, 6663, 6664, 6665, 6666, 6667, 7380, 8594, 2474, 2475, 2476, 4455, 540, 541, 2782, 6661, 2346, 1010, 3497, 1625, 6668, 1626, 1627, 1628, 1629, 1630, 1631, 6683, 6684, 6685, 6686, 6687, 6688, 1632, 6689, 2644, 4229, 5598, 5599, 5600);

	private int allergyCD = 15;
	private int allergyCDTimer = 0;

	@Override
	protected void startUp() throws Exception
	{
		Owoify.Setup();
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameTick(GameTick e) {
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
		if(e.getGroupId() == WidgetID.DIALOG_NPC_GROUP_ID) {
			Widget widget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT.getPackedId());
			clientThread.invokeLater(() -> {
				String text = Owoify.convert(widget.getText());
				widget.setText(text);
			});
		}
		else if(e.getGroupId() == WidgetInfo.CHATBOX_MESSAGES.getGroupId()) {
			Widget parent = client.getWidget(WidgetInfo.CHATBOX_MESSAGES.getGroupId()).createChild(0);
			Widget child1 = parent.createChild(4);
			child1.setText("I hate that task wtf??");
			child1.setAction(0, "hover");
			child1.setTextColor(Color.BLACK.getRGB());
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

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
