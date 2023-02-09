package com.survivalist;

import lombok.Getter;

@Getter
public enum TimeOfDay {
    DAWN("Dawn", 0, 200, 150),
    DAY("Day", 200, 1300, 255),
    DUSK("Dusk", 1500, 1700, 120),
    NIGHT("Night", 1700, 2400, 25);

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
        for(TimeOfDay t : TimeOfDay.values()) {
            if(gameTime >= t.startTick && gameTime < t.endTick) {
                return t;
            }
        }

        return DAY;
    }
}
