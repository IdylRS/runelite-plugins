package com.survivalist;

import lombok.Getter;

import java.awt.*;

@Getter
public enum Hunger {
    FULL("Full", 100, 80, Color.GREEN),
    PECKISH("Peckish", 80, 60, Color.YELLOW),
    HUNGRY("Hungry", 60, 30, Color.ORANGE),
    STARVING("Starving", 30, -1, Color.RED);

    private String name;
    private int startHunger;
    private int endHunger;
    private Color color;

    Hunger(String name, int startHunger, int endHunger, Color color) {
        this.endHunger = endHunger;
        this.startHunger = startHunger;
        this.name = name;
        this.color = color;
    }

    public static Hunger getHunger(int hunger) {
        for(Hunger h : Hunger.values()) {
            if(hunger <= h.startHunger && hunger > h.endHunger) return h;
        }

        return FULL;
    }
}
