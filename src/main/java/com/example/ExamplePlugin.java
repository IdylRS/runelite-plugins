package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import okhttp3.*;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	private List<WorldPoint> worldPoints = Arrays.asList(new WorldPoint(3048, 3230, 1),new WorldPoint(2977, 3239, 0),new WorldPoint(3009, 3341, 0),
			new WorldPoint(3015, 3300, 0),new WorldPoint(3114, 3187, 0),new WorldPoint(3259, 3228, 0),
			new WorldPoint(3194, 3258, 0),new WorldPoint(3175, 3330, 0),new WorldPoint(3124, 3270, 0),
			new WorldPoint(3109, 3356, 0),new WorldPoint(3081, 3421, 0),new WorldPoint(3019, 3472, 0));

	private static final int POWER_UP_MODEL_ID = 42902;

	private final String baseUrl = "http://44.212.22.140:8080";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Gson gson;

	@Inject
	private OkHttpClient okHttpClient;

	private HashMap<WorldPoint, RuneLiteObject> powerUps = new HashMap<>();
	private HashMap<WorldPoint, Lootbeam> lootbeams = new HashMap<>();

	private List<WorldPoint> pickedUp = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			createRuneliteObjects();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		WorldPoint location = client.getLocalPlayer().getWorldLocation();
		Player opponent = null;

		if(config.opponent() != "") {
			for(Player p : client.getPlayers()) {
				if(p.getName().equalsIgnoreCase(config.opponent())) {
					opponent = p;
					break;
				}
			}
		}

		for(WorldPoint point : powerUps.keySet()) {
			if(point.distanceTo(location) == 0) {
				powerUps.remove(point).setActive(false);
				lootbeams.remove(point).remove();

				client.playSoundEffect(SoundEffectID.ITEM_PICKUP);
				updatePoints(point);
			}
			else if(opponent != null && point.distanceTo(opponent.getWorldLocation()) == 0) {
				powerUps.remove(point).setActive(false);
				lootbeams.remove(point).remove();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {

	}

	protected void updatePoints(WorldPoint point)
	{
		String url = baseUrl.concat("/points");

		try
		{
			Request r = new Request.Builder()
					.url(url)
					.post(RequestBody.create(JSON, gson.toJson(point)))
					.build();

			okHttpClient.newCall(r).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					log.debug("Error sending post data", e);
				}

				@Override
				public void onResponse(Call call, Response response)
				{
					if (response.isSuccessful())
					{
						log.debug("Successfully sent data");
						response.close();
					}
					else
					{
						log.debug("Post request unsuccessful");
						response.close();
					}
				}
			});
		}
		catch (IllegalArgumentException e)
		{
			log.error("Bad URL given: " + e.getLocalizedMessage());
		}
	}

	@Schedule(
			period = 3,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void getPoints() {
		if(client.getGameState().equals(GameState.LOGIN_SCREEN)) return;

		try {
			Request r = new Request.Builder()
					.url(baseUrl.concat("/points"))
					.get()
					.build();

			okHttpClient.newCall(r).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					log.info("Error getting points", e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if(response.isSuccessful()) {
						try
						{
							pickedUp = Arrays.asList(gson.fromJson(response.body().string(), WorldPoint[].class));
							removeObjects();
						}
						catch (IOException | JsonSyntaxException e)
						{
							log.error(e.getMessage());
						}
					}

					response.close();
				}
			});
		}
		catch(IllegalArgumentException e) {
			log.error("Bad URL given: " + e.getLocalizedMessage());
		}
	}

	private void removeObjects() {
		pickedUp.forEach(this::removeObject);
	}

	private void removeObject(WorldPoint point) {
		RuneLiteObject obj = powerUps.remove(point);
		if(obj != null) {
			obj.setActive(false);
			lootbeams.remove(point).remove();
		}
	}

	private void createRuneliteObjects() {
		worldPoints.forEach(this::createRuneliteObject);
	}

	private void createRuneliteObject(WorldPoint point) {
		RuneLiteObject object = client.createRuneLiteObject();

		LocalPoint loc = LocalPoint.fromWorld(client, point);
		if (loc == null || pickedUp.contains(point))
		{
			log.info("here");
			removeObject(point);
			return;
		}

		Model model = client.loadModel(POWER_UP_MODEL_ID);

		if (model == null)
		{
			final Instant loadTimeOutInstant = Instant.now().plus(Duration.ofSeconds(5));

			clientThread.invoke(() ->
			{
				if (Instant.now().isAfter(loadTimeOutInstant))
				{
					return true;
				}

				Model reloadedModel = client.loadModel(POWER_UP_MODEL_ID);

				if (reloadedModel == null)
				{
					return false;
				}

				object.setModel(reloadedModel);

				return true;
			});
		}
		else {
			object.setModel(model);
		}

		object.setLocation(loc, point.getPlane());
		object.setActive(true);
		log.info("Created @"+point);
		powerUps.put(point, object);
		addLootbeam(point);
	}

	private void addLootbeam(WorldPoint worldPoint)
	{
		Lootbeam lootbeam = lootbeams.get(worldPoint);
		if (lootbeam == null)
		{
			lootbeam = new Lootbeam(client, clientThread, worldPoint, Color.CYAN, Lootbeam.Style.MODERN);
			lootbeams.put(worldPoint, lootbeam);
		}
		else
		{
			lootbeam.setColor(Color.CYAN);
			lootbeam.setStyle(Lootbeam.Style.MODERN);
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
