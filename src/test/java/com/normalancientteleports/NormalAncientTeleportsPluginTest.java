package com.normalancientteleports;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NormalAncientTeleportsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NormalAncientTeleportsPlugin.class);
		RuneLite.main(args);
	}
}