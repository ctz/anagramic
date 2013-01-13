package com.ifihada.anagramic;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

class LayoutEngine
{
	static final int minFontSize = 18;
	static final int maxFontSize = 64;
	static final int pad = 3;
	static final long timeout = 3000;
	
	Paint guessedStyle;
	Paint recentlyGuessedStyle;
	Paint cheatedStyle;
	Paint unknownStyle;
  Paint unknownHilightStyle;
  Paint unknownLolightStyle;
	float x, y, em, li, liOffs;
	int maxHeight;
	int largestWord;
	ArrayList<Integer> wordLengthList;
	int preferredFontSize, desiredWidth, requiredWidth;
	private long time;
	
	public LayoutEngine()
	{
		this.desiredWidth = 0;
		this.requiredWidth = 0;
		this.maxHeight = 0;
		this.wordLengthList = new ArrayList<Integer>();
		this.resetLayout();
		
		this.makeStyles(maxFontSize);
	}
	
	private void makeStyles(int fontsz)
	{
		guessedStyle = new Paint(0);
		guessedStyle.setAntiAlias(true);
		guessedStyle.setSubpixelText(true);
		guessedStyle.setColor(0xffffffff);
		guessedStyle.setTypeface(Typeface.MONOSPACE);
		guessedStyle.setFakeBoldText(false);
		guessedStyle.setShadowLayer(3, 0, 1, 0xcc000000);

		recentlyGuessedStyle = new Paint(guessedStyle);
		recentlyGuessedStyle.setShadowLayer(3, 0, 1, 0xff33ff33);
		recentlyGuessedStyle.setFakeBoldText(true);
		
		cheatedStyle = new Paint(guessedStyle);
		cheatedStyle.setShadowLayer(3, 0, 1, 0xffff3333);
    cheatedStyle.setFakeBoldText(true);
		
		unknownStyle = new Paint(0);
		unknownStyle.setAntiAlias(true);
		unknownStyle.setColor(0xff555555);
		// unknownStyle.setShadowLayer(3, 0, 3, 0x66000000);
    
    unknownHilightStyle = new Paint(0);
    unknownHilightStyle.setAntiAlias(true);
    unknownHilightStyle.setColor(0xffcccccc);
    
    unknownLolightStyle = new Paint(unknownHilightStyle);
    unknownLolightStyle.setAntiAlias(true);
    unknownLolightStyle.setColor(0xff000000);
		
		this.setFontSize(fontsz);
	}
	
	private void setFontSize(int fontsz)
	{
		this.guessedStyle.setTextSize(fontsz);
		this.recentlyGuessedStyle.setTextSize(fontsz);
		this.cheatedStyle.setTextSize(fontsz);
		this.em = this.guessedStyle.measureText("m");
		Paint.FontMetrics fm = this.guessedStyle.getFontMetrics();
		this.li = -fm.top + fm.bottom;
		this.liOffs = this.li / 2;
	}
	
	public void setupForLayout(int desiredWidth, int maxHeight)
	{
		this.wordLengthList.clear();
		this.desiredWidth = desiredWidth;
		this.requiredWidth = 0;
		this.maxHeight = maxHeight;
		this.resetLayout();
	}
	
	public void setupForDrawing(int fontSz, int maxHeight)
	{
		this.desiredWidth = 0;
		this.maxHeight = maxHeight;
		this.setFontSize(fontSz);
		this.resetLayout();
		this.time = Util.getTimeMillis();
	}
	
	public void addWords(int len, int n)
	{
		for (int i = 0; i < n; i++)
		{
			this.wordLengthList.add(len);
		}
	}
	
	private int currentWidth()
	{
		if (this.y <= this.li)
		{
			return (int)(this.x + (3 * this.em) + LayoutEngine.pad);
		} else {
			return (int)(this.x + ((this.largestWord + 3) * this.em) + LayoutEngine.pad);
		}
	}
	
	private void resetLayout()
	{
		this.largestWord = 0;
		this.x = LayoutEngine.pad;
		this.y = this.liOffs;
	}
	
	private void tryLayout()
	{
		int last = 0;
		this.setFontSize(this.preferredFontSize);
		this.resetLayout();
		for (Integer i : this.wordLengthList)
		{
			this.step(i);
			if (last == 0)
			{
				last = i;
			} else if (last != i) {
				this.step(last);
				last = i;
			}
		}
	}
	
	public void endLayout()
	{
		for (this.preferredFontSize = LayoutEngine.maxFontSize;
			 this.preferredFontSize > LayoutEngine.minFontSize;
			 this.preferredFontSize -= 4)
		{
			this.tryLayout();
			this.requiredWidth = this.currentWidth();
			if (this.requiredWidth < this.desiredWidth)
				break;
		}
	}
	
	public int getFontSize()
	{
		return this.preferredFontSize;
	}
	
	public int getWidth()
	{
		return this.requiredWidth;
	}
	
	public void renderWord(Canvas c, WordToGuess w)
	{
	  String wordStr = w.word;
	  
	  if (GlobalSettings.useUpperCase)
	    wordStr = wordStr.toUpperCase(GlobalSettings.locale);
	  
		if (w.cheated)
			c.drawText(wordStr, this.x, this.y + this.liOffs, this.cheatedStyle);
		else if (w.guessed && w.guessedAt + timeout > this.time)
			c.drawText(wordStr, this.x, this.y + this.liOffs, this.recentlyGuessedStyle);
		else if (w.guessed)
			c.drawText(wordStr, this.x, this.y + this.liOffs, this.guessedStyle);
		else
		{
			for (int i = 0; i < w.word.length(); i++)
			{
			  float x1 = this.x + this.em * i;
			  float x2 = this.x + (this.em * (i + 1)) - 2;
			  float y1 = this.y - 2.0f;
			  float y2 = y1 + this.liOffs * 1.5f;
				c.drawRect(x1, y1, x2, y2, this.unknownStyle);
        c.drawLine(x1 + 1, y1 - 1, x2 - 1, y1 - 1, this.unknownHilightStyle);
        c.drawLine(x1 + 1, y2, x2 - 1, y2, this.unknownLolightStyle);
			}
		}
		
		this.step(w.word.length());
	}
	
	public void space(int len)
	{
		if (this.y > this.liOffs + 0.1)
		{
			this.step(len);
		}
	}
	
	public void step(int len)
	{
		this.y += this.li;
		if (this.y + this.li + LayoutEngine.pad >= this.maxHeight)
		{
			this.wrap(len);
		}
		this.largestWord = len;
	}
	
	private void wrap(int len)
	{
		if (this.y > LayoutEngine.pad + 0.1)
		{
			this.y = this.liOffs;
			this.x += (len + 1) * em;
		}
	}
}

class LayoutCache
{
	class LayoutValues
	{
		boolean taken;
		int screenW, screenH, gameHash;
		int cachedFontSize, cachedWidth;
		
		boolean matches(int screenW, int screenH, int gameHash)
		{
			return (this.taken &&
					this.screenW == screenW &&
					this.screenH == screenH &&
					this.gameHash == gameHash);
		}

		public void write(int screenW, int screenH, int gameHash)
		{
			this.taken = true;
			this.screenW = screenW;
			this.screenH = screenH;
			this.gameHash = gameHash;
			this.cachedFontSize = 0;
			this.cachedWidth = 0;
		}
	}
	
	private final static int cacheSize = 4;
	private LayoutValues cache[];
	private int current;
	
	public LayoutCache()
	{
		this.cache = new LayoutValues[cacheSize];
		for (int i = 0; i < cacheSize; i++)
			this.cache[i] = new LayoutValues();
	}
	
	public boolean hasCache(int screenW, int screenH, int gameHash)
	{
		this.current = -1;
		
		for (int i = 0; i < cacheSize; i++)
		{
			if (this.cache[i].matches(screenW, screenH, gameHash))
			{
				this.current = i;
				return true;
			} else {
				if (this.cache[i].taken)
					continue;
				this.cache[i].write(screenW, screenH, gameHash);
				this.current = i;
				return false;
			}
		}
		
		/* If we have no slots available, empty them all. */
		for (int i = 0; i < cacheSize; i++)
			this.cache[i].taken = false;
		
		return this.hasCache(screenW, screenH, gameHash);
	}
	
	public void saveFontSize(int fontSize)
	{
		if (this.current != -1)
		{
			this.cache[this.current].cachedFontSize = fontSize;
		}
	}
	
	public void saveWidth(int width)
	{
		if (this.current != -1)
		{
			this.cache[this.current].cachedWidth = width;
		}
	}
	
	public int getFontSize()
	{
		if (this.current != -1)
		{
			return this.cache[this.current].cachedFontSize;
		}
		return 0;
	}
	
	public int getWidth()
	{
		if (this.current != -1)
		{
			return this.cache[this.current].cachedWidth;
		}
		return 0;
	}
}

public class FoundView extends View
{
	private WordGameAct act;
	private int fontsize;
	private LayoutCache cache;
	private LayoutEngine layout;
	
	public FoundView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FoundView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FoundView(Context context)
	{
		super(context);
	}

	public void setActivity(WordGameAct act)
	{
		this.act = act;
		this.cache = new LayoutCache();
		this.layout = new LayoutEngine();
	}
	
	private void doLayout()
	{
		this.layout.setupForLayout(this.act.getScreenWidth(), this.getMeasuredHeight());
		
		for (int i = 2; i < this.act.game.allwords.size(); i++)
		{
			Map<String, WordToGuess> wg = this.act.game.allwords.get(i);
			this.layout.addWords(i, wg.size());
			if (wg.size() > 0)
				this.layout.space(i);
		}
		this.layout.endLayout();
		
		this.cache.saveWidth(this.layout.getWidth());
		this.cache.saveFontSize(this.layout.getFontSize());
	}
	
	public void onMeasure(int parentx, int parenty)
	{
		super.onMeasure(parentx, parenty);
		
		if (this.act == null)
		{
			this.setMeasuredDimension(this.getMeasuredWidth(), this.getMeasuredHeight());
			return;
		}

		if (this.cache.hasCache(this.act.getScreenWidth(), this.getMeasuredHeight(), this.act.game.hashCode()))
		{
			/* We read from cache. */
		} else {
			this.doLayout();
		}

		this.setMeasuredDimension(this.cache.getWidth(), this.getMeasuredHeight());
		this.fontsize = this.cache.getFontSize();
	}
	
	public void onDraw(Canvas c)
	{
		if (this.act == null)
			return;
		
		this.layout.setupForDrawing(this.fontsize, this.getHeight());
		
		for (int i = 2; i < this.act.game.allwords.size(); i++)
		{
			Map<String, WordToGuess> wg = this.act.game.allwords.get(i);
			for (WordToGuess w : wg.values())
			{
				this.layout.renderWord(c, w);
			}
			if (wg.size() > 0)
				this.layout.space(i);
		}
	}
}
