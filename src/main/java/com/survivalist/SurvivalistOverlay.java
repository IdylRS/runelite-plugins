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
    private final LineComponent timeOfDayComponent;
    private final LineComponent hungerComponent;
    private final LineComponent lifePointsComponent;
    private final LineComponent bossNameComponent;

    public SurvivalistOverlay(SurvivalistPlugin plugin, SurvivalistConfig config) {
        this.plugin = plugin;
        this.config = config;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);

        timeOfDayComponent = LineComponent.builder().left("Time of Day:").right("").build();
        hungerComponent = LineComponent.builder().left("Hunger:").right("").build();
        lifePointsComponent = LineComponent.builder().left("Life Points:").right("").build();
        bossNameComponent = LineComponent.builder().left("Age Boss:").right("").build();
        titleComponent = TitleComponent.builder().text("Survivalist").build();

        panelComponent.getChildren().addAll(Arrays.asList(titleComponent, lifePointsComponent, timeOfDayComponent, hungerComponent, bossNameComponent));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setFont(FontManager.getRunescapeFont());
        TimeOfDay tod = TimeOfDay.getTimeOfDay(plugin.getUnlockData().getGameTime());
        Hunger hunger = Hunger.getHunger(plugin.getUnlockData().getHunger());
        String age = plugin.getUnlockData() != null ? plugin.getUnlockData().getAge().getName() : "Survivalist";
        String boss = plugin.getUnlockData() != null ? plugin.getAgeBossName() : "???";

        bossNameComponent.setRight(boss);

        lifePointsComponent.setRight((int) Math.ceil(plugin.getUnlockData().getLifePoints()/10)+"LP");
        lifePointsComponent.setRightColor(getLifePointsColor(plugin.getUnlockData().getLifePoints()));

        timeOfDayComponent.setRight(tod.getName());
        timeOfDayComponent.setRightColor(getTimeOfDayColor(tod));

        hungerComponent.setRight(hunger.getName());
        hungerComponent.setRightColor(hunger.getColor());

        titleComponent.setText(age);

        if(config.showAgeBoss()) {
            panelComponent.getChildren().addAll(Arrays.asList(titleComponent, lifePointsComponent, timeOfDayComponent, hungerComponent, bossNameComponent));
        }
        else {
            panelComponent.getChildren().addAll(Arrays.asList(titleComponent, lifePointsComponent, timeOfDayComponent, hungerComponent));
        }

        return super.render(graphics);
    }

    private Color getTimeOfDayColor(TimeOfDay tod) {
        switch(tod) {
            case NIGHT:
                return Color.CYAN;
            case DUSK:
            case DAWN:
                return Color.ORANGE;
            case DAY:
            default:
                return Color.GREEN;
        }
    }

    private Color getLifePointsColor(int lifePoints) {
        if(lifePoints >= 750) return Color.GREEN;
        else if(lifePoints >= 500) return Color.YELLOW;
        else if(lifePoints >= 200) return Color.ORANGE;
        else return Color.RED;
    }
}
