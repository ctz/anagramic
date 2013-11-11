package com.ifihada.anagramic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;

@SuppressWarnings("deprecation")
public class WordButtonView extends AbsoluteLayout
{
	public WordButtonView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public WordButtonView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public WordButtonView(Context context)
	{
		super(context);
	}
	
	public void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, r, b);
	}
	
	// --- Generic handling
	private int screenWidth;	
	private int shown;
	private int[] returnIndices;
	private LayoutParams[] layouts;
	private static String EMPTY = "";
	
	private Button makeButton(LayoutInflater li, String c)
	{
		Button b = (Button) li.inflate(R.layout.letterbutton, null);
		if (GlobalSettings.useUpperCase)
		  c = c.toUpperCase(GlobalSettings.locale);
		b.setText(c);
		return b;
	}
	
	public void fixCase()
	{
	  this.reLayout();
	}
	
	public void fillForSelect(LayoutInflater li, int n)
	{
		this.fill(li, n, null);
		this.setAllVisibilityFast(View.INVISIBLE);
	}
	
	public void fillForLetters(LayoutInflater li, char [] letters)
	{
		this.fill(li, letters.length, letters);
	}
	
	private void fill(LayoutInflater li, int n, char [] letters)
	{
		assert (n == 0 && letters == null) || n == letters.length;
		this.removeAllViews();
		
		// set up return indices
		this.returnIndices = new int[n];
		this.resetReturns();
		
		// set up layout params
		this.layouts = new LayoutParams[n];
		
		// make buttons
		for (int i = 0; i < n; i++)
		{
			Button b = this.makeButton(li,
					letters == null ?
							EMPTY :
							String.valueOf(letters[i]));
			this.layouts[i] = this.makeLayout(i, n);
			this.addView(b, this.layouts[i]);
		}
	}
	
	public void setScreenWidth(int sw)
	{
		this.screenWidth = sw;
		this.reLayout();
		this.requestLayout();
	}
	
	private void reLayout()
	{
		int n = this.getChildCount();
		int eachWidth = this.screenWidth / n;
		
		for (int i = 0; i < n; i++)
		{
			LayoutParams lp = this.layouts[i];
			lp.width = eachWidth;
			lp.x = eachWidth * i;
		}
		
		for (int i = 0; i < n; i++)
		{
			Button b = (Button) this.getChildAt(i);
			b.setTextSize(TypedValue.COMPLEX_UNIT_SP, eachWidth < 60 ? 26 : 32);
			String text = b.getText().toString();
			if (GlobalSettings.useUpperCase)
			  text = text.toUpperCase(GlobalSettings.locale);
			else
			  text = text.toLowerCase(GlobalSettings.locale);
			b.setText(text);
		}
	}
	
	private AbsoluteLayout.LayoutParams makeLayout(int index, int max)
	{
		AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(0, 0, 0, 0);
		lp.height = AbsoluteLayout.LayoutParams.FILL_PARENT;
		return lp;
	}
	
	public String getWord()
	{
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < this.getChildCount(); i++)
    	{
    		Button b = (Button) this.getChildAt(i);
    		if (b.getVisibility() == View.VISIBLE)
    			sb.append(b.getText());
    	}
    	return sb.toString().toLowerCase(GlobalSettings.locale);
	}
	
	public void permute()
	{
		Util.permute(this.layouts);
		this.reLayout();
		this.requestLayout();
		this.invalidate();
	}
	
	private void setAllVisibility(int visiblity, int focus)
	{
		for (int i = 0; i < this.getChildCount(); i++)
		{
			Button b = (Button) this.getChildAt(i);
			AnimationUtil.setVisibility(b, visiblity, focus, i);
		}
	}
	
	private void setAllVisibilityFast(int v)
	{
		for (int i = 0; i < this.getChildCount(); i++)
		{
			Button b = (Button) this.getChildAt(i);
			b.setVisibility(v);
		}
	}
	
	// --- Bottom view style
	public int hideButton(Button b)
	{
		int idx = this.indexOfChild(b);
		assert idx != -1;
		AnimationUtil.setVisibility(b, View.INVISIBLE, View.FOCUS_DOWN);
		return idx;
	}
	
	public void reshowButton(int idx)
	{
		Button b = (Button) this.getChildAt(idx);
		if (b == null)
			return;
		AnimationUtil.setVisibility(b, View.VISIBLE, View.FOCUS_DOWN);
	}
	
	public void showAll()
	{
		this.setAllVisibility(View.VISIBLE, View.FOCUS_DOWN);
	}
	
	// --- Top view style
	public void pushButton(Button b, int returnIndex)
	{
		if (b == null)
			return;
		int i = this.shown;
		this.shown++;
		Button candidate = (Button) this.getChildAt(i);
		candidate.setText(b.getText());
		AnimationUtil.setVisibility(candidate, View.VISIBLE, View.FOCUS_UP);
		this.returnIndices[i] = returnIndex;
	}
	
	public int returnButton(Button b)
	{
		int idx = this.indexOfChild(b);
		int ridx = this.returnIndices[idx];
		AnimationUtil.setVisibility(b, View.INVISIBLE, View.FOCUS_UP);
		assert ridx != -1;
		this.returnIndices[idx] = -1;
		this.shown--;
		this.compress();
		return ridx;
	}
	
	private void compress()
	{
		while (this.hasSpaces())
			this.shiftLeft();
	}
	
	private boolean hasSpaces()
	{
		int invis = 0;
		for (int i = 0; i < this.getChildCount(); i++)
		{
			Button b = (Button) this.getChildAt(i);
			if (b.getVisibility() == View.INVISIBLE)
				invis++;
			if (b.getVisibility() == View.VISIBLE)
			{
				if (invis > 0)
					return true;
			}	
		}

		return false;
	}
	
	private void shiftLeft()
	{
		for (int i = this.getChildCount() - 1; i > 0; i--)
		{
			Button b = (Button) this.getChildAt(i);
			Button bl = (Button) this.getChildAt(i - 1);
			if (b.getVisibility() == View.VISIBLE &&
				bl.getVisibility() == View.INVISIBLE)
			{
				bl.setText(b.getText());
				AnimationUtil.setVisibility(bl, View.VISIBLE, View.FOCUS_UP);
				AnimationUtil.setVisibility(b, View.INVISIBLE, View.FOCUS_UP);
				this.returnIndices[i - 1] = this.returnIndices[i];
			}
		}
	}
	
	public void hideAll()
	{
		this.setAllVisibility(View.INVISIBLE, View.FOCUS_UP);
		this.resetReturns();
	}
	
	private void resetReturns()
	{
		for (int i = 0; i < this.returnIndices.length; i++)
			this.returnIndices[i] = -1;
		this.shown = 0;
	}

	public Button findWithLabel(String s)
	{
		for (int i = 0; i < this.getChildCount(); i++)
		{
			Button b = (Button) this.getChildAt(i);
			if (b.getVisibility() == View.VISIBLE && b.getText().equals(s))
				return b;
		}
		return null;
	}
}