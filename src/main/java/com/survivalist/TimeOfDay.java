package com.survivalist;

import lombok.Getter;

@Getter
public enum TimeOfDay {
    DAWN("Dawn", 500, 700, 150),
    DAY("Day", 700, 1800, 255),
    DUSK("Dusk", 1800, 2000, 120),
    NIGHT("Night", 20, 500, 25);

    private String name;
    private int startTick;
    private int endTick;
    private int darkness;

    TimeOfDay(String name, int startTick, int endTick, int darkness) {
        this.name = name;
        this.startTick = startTick;
        this.endTick = endTick;
        this.darkness = darkness;
    }

    public static TimeOfDay getTimeOfDay(int gameTime) {
        if(gameTime >= NIGHT.startTick || gameTime < NIGHT.endTick) return NIGHT;

        for(TimeOfDay t : TimeOfDay.values()) {
            if(gameTime >= t.startTick && gameTime < t.endTick) {
                return t;
            }
        }

        return DAY;
    }
}
