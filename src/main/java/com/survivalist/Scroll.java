package com.survivalist;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;

public class Scroll {
    private static final int GROUND_MODEL = 20765;

    private Client client;
    private LocalPoint location;

    @Getter
    private Skill skill;

    private RuneLiteObject obj;

    public Scroll(Client client, LocalPoint location, Skill skill) {
        this.client = client;
        this.location = location;
        this.skill = skill;

        this.obj = client.createRuneLiteObject();
        Model model = client.loadModel(GROUND_MODEL);
        obj.setModel(model);
        obj.setLocation(location, client.getPlane());
        obj.setActive(true);
    }

    public void setActive(boolean active) {
        this.obj.setActive(active);
    }
}
