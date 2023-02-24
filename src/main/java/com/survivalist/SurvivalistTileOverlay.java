package com.survivalist;

import com.google.common.base.Strings;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;

public class SurvivalistTileOverlay extends Overlay {
    private Client client;
    private SurvivalistPlugin plugin;
    private SurvivalistConfig config;


    @Inject
    private SurvivalistTileOverlay(Client client, SurvivalistPlugin plugin, SurvivalistConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Stroke stroke = new BasicStroke((float) 2);
        if(plugin.nearbyFires.size() > 0 && config.drawFires()) {
            plugin.nearbyFires.forEach((t) -> drawTile(graphics, t, Color.GREEN, null, stroke));
        }
        return null;
    }

    private void drawTile(Graphics2D graphics, Tile tile, Color color, @Nullable String label, Stroke borderStroke)
    {
        if(plugin.closestFire == null) return;

        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        WorldPoint point = tile.getWorldLocation();

        int distanceToClosest = plugin.closestFire.getWorldLocation().distanceTo(point);

        Color tileColor = plugin.nearbyWarmingFires.contains(tile) ? Color.ORANGE : color;
        Color borderColor = distanceToClosest == 0 ? Color.RED : tileColor;

        if (point.distanceTo(playerLocation) >= 32)
        {
            return;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null)
        {
            OverlayUtil.renderPolygon(graphics, poly, borderColor, new Color(0, 0, 0, 0), borderStroke);
        }

        if (!Strings.isNullOrEmpty(label))
        {
            Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
            if (canvasTextLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, borderColor);
            }
        }
    }
}
