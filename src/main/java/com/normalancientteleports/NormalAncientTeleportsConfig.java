package com.normalancientteleports;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("normal-ancient-teleports")
public interface NormalAncientTeleportsConfig extends Config
{
    @ConfigItem(
            keyName = "replacePortalNexus",
            name = "Replace Portal Nexus Spell names",
            description = "Replace the spell names in the portal nexus"
    )
    default boolean replacePortalNexus() { return true; }
}
