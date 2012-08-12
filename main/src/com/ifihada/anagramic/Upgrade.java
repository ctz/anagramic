package com.ifihada.anagramic;

import android.content.Context;
import android.content.pm.PackageManager;

public class Upgrade
{
	public static String WORDGAME = "com.ifihada.anagramic";
	public static String WORDGAME_UPGRADE = "com.ifihada.anagramic.upgrade";
	public static boolean OK = false;
	public static final boolean Debug = false;
	
	public static boolean check(Context ctx)
	{
		Upgrade.OK = ctx.getPackageManager().checkSignatures(WORDGAME, WORDGAME_UPGRADE) == PackageManager.SIGNATURE_MATCH;
		return Upgrade.OK;
	}
	
	public static void perform()
	{
		WordDictionary.upgrade();
	}
	
	public static void cheat(Context ctx)
	{
		if (Upgrade.Debug)
		{
			Util.toast(ctx, "Cheating...");
			Upgrade.OK = true;
		}
	}
}
