package com.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HitsplatID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private OkHttpClient okHttpClient;

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
	public void onHitsplatApplied(HitsplatApplied e) throws IOException {
		if(e.getHitsplat().isMine() && e.getHitsplat().getHitsplatType() == HitsplatID.DAMAGE_ME) {
			double max = 10;
			double value = ( (double) e.getHitsplat().getAmount() / max) * 1000;

			log.info("Setting res to "+value);

			URL url = new URL("http://"+config.ip()+"/resist?val="+Math.floor(value));

			try {
				Request r = new Request.Builder()
						.url(url)
						.get()
						.build();

				okHttpClient.newCall(r).enqueue(new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						log.info("Error setting LED ", e);
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if(response.isSuccessful()) {
							log.info("Success!");
						}

						response.close();
					}
				});
			}
			catch(IllegalArgumentException err) {
				log.error("Bad URL given: " + err.getLocalizedMessage());
			}
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
