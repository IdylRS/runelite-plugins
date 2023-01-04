package com.normalancientteleports;

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
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
	name = "Normal Ancient Teleports"
)
public class NormalAncientTeleportsPlugin extends Plugin
{
	private final String DEF_FILE_SPELLS = "spells.json";

	@Inject
	private Client client;

	@Inject
	private NormalAncientTeleportsConfig config;

	@Inject
	private Gson gson;

	private List<NormalAncientTeleportsSpellData> spells;

	@Override
	protected void startUp() throws Exception
	{
		InputStream resourceStream = NormalAncientTeleportsSpellData.class.getResourceAsStream(DEF_FILE_SPELLS);
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);
		this.spells = Arrays.asList(gson.fromJson(definitionReader, NormalAncientTeleportsSpellData[].class));
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	private <T> T loadDefinitionResource(Class<T> classType, String resource) {
		// Load the resource as a stream and wrap it in a reader
		InputStream resourceStream = classType.getResourceAsStream(resource);
		assert resourceStream != null;
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);

		// Load the objects from the JSON file
		return gson.fromJson(definitionReader, classType);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(spells == null) return;

		// On Spell book loaded
		if(e.getGroupId() == WidgetID.SPELLBOOK_GROUP_ID) {
			spells.forEach(spell -> {
				Widget widget = client.getWidget(spell.widgetID);
				String newText = widget.getName().replaceAll(spell.originalName, spell.newName.concat(" Teleport"));
				widget.setName(newText);
			});
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e) {
		Widget widget = client.getWidget(14287047);
		if(widget == null || widget.getChildren() == null) return;

		Widget textWidget = widget.getChild(3);

		spells.forEach(spell -> {
			if(textWidget.getText().contains(spell.originalName)) {
				String newText = textWidget.getText().replaceAll(spell.originalName, spell.newName);
				textWidget.setText(newText);

//				if(spell.widgetID == 14286947 || spell.widgetID == 14286945 || spell.widgetID == 14286944) {
//					int i = 0;
//					if(widget.getChildren() == null) return;
//
//					log.info(widget.getChildren().length+" length");
//
//					for(Widget w : widget.getChildren()) {
//						int offset = 0;
//
//						if(i < 3) {
//							w.setSize(w.getWidth(), w.getHeight()+20);
//						}
//						else if(i == 3) {
//							offset = 7;
//						}
//						else {
//							offset = 15;
//						}
//
//						w.setPos(w.getOriginalX(), w.getOriginalY()+offset);
//						w.revalidate();
//						i++;
//					}
//				}
			}
		});
	}

	@Provides
	NormalAncientTeleportsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NormalAncientTeleportsConfig.class);
	}
}
