package com.ifihada.anagramic;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Util
{
	public static void setBackground(Activity a)
	{
		a.getWindow().setBackgroundDrawableResource(R.drawable.background);
	} 
	
	public static long getTimeMillis()
	{
		return SystemClock.uptimeMillis();
	}
	
	public static long getTimeSecs()
	{
		return SystemClock.uptimeMillis() / 1000;
	}
	
	private static void toast(Context ctx, String what, int id)
	{
		LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Toast t = new Toast(ctx);
		View vv = li.inflate(id, null);
		TextView tv = (TextView) vv.findViewById(R.id.ToastText);
		tv.setText(what);
		t.setView(vv);
		t.show();
	}
	
	public static void toast(Context ctx, String what)
	{
		Util.toast(ctx, what, R.layout.toast_normal);
	}
	
	public static void toast(Context ctx, String what, boolean positive)
	{
		Util.toast(ctx, what, positive ? R.layout.toast_positive : R.layout.toast_negative);
	}
	
	public static Random rand = new Random();

	public static <T> void permute(T[] r)
	{
		for (int i = r.length; i > 1; i--)
		{
			int swapto = Util.rand.nextInt(r.length);
			T tmp = r[swapto];
			r[swapto] = r[i - 1];
			r[i - 1] = tmp;
		}
	}
	
	// -- can't use generics for these without tedious boxing
	public static void permute(char[] r)
	{
		for (int i = r.length; i > 1; i--)
		{
			int swapto = Util.rand.nextInt(r.length);
			char tmp = r[swapto];
			r[swapto] = r[i - 1];
			r[i - 1] = tmp;
		}
	}
}
