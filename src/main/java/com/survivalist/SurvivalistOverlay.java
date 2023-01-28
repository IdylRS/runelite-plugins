package com.survivalist;

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.awt.*;
import java.util.Arrays;

public class SurvivalistOverlay extends OverlayPanel {
    private SurvivalistPlugin plugin;

    private final LineComponent timeOfDayComponent;
    private final LineComponent hungerComponent;
    private final LineComponent lifePointsComponent;

    public SurvivalistOverlay(SurvivalistPlugin plugin) {
        this.plugin = plugin;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.BOTTOM_LEFT);

        timeOfDayComponent = LineComponent.builder().left("Time of Day:").right("").build();
        hungerComponent = LineComponent.builder().left("Hunger:").right("").build();
        lifePointsComponent = LineComponent.builder().left("Life Points:").right("").build();

        panelComponent.getChildren().addAll(Arrays.asList(lifePointsComponent, timeOfDayComponent, hungerComponent));
        setClearChildren(false);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setFont(FontManager.getRunescapeFont());
        TimeOfDay tod = TimeOfDay.getTimeOfDay(plugin.getGameTime());
        Hunger hunger = Hunger.getHunger(plugin.getHunger());

        lifePointsComponent.setRight(plugin.getLifePoints()+"LP");
        lifePointsComponent.setRightColor(getLifePointsColor(plugin.getLifePoints()));

        timeOfDayComponent.setRight(tod.getName());
        timeOfDayComponent.setRightColor(getTimeOfDayColor(tod));

        hungerComponent.setRight(hunger.getName());
        hungerComponent.setRightColor(hunger.getColor());

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
