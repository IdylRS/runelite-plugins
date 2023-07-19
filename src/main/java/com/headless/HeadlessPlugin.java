package com.headless;

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Headless"
)
public class HeadlessPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private HeadlessConfig config;

	@Inject
	private Gson gson;

	private List<String> helmetNames;

	@Override
	protected void startUp() throws Exception
	{
		helmetNames = Arrays.asList(loadDefinitionResource(String[].class, "helmets.json"));
	}

	@Override
	protected void shutDown() throws Exception
	{
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
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if(e.getMenuOption().startsWith("Talk")) {
			e.consume();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can't talk! You have no head.", null);
		}
		else if(e.getMenuOption().startsWith("Eat")) {
			e.consume();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can't eat that! You'd need a head to chew it.", null);
		}
		else if(e.getMenuOption().startsWith("Wear")) {
			ItemComposition item = client.getItemDefinition(e.getMenuEntry().getItemId());

			if(item == null) return;

			for(String helmet : helmetNames) {
				if(item.getName().startsWith(helmet)) {
					e.consume();
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You can't wear that! You'd need a head to put it on.", null);
					return;
				}
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e) {
		if(e.getScriptId() == 914 && !client.getWidget(WidgetInfo.EQUIPMENT.getPackedId()).isHidden()) {
			Widget helm = client.getWidget(25362447);
			if(helm != null) {
				helm.setHidden(true);
			}
		}
	}

	@Provides
	HeadlessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HeadlessConfig.class);
	}
}
