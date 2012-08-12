package com.ifihada.anagramic;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

class OutToBottomAnimation extends ScaleAnimation
{
	public OutToBottomAnimation(long offset)
	{
		super(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		this.setDuration(AnimationUtil.alphaAnimationTime);
		this.setStartOffset(offset);
	}
}

class OutToTopAnimation extends ScaleAnimation
{
	public OutToTopAnimation(long offset)
	{
		super(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
		this.setDuration(AnimationUtil.alphaAnimationTime);
		this.setStartOffset(offset);
	}
}

class InFromTopAnimation extends ScaleAnimation
{
	public InFromTopAnimation(long offset)
	{
		super(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
		this.setDuration(AnimationUtil.alphaAnimationTime);
		this.setStartOffset(offset);
	}
}

class InFromBottomAnimation extends ScaleAnimation
{
	public InFromBottomAnimation(long offset)
	{
		super(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		this.setDuration(AnimationUtil.alphaAnimationTime);
		this.setStartOffset(offset);
	}
}

public class AnimationUtil
{
	public static void setVisibility(View v, int vis, int direction)
	{
		setVisibility(v, vis, direction, 0);
	}
	
	public static void setVisibility(View v, int vis, int direction, int ordering)
	{
		if (v.getVisibility() != vis)
		{
			v.setVisibility(vis);
			v.setClickable(vis == View.INVISIBLE ? false : true);
			
			if (!AnimationUtil.animationEnabled)
				return;
			
			Animation a = null;
			int delay = ordering * animationDelay;
			
			switch (vis)
			{
			case View.VISIBLE:
				switch (direction)
				{
				case View.FOCUS_UP:
					a = new InFromTopAnimation(delay);
					break;
				case View.FOCUS_DOWN:
					a = new InFromBottomAnimation(delay);
					break;
				}
				break;
				
			case View.INVISIBLE:
				switch (direction)
				{
				case View.FOCUS_UP:
					a = new OutToTopAnimation(delay);
					break;
				case View.FOCUS_DOWN:
					a = new OutToBottomAnimation(delay);
					break;
				}
				break;
			}
			
			if (a != null)
				v.startAnimation(a);
		}
	}

	public static final int alphaAnimationTime = 150;
	public static final int animationDelay = 75;
	public static boolean animationEnabled = true;
}
