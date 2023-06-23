package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

@Slf4j
class FunBarOverlay extends Overlay
{
    private static final Color LIFE_POINTS_COLOR = new Color(58, 219, 0, 150);
    private static final Color HUNGER_COLOR = new Color(255, 149, 0, 150);
    private static final Color MAX_HUNGER_COLOR = new Color(255, 183, 0, 150);
    private static final Color TIME_COLOR = new Color(0, 221, 255, 150);
    private static final Color HEAL_COLOR = new Color(255, 112, 6, 150);
    private static final int HEIGHT = 252;
    private static final int WIDTH = 152;
    private static final Dimension ICON_DIMENSIONS = new Dimension(26, 25);
    private static final int MAX_LIFE_POINTS_VALUE = 1000;
    private static final int MAX_HUNGER_VALUE = 100;

    private final Client client;
    private final ExamplePlugin plugin;
    private final SpriteManager spriteManager;

    private Image lpIcon;
    private Image hungerIcon;
    private Image dayIcon;
    private Image nightIcon;
    private final List<BarRenderer> barRenderers = new ArrayList<>();

    FunBarOverlay(Client client, ExamplePlugin plugin, SpriteManager spriteManager)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.client = client;
        this.plugin = plugin;
        this.spriteManager = spriteManager;
        initRenderers();
    }

    private void initRenderers()
    {
        barRenderers.add(new BarRenderer(
                () -> MAX_LIFE_POINTS_VALUE,
                () -> plugin.getFun(),
                () -> 0,
                () -> LIFE_POINTS_COLOR,
                () -> HEAL_COLOR,
                () -> lpIcon,
                () -> Integer.toString(plugin.getFun())
        ));
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        Viewport curViewport = null;
        Widget curWidget = null;

        for (Viewport viewport : Viewport.values())
        {
            final Widget viewportWidget = client.getWidget(viewport.getViewport());
            if (viewportWidget != null && !viewportWidget.isHidden())
            {
                curViewport = viewport;
                curWidget = viewportWidget;
                break;
            }
        }

        if (curViewport == null)
        {
            return null;
        }

        buildIcons();

        final Point offsetLeft = curViewport.getOffsetLeft();
        final Point location = curWidget.getCanvasLocation();
        final int width, height, offsetLeftBarX, offsetLeftBarY, offsetRightBarX, offsetRightBarY;

        width = WIDTH;
        height = BarRenderer.DEFAULT_HEIGHT;
        Widget container = client.getWidget(curViewport.getViewport().getPackedId());
        offsetLeftBarX = (location.getX() + (container.getWidth() / 2 - (WIDTH / 2)));
        offsetLeftBarY = (location.getY() - offsetLeft.getY());

        int offset = 0;
        for(BarRenderer barRenderer : barRenderers) {
            if(barRenderer != null) {
                barRenderer.renderBar(g, offsetLeftBarX, offsetLeftBarY + (offset), width, height);
                offset += 20;
            }
        }

        return null;
    }

    private void buildIcons()
    {
        if (lpIcon == null)
        {
            lpIcon = loadAndResize(SpriteID.SPELL_CURE_ME);
        }
        if (hungerIcon == null)
        {
            hungerIcon = loadAndResize(SpriteID.SPELL_BONES_TO_PEACHES);
        }
        if(dayIcon == null) {
            dayIcon = loadAndResize(SpriteID.SPELL_TELE_GROUP_KHAZARD);
        }
        if(nightIcon == null) {
            nightIcon = loadAndResize(SpriteID.SPELL_TELE_GROUP_MOONCLAN);
        }
    }

    private BufferedImage loadAndResize(int spriteId)
    {
        BufferedImage image = spriteManager.getSprite(spriteId, 0);
        if (image == null)
        {
            return null;
        }

        return ImageUtil.resizeCanvas(image, ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
    }
}
