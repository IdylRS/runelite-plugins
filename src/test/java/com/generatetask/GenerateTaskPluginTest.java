package com.generatetask;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GenerateTaskPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GenerateTaskPlugin.class);
		RuneLite.main(args);
	}
}