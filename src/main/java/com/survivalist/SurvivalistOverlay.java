package com.survivalist;

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;
import java.util.Arrays;

public class SurvivalistOverlay extends OverlayPanel {
    private SurvivalistPlugin plugin;
    private SurvivalistConfig config;

    private final TitleComponent titleComponent;
    private final LineComponent bossNameComponent;

    public SurvivalistOverlay(SurvivalistPlugin plugin, SurvivalistConfig config) {
        this.plugin = plugin;
        this.config = config;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);

        bossNameComponent = LineComponent.builder().left("Age Boss:").right("").build();
        titleComponent = TitleComponent.builder().text("Survivalist").build();

        panelComponent.getChildren().addAll(Arrays.asList(titleComponent, bossNameComponent));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!config.showAgeInfobox()) return null;

        graphics.setFont(FontManager.getRunescapeFont());
        String age = plugin.getUnlockData() != null ? plugin.getUnlockData().getAge().getName() : "Survivalist";
        String boss = plugin.getUnlockData() != null ? plugin.getAgeBossName() : "???";

        bossNameComponent.setRight(boss);
        titleComponent.setText(age);

        if(config.showAgeBoss()) {
            panelComponent.getChildren().addAll(Arrays.asList(titleComponent, bossNameComponent));
        }
        else {
            panelComponent.getChildren().addAll(Arrays.asList(titleComponent));
        }

        return super.render(graphics);
    }
}
