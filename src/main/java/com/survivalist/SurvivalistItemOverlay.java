package com.survivalist;

import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.text.ParseException;
import javax.inject.Inject;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

public class SurvivalistItemOverlay extends WidgetItemOverlay {

    private final ItemManager itemManager;
    private SurvivalistPlugin survivalistPlugin;
    private final Cache<Long, Image> fillCache;

    @Inject
    SurvivalistItemOverlay(ItemManager itemManager, SurvivalistPlugin plugin)
    {
        this.itemManager = itemManager;
        this.survivalistPlugin = plugin;
        showOnEquipment();
        showOnInventory();
        fillCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(32)
                .build();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        final Color color = Color.GRAY;

        if(!survivalistPlugin.isItemUnlocked(itemId)) {
            Rectangle bounds = widgetItem.getCanvasBounds();
            final Image image = getFillImage(color, widgetItem.getId(), widgetItem.getQuantity());
            graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
        }
    }

    private Image getFillImage(Color color, int itemId, int qty)
    {
        long key = (((long) itemId) << 32) | qty;
        Image image = fillCache.getIfPresent(key);
        if (image == null)
        {
            final Color fillColor = ColorUtil.colorWithAlpha(color, 150);
            image = ImageUtil.fillImage(itemManager.getImage(itemId, qty, false), fillColor);
            fillCache.put(key, image);
        }
        return image;
    }

    void invalidateCache()
    {
        fillCache.invalidateAll();
    }
}
