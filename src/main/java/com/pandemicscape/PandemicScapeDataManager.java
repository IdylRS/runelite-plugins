package com.pandemicscape;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Singleton
public class  PandemicScapeDataManager {
    private final String baseUrl = "http://localhost:8080";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private PandemicScapePlugin plugin;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private Gson gson;

    protected void updatePandemicScapeApi(PandemicScapeData data)
    {
        String username = urlifyString(data.getUsername());
        String url = baseUrl.concat("/infected/u/"+username);

        try
        {
            Request r = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, gson.toJson(data)))
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
                        log.debug("Successfully sent pandemic scape data");
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

    public void getInfectedByUsernames(List<String> players) {
        if(players == null || players.size() == 0) {
            return;
        }

        String playersString = urlifyString(String.join(",", players));

        try {
            Request r = new Request.Builder()
                    .url(baseUrl.concat("/infected/u/".concat(playersString)))
                    .get()
                    .build();

            okHttpClient.newCall(r).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.info("Error getting pandemic scape data by username", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()) {
                        try
                        {
                            String body = response.body().string();
                            JsonArray j = gson.fromJson(body, JsonArray.class);
                            HashMap<String, PandemicScapeData> playerData = parsePropHuntData(j);
                            plugin.onPlayerDataReceived(playerData);
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

    private HashMap<String, PandemicScapeData> parsePropHuntData(JsonArray j) {
        HashMap<String, PandemicScapeData> l = new HashMap<>();
        for (JsonElement jsonElement : j)
        {
            JsonObject jObj = jsonElement.getAsJsonObject();
            String username = jObj.get("username").getAsString();
            JsonObject worldPointObj = jObj.get("infectionPoint").getAsJsonObject();
            WorldPoint point = new WorldPoint(
                    worldPointObj.get("x").getAsInt(),
                    worldPointObj.get("y").getAsInt(),
                    worldPointObj.get("plane").getAsInt()
            );
            PandemicScapeData d = new PandemicScapeData(
                    jObj.get("username").getAsString(),
                    jObj.get("infectedDateTime").getAsString(),
                    jObj.get("infectedBy").getAsString(),
                    jObj.get("numberInfected").getAsInt(),
                    point
            );
            l.put(username, d);
        }
        return l;
    }

    private String urlifyString(String str) {
        if(str == null) return "";
        return str.trim().replaceAll("\\s", "%20");
    }
}
