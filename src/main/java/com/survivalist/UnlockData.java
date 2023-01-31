package com.survivalist;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Skill;

import java.util.HashMap;

@Getter
public class UnlockData {
    private static final int MAX_LIFE_POINTS = 1000;
    private static final int MAX_HUNGER = 100;
    private final int TICKS_PER_DAY = 2400;

    @Setter
    private boolean magicUnlocked;
    @Setter
    private boolean prayerUnlocked;
    @Setter
    private Age age;
    private int hunger;
    private int lifePoints;
    private int gameTime;
    private final HashMap<StatusEffect, Integer> statusEffects = new HashMap<>();

    UnlockData() {
        age = Age.STEEL_AGE;
        hunger = 0;
        lifePoints = 0;
        gameTime = 0;

        magicUnlocked = false;
        prayerUnlocked = false;
    }

    public void setDefaults() {
        hunger = MAX_HUNGER;
        lifePoints = MAX_LIFE_POINTS;
        gameTime = TimeOfDay.DAY.getStartTick();
    }

    public void sleep() {
        this.gameTime = TimeOfDay.DAWN.getStartTick();
    }

    public void updateGameTime() {
        this.gameTime = (this.gameTime+2) % TICKS_PER_DAY;
    }

    public void updateLifePoints() {
        for(StatusEffect effect : statusEffects.keySet()) {
            if(statusEffects.get(effect) > 0) {
                lifePoints = Math.max(0, Math.min(effect.getLpPerTick()+lifePoints, MAX_LIFE_POINTS));
            }
        }
    }

    public void updateHunger() {
        if(this.gameTime % 5 == 0) {
            hunger = Math.max(0, Math.min(MAX_HUNGER, hunger-1));
        }

        if(Hunger.getHunger(this.hunger) == Hunger.FULL) {
            statusEffects.put(StatusEffect.FULL, 1);
        }
        else {
            statusEffects.put(StatusEffect.FULL, 0);
        }

        if(Hunger.getHunger(this.hunger) == Hunger.HUNGRY) {
            statusEffects.put(StatusEffect.STARVING, 1);
        }
        else if(Hunger.getHunger(this.hunger) == Hunger.STARVING) {
            statusEffects.put(StatusEffect.STARVING, 1);
        }
        else {
            statusEffects.put(StatusEffect.HUNGRY, 0);
            statusEffects.put(StatusEffect.STARVING, 0);
        }
    }

    public void addHunger(int amount) {
        this.hunger = Math.max(0, Math.min(MAX_HUNGER, hunger+amount*5));
        this.hunger = Math.max(0, Math.min(MAX_HUNGER, hunger+amount*5));

        if(amount > 0)
            statusEffects.put(StatusEffect.EATING, Math.min(50, amount*2));
    }

    public void updateInjury(double ratio) {
        if(ratio <= .2) {
            statusEffects.put(StatusEffect.INJURED, 1);
        }
        else {
            statusEffects.put(StatusEffect.INJURED, 0);
        }
    }

}
