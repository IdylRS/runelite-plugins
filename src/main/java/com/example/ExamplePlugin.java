package com.example;

import com.example.ui.UIButton;
import com.example.ui.UICheckBox;
import com.example.ui.UIComponent;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	public static final String DEF_FILE_SPRITES = "SpriteDef.json";

	public static final int COLLECTION_LOG_WINDOW_WIDTH = 500;
	public static final int COLLECTION_LOG_WINDOW_HEIGHT = 314;
	public static final int COLLECTION_LOG_CONTENT_WIDGET_ID = 40697858;

	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private Gson gson;

	private SpriteDefinition[] spriteDefinitions;

	private Task currentTask;

	private TaskDashboard taskDashboard;
	private boolean showTaskDashboard = false;

	@Override
	protected void startUp() throws Exception
	{
		this.spriteDefinitions = loadDefinitionResource(SpriteDefinition[].class, DEF_FILE_SPRITES, gson);
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	/**
	 * Loads a definition resource from a JSON file
	 *
	 * @param classType the class into which the data contained in the JSON file will be read into
	 * @param resource  the name of the resource (file name)
	 * @param gson      a reference to the GSON object
	 * @param <T>       the class type
	 * @return the data read from the JSON definition file
	 */
	private <T> T loadDefinitionResource(Class<T> classType, String resource, Gson gson) {
		// Load the resource as a stream and wrap it in a reader
		InputStream resourceStream = classType.getResourceAsStream(resource);
		assert resourceStream != null;
		InputStreamReader definitionReader = new InputStreamReader(resourceStream);

		// Load the objects from the JSON file
		return gson.fromJson(definitionReader, classType);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == WidgetInfo.COLLECTION_LOG.getGroupId()) {
			createGenerateButton();
			createTaskDashboard(client.getWidget(40697857));
		}
	}

	private void createGenerateButton() {
		Widget window = client.getWidget(40697857);
		// Create the graphic widget for the checkbox
		Widget toggleWidget = window.createChild(-1, WidgetType.GRAPHIC);
		Widget labelWidget = window.createChild(-1, WidgetType.TEXT);

		// Wrap in checkbox, set size, position, etc.
		UICheckBox mapToggle = new UICheckBox(toggleWidget, labelWidget);
		mapToggle.setPosition(360, 10);
		mapToggle.setName("Task Dashboard");
		mapToggle.setEnabled(showTaskDashboard);
		mapToggle.setText("Task Dashboard");
		labelWidget.setPos(375, 10);
		mapToggle.setToggleListener(this::toggleTaskDashboard);
	}

	private void createTaskDashboard(Widget window) {
		this.taskDashboard = new TaskDashboard(window);
		this.taskDashboard.setVisibility(this.showTaskDashboard);
	}

	private void toggleTaskDashboard(UIComponent src) {
		// The checkbox component
		UICheckBox toggleCheckbox = (UICheckBox) src;

		// Update the map enabled flag
		this.showTaskDashboard = toggleCheckbox.isEnabled();
		this.taskDashboard.setVisibility(showTaskDashboard);

		if(currentTask != null) this.taskDashboard.setTaskText(currentTask.getDescription());
		else this.taskDashboard.setTaskText("No task.");

		client.getWidget(COLLECTION_LOG_CONTENT_WIDGET_ID).setHidden(showTaskDashboard);

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	public static int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	public static int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
