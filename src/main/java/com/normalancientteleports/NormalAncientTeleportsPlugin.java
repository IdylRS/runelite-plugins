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
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(spells == null) return;

		// On Spell book loaded
		if(e.getGroupId() == WidgetID.SPELLBOOK_GROUP_ID) {
			spells.forEach(spell -> {
				Widget widget = client.getWidget(spell.widgetID);
				String newText = widget.getName().replaceAll(spell.originalName, spell.newName);
				widget.setName(newText);
			});
		}
		else if(e.getGroupId() == 17 && config.replacePortalNexus()) {
			Widget widget = client.getWidget(17, 12);

			for(Widget w : widget.getChildren()) {
				spells.forEach(spell -> {
					if(w.getText().contains(spell.originalName)) {
						String newText = w.getText().replaceAll(spell.originalName, spell.newName);
						w.setText(newText);
					}
				});
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e) {
		Widget widget = client.getWidget(14287047);
		if(widget == null || widget.getChildren() == null) return;

		Widget textWidget = widget.getChild(3);

		spells.forEach(spell -> {
			if(textWidget.getText().contains(spell.originalName)) {
				String newText = textWidget.getText().replaceAll(spell.originalName.concat(" Teleport"), spell.newName);
				textWidget.setText(newText);
			}
		});
	}

	@Provides
	NormalAncientTeleportsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NormalAncientTeleportsConfig.class);
	}
}
