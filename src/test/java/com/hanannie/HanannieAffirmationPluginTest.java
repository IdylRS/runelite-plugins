package com.hanannie;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HanannieAffirmationPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HanannieAffirmationPlugin.class);
		RuneLite.main(args);
	}
}