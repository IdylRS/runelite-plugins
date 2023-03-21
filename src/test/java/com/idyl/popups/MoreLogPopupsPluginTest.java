package com.idyl.popups;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MoreLogPopupsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MoreLogPopupsPlugin.class);
		RuneLite.main(args);
	}
}