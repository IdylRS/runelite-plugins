package com.example;

import com.example.ui.UICheckBox;
import com.example.ui.UIComponent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	public static final String DEF_FILE_SPRITES = "SpriteDef.json";
	public static final String DEF_FILE_TASKS = "tasks.json";

	private static final String DATA_FOLDER_NAME = "generate-task";
	public static final int COLLECTION_LOG_WINDOW_WIDTH = 500;
	public static final int COLLECTION_LOG_WINDOW_HEIGHT = 314;
	public static final int COLLECTION_LOG_CONTENT_WIDGET_ID = 40697858;

	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private Gson gson;

	@Inject
	private SpriteManager spriteManager;

	private SpriteDefinition[] spriteDefinitions;
	private Task[] tasks;
	private SaveData saveData;

	private TaskDashboard taskDashboard;
	private UICheckBox taskDashboardCheckbox;

	private File playerFile;

	@Override
	protected void startUp() throws Exception
	{
		this.spriteDefinitions = loadDefinitionResource(SpriteDefinition[].class, DEF_FILE_SPRITES, gson);
		this.tasks = loadDefinitionResource(Task[].class, DEF_FILE_TASKS, gson);
		this.spriteManager.addSpriteOverrides(spriteDefinitions);

		for(Task t : tasks) {
			log.info("Created task: "+t.getDescription());
		}
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

	/**
	 * Sets up the playerFile variable, and makes the player file if needed.
	 */
	private void setupPlayerFile() {
		saveData = new SaveData();
		File playerFolder = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
		if (!playerFolder.exists()) {
			playerFolder.mkdirs();
		}
		playerFile = new File(playerFolder, client.getAccountHash() + ".txt");
		if (!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			loadPlayerData();
		}
	}

	private void loadPlayerData() {
		try {
			String json = new Scanner(playerFile).useDelimiter("\\Z").next();
			saveData = GSON.fromJson(json, new TypeToken<SaveData>() {}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void savePlayerData() {
		try {
			PrintWriter w = new PrintWriter(playerFile);
			String json = GSON.toJson(saveData);
			w.println(json);
			w.close();
			log.debug("Saving player data");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			if(saveData == null) {
				setupPlayerFile();
			}
		}
		else if(gameStateChanged.getGameState().equals(GameState.LOGIN_SCREEN)) {
			if(saveData != null) {
				savePlayerData();
			}

			saveData = null;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if(e.getGroupId() == WidgetInfo.COLLECTION_LOG.getGroupId()) {
			createTaskDashboard(client.getWidget(40697857));
			createGenerateButton();

			this.taskDashboardCheckbox.setEnabled(false);
			this.taskDashboard.setVisibility(false);
		}
	}

	private void createGenerateButton() {
		Widget window = client.getWidget(40697857);
		// Create the graphic widget for the checkbox
		Widget toggleWidget = window.createChild(-1, WidgetType.GRAPHIC);
		Widget labelWidget = window.createChild(-1, WidgetType.TEXT);

		// Wrap in checkbox, set size, position, etc.
		taskDashboardCheckbox = new UICheckBox(toggleWidget, labelWidget);
		taskDashboardCheckbox.setPosition(360, 10);
		taskDashboardCheckbox.setName("Task Dashboard");
		taskDashboardCheckbox.setEnabled(false);
		taskDashboardCheckbox.setText("Task Dashboard");
		labelWidget.setPos(375, 10);
		taskDashboardCheckbox.setToggleListener(this::toggleTaskDashboard);
	}

	private void createTaskDashboard(Widget window) {
		this.taskDashboard = new TaskDashboard(this, window);
		this.taskDashboard.setVisibility(false);
	}

	private void toggleTaskDashboard(UIComponent src) {
		if(this.taskDashboard == null) return;

		this.taskDashboard.setVisibility(this.taskDashboardCheckbox.isEnabled());

		if(saveData.currentTask != null) {
			this.taskDashboard.setTask(this.saveData.currentTask.getDescription(), this.saveData.currentTask.getItemID());
			this.taskDashboard.disableGenerateTask();
		}
		else {
			nullCurrentTask();
		}

		client.getWidget(COLLECTION_LOG_CONTENT_WIDGET_ID).setHidden(this.taskDashboardCheckbox.isEnabled());

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	public void generateTask() {
		if(this.saveData.currentTask != null || this.tasks == null) {
			this.taskDashboard.disableGenerateTask();
			return;
		}

		List<Task> uniqueTasks = filterCompleteTasks(Arrays.asList(this.tasks));

		if(uniqueTasks.size() <= 0) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No more tasks left. Looks like you win?", "");
			playFailSound();

			return;
		};

		int index = (int) Math.floor(Math.random()*uniqueTasks.size());


		this.saveData.currentTask = uniqueTasks.get(index);
		this.taskDashboard.setTask(this.saveData.currentTask.getDescription(), this.saveData.currentTask.getItemID());
		log.debug("Task generated: "+this.saveData.currentTask.getDescription());

		this.taskDashboard.disableGenerateTask();

		savePlayerData();
	}

	public void completeTask() {
		if(this.saveData.currentTask == null) {
			this.taskDashboard.enableGenerateTask();
			return;
		}

		addCompletedTask(this.saveData.currentTask);
		nullCurrentTask();

		savePlayerData();
	}

	private void nullCurrentTask() {
		this.saveData.currentTask = null;
		this.taskDashboard.setTask("No task.", -1);
		this.taskDashboard.enableGenerateTask();
	}

	public static int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	public static int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	public void addCompletedTask(Task task) {
		if(this.saveData.getCompletedTasks().get(task.getId()) != null) return;

		this.saveData.getCompletedTasks().put(task.getId(), 0);
	}

	public List<Task> filterCompleteTasks(List<Task> taskList) {
		return taskList.stream().filter(t -> this.saveData.getCompletedTasks().get(t.getId()) == null).collect(Collectors.toList());
	}

	public void playFailSound() {
		client.playSoundEffect(2277);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
