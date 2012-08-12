package com.ifihada.anagramic;

import android.content.Context;
import android.media.MediaPlayer;

class SoundEngine
{
	public MediaPlayer harp_up;
	public MediaPlayer harp_up_down;
	public MediaPlayer harp_down;
	public MediaPlayer harp_end;

	SoundEngine()
	{
		this.unloadSounds();
	}
	
	void loadSounds(Context ctx)
	{
		this.harp_up = MediaPlayer.create(ctx, R.raw.harp_up);
		this.harp_up_down = MediaPlayer.create(ctx, R.raw.harp_up_down);
		this.harp_down = MediaPlayer.create(ctx, R.raw.harp_down);
		this.harp_end = MediaPlayer.create(ctx, R.raw.harp_end);
	}
	
	void unloadSounds()
	{
		if (this.harp_up != null)
			this.harp_up.release();
		if (this.harp_up_down != null)
			this.harp_up_down.release();
		if (this.harp_down != null)
			this.harp_down.release();
		if (this.harp_end != null)
			this.harp_end.release();
		this.harp_up = null;
		this.harp_up_down = null;
		this.harp_down = null;
		this.harp_end = null;
	}
	
	public void play(MediaPlayer what)
	{
		if (what == null)
			return;
		what.start();
	}
}

public class SoundUtil
{
	private static SoundEngine engine = new SoundEngine();
	
	public static void playJingle(int wordlen)
	{
		switch (wordlen)
		{
		case 5:
		case 6:
			SoundUtil.engine.play(SoundUtil.engine.harp_up);
			break;
			
		case 7:
		case 8:
		case 9:
			SoundUtil.engine.play(SoundUtil.engine.harp_up_down);
			break;
		}
	}
	
	public static void playGoodFinishSound()
	{
		SoundUtil.engine.play(SoundUtil.engine.harp_end);
	}
	
	public static void playBadFinishSound()
	{
		SoundUtil.engine.play(SoundUtil.engine.harp_down);
	}
	
	public static void loadSounds(Context ctx)
	{
		SoundUtil.engine.loadSounds(ctx);
	}
	
	public static void unloadSounds()
	{
		SoundUtil.engine.unloadSounds();
	}
}
