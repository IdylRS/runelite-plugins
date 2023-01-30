package com.survivalist;

import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class StatusEffectInfobox extends InfoBox {
    private static final List<StatusEffect> skipRender = Arrays.asList(StatusEffect.STARVING, StatusEffect.HUNGRY);

    private StatusEffect effect;
    private SurvivalistPlugin plugin;

    public StatusEffectInfobox(StatusEffect effect, BufferedImage image, SurvivalistPlugin plugin) {
        super(image, plugin);
        this.plugin = plugin;
        this.effect = effect;

        this.setTooltip(effect.getName());
    }

    @Override
    public String getText() {
        return effect.isShowTicksRemaining() ? plugin.getStatusEffects().get(effect)+"" : "";
    }

    @Override
    public Color getTextColor() {
        return effect.getLpPerTick() < 0 ? Color.red : Color.GREEN;
    }

    @Override
    public boolean render() {
        return plugin.getStatusEffects().get(effect) > 0 && !skipRender.contains(effect);
    }
}
