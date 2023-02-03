package com.generatetask;

import com.generatetask.ui.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.input.MouseWheelListener;

import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
@PluginDescriptor(
	name = "Generate Task"
)
public class GenerateTaskPlugin extends Plugin implements MouseWheelListener
{
	public static final String DEF_FILE_SPRITES = "SpriteDef.json";
	public static final String DEF_FILE_TASKS = "tasks.json";
	public static final int TASK_BACKGROUND_SPRITE_ID = -20006;
	public static final int TASK_LIST_BACKGROUND_SPRITE_ID = -20012;
	public static final int TASK_COMPLETE_BACKGROUND_SPRITE_ID = -20013;
	public static final int TASK_CURRENT_BACKGROUND_SPRITE_ID = -20016;

	private static final String DATA_FOLDER_NAME = "generate-task";
	public static final int COLLECTION_LOG_WINDOW_WIDTH = 500;
	public static final int COLLECTION_LOG_WINDOW_HEIGHT = 314;
	public static final int COLLECTION_LOG_CONTENT_WIDGET_ID = 40697858;

	private static final int DASHBOARD_TAB_SPRITE_ID = -20007;
	private static final int DASHBOARD_TAB_HOVER_SPRITE_ID = -20008;
	private static final int TASKLIST_TAB_SPRITE_ID = -20009;
	private static final int TASKLIST_TAB_HOVER_SPRITE_ID = -20010;
	private static final int DIVIDER_SPRITE_ID = -20011;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private GenerateTaskConfig config;

	@Inject
	private Gson gson;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private MouseManager mouseManager;

	private SpriteDefinition[] spriteDefinitions;
	private Task[] tasks;

	@Getter
	private SaveData saveData;

	private TaskDashboard taskDashboard;
	private TaskList taskList;
	private UICheckBox taskDashboardCheckbox;

	private UIButton taskListTab;
	private UIButton taskDashboardTab;

	private int activeTab = 0;

	private File playerFile;

	@Override
	protected void startUp() throws Exception
	{
		this.spriteDefinitions = loadDefinitionResource(SpriteDefinition[].class, DEF_FILE_SPRITES, gson);
		this.tasks = loadDefinitionResource(Task[].class, DEF_FILE_TASKS, gson);
		this.spriteManager.addSpriteOverrides(spriteDefinitions);
		mouseManager.registerMouseWheelListener(this);
	}

	@Override
	protected void shutDown() throws Exception
	{
		mouseManager.unregisterMouseWheelListener(this);
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
			Widget window = client.getWidget(40697857);

			Widget dashboardTabWidget = window.createChild(-1, WidgetType.GRAPHIC);
			taskDashboardTab = new UIButton(dashboardTabWidget);
			taskDashboardTab.setSprites(DASHBOARD_TAB_SPRITE_ID, DASHBOARD_TAB_HOVER_SPRITE_ID);
			taskDashboardTab.setSize(95, 21);
			taskDashboardTab.setPosition(10, 36);
			taskDashboardTab.addAction("Switch", this::activateTaskDashboard);

			Widget taskListTabWidget = window.createChild(-1, WidgetType.GRAPHIC);
			taskListTab = new UIButton(taskListTabWidget);
			taskListTab.setSprites(TASKLIST_TAB_SPRITE_ID, TASKLIST_TAB_HOVER_SPRITE_ID);
			taskListTab.setSize(95, 21);
			taskListTab.setPosition(110, 36);
			taskListTab.addAction("Switch", this::activateTaskList);

			Widget dividerWidget = window.createChild(-1, WidgetType.GRAPHIC);
			UIGraphic divider = new UIGraphic(dividerWidget);
			divider.setSprite(DIVIDER_SPRITE_ID);
			divider.setSize(480, 1);
			divider.setPosition(10, 56);

			createTaskDashboard(window);
			createTaskList(window);
			createGenerateButton();

			this.taskDashboardCheckbox.setEnabled(false);
			this.taskDashboard.setVisibility(false);
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed e) {
		if(e.getGroupId() == WidgetInfo.COLLECTION_LOG.getGroupId()) {
			this.taskDashboard.setVisibility(false);
			this.taskList.setVisibility(false);
			this.taskDashboardCheckbox.setEnabled(false);
		}
	}

	@Override
	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
	{
		if(this.taskList != null) {
			taskList.handleWheel(event);
		}

		return event;
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

		if(saveData != null) setTaskCompletionPercent();
	}

	private void createTaskList(Widget window) {
		this.taskList = new TaskList(window, Arrays.asList(this.tasks), this, clientThread);
		this.taskList.setVisibility(false);
	}

	private void toggleTaskDashboard(UIComponent src) {
		if(this.taskDashboard == null) return;

		if(saveData.currentTask != null) {
			this.taskDashboard.setTask(this.saveData.currentTask.getDescription(), this.saveData.currentTask.getItemID());
			this.taskDashboard.disableGenerateTask();
		}
		else {
			nullCurrentTask();
		}

		client.getWidget(COLLECTION_LOG_CONTENT_WIDGET_ID).setHidden(this.taskDashboardCheckbox.isEnabled());
		client.getWidget(40697936).setHidden(this.taskDashboardCheckbox.isEnabled());

		if(this.taskDashboardCheckbox.isEnabled()) {
			activateTaskDashboard();
		}
		else {
			this.taskDashboard.setVisibility(false);
			this.taskList.setVisibility(false);
		}

		// *Boop*
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
	}

	public void generateTask() {
		if(this.saveData.currentTask != null || this.tasks == null) {
			this.taskDashboard.disableGenerateTask();
			return;
		}

		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
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
		taskList.refreshTasks(0);

		savePlayerData();
	}

	public void completeTask() {
		completeTask(saveData.currentTask.getId());
	}

	public void completeTask(int taskID) {
		this.client.playSoundEffect(SoundEffectID.UI_BOOP);

		if(saveData.getCompletedTasks().get(taskID) != null) {
			saveData.getCompletedTasks().remove(taskID);
		}
		else {
			addCompletedTask(taskID);
			if(saveData.currentTask != null && taskID == saveData.currentTask.getId()) nullCurrentTask();
		}

		setTaskCompletionPercent();
		taskList.refreshTasks(0);

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

	public void addCompletedTask(int taskID) {
		if(this.saveData.getCompletedTasks().get(taskID) != null) return;

		this.saveData.getCompletedTasks().put(taskID, 0);
	}

	public List<Task> filterCompleteTasks(List<Task> taskList) {
		return taskList.stream().filter(t -> this.saveData.getCompletedTasks().get(t.getId()) == null).collect(Collectors.toList());
	}

	private void activateTaskList() {
		this.taskListTab.setSprites(TASKLIST_TAB_HOVER_SPRITE_ID);
		this.taskDashboardTab.setSprites(DASHBOARD_TAB_SPRITE_ID, DASHBOARD_TAB_HOVER_SPRITE_ID);
		this.taskDashboard.setVisibility(false);
		this.taskList.setVisibility(true);
	}

	private void activateTaskDashboard() {
		this.taskDashboardTab.setSprites(DASHBOARD_TAB_HOVER_SPRITE_ID);
		this.taskListTab.setSprites(TASKLIST_TAB_SPRITE_ID, TASKLIST_TAB_HOVER_SPRITE_ID);
		this.taskDashboard.setVisibility(true);
		this.taskList.setVisibility(false);
	}

	public void playFailSound() {
		client.playSoundEffect(2277);
	}

	public void setTaskCompletionPercent() {
		if(taskDashboard == null) return;

		int percent = (int) Math.round(((double) saveData.getCompletedTasks().keySet().size() / (double) this.tasks.length) * 100);
		taskDashboard.setCompletion(percent);
	}

	@Provides
	GenerateTaskConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GenerateTaskConfig.class);
	}
}
