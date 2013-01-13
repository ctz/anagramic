package com.ifihada.anagramic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PermuteIterator
{
	private Pattern pat;
	private Matcher m;
	private String src;
	private int len;
		
	public PermuteIterator(String source, int length)
	{
		this.src = source;
		this.len = length;
		this.pat = Pattern.compile("(?<=\\|)([" + this.src + "]{" + this.len + "})(?=\\|)");
		this.m = this.pat.matcher(WordDictionary.allOfLength(this.len));
	}
	
	private boolean valid(String candidate)
	{
		long avail = Long.MAX_VALUE;
		for (int i = 0; i < candidate.length(); i++)
		{
			int offset = 0;
			boolean found = false;
			while (!found)
			{
				offset = this.src.indexOf(candidate.charAt(i), offset);
				if (offset == -1)
				{
					return false;
				}
				if ((avail & (1 << offset)) == 0)
				{
					offset += 1;
					continue;
				}

				avail &= ~ (1 << offset);
				found = true;
			}
			
			if (!found)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public String next()
	{
		while (m.find())
		{
			String candidate = m.group(1);
			if (this.valid(candidate))
				return candidate;
		}
		return null;
	}
}

class WordToGuess
{
	public final String word;
	public boolean guessed;
	public boolean cheated;
	public long guessedAt;
	
	public WordToGuess(String s)
	{
		this.word = s;
		this.guessed = false;
		this.cheated = false;
		this.guessedAt = 0;
	}
}

public class WordGame
{
	public static class Config
	{
		public Config(int rl, int mo, int secs)
		{
			this.rootLength = rl;
			this.minOthers = mo;
			this.seconds = secs;
			this.conundrum = false;
			this.hasTime = true;
			this.minWordLength = 3;
		}
		
		public int rootLength;
		public int minOthers;
		public int minWordLength;
		public int seconds;
		public boolean conundrum;
		public boolean hasTime;
		public int type;
		public int difficulty;
	}

	public static final int BEGINNER = 0;
	public static final int EASY = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
	public static final int INSANE = 4;
	public static final int DIFF_MAX = 5;

	public static final int CONUNDRUM = 0;
	public static final int AGAINST_CLOCK = 1;
	public static final int FREE_PLAY = 2;
	public static final int MODE_MAX = 3;
	
	public static int rootlengths[] = new int[] { 5, 6, 7, 8, 9 };
	
	public static Config makeConfig(int type, int difficulty)
	{
		Config c;
		
		switch (difficulty)
		{
		default:
		case BEGINNER:
			difficulty = BEGINNER;
			c = new Config(rootlengths[difficulty], 15, 360);
			break;
		case EASY:
			c = new Config(rootlengths[difficulty], 15, 360);
			break;
		case MEDIUM:
			c = new Config(rootlengths[difficulty], 15, 480);
			break;
		case HARD:
			c = new Config(rootlengths[difficulty], 15, 720);
			break;
		case INSANE:
			c = new Config(rootlengths[difficulty], 15, 720);
			break;
		}
		
		if (type == CONUNDRUM)
		{
			c.seconds = 30;
			c.conundrum = true;
		}
		
		if (type == FREE_PLAY)
		{
			c.hasTime = false;
		}
		
		c.type = type;
		c.difficulty = difficulty;
		
		return c;
	}
	
	public static final int FOUND_NEW = 0;
	public static final int ALREADY_FOUND = 1;
	public static final int INVALID = 2;
	
	public String rootword;
	public final Config config;
	public List< TreeMap<String, WordToGuess> > allwords;
	public char[] letters;
	public int got;
	public int avail;
	public int timeleft;
	
	public static int countGamesWithConfig(Config cfg)
	{
		return WordDictionary.getAdvertisedNumberWordsOfLength(cfg.rootLength);
	}
	
	public static int countAdvertisedGamesWithDifficulty(int diff)
	{
		return WordDictionary.getAdvertisedNumberWordsOfLength(WordGame.rootlengths[diff]);
	}
	
	public static int countGamesWithDifficulty(int diff)
	{
		return WordDictionary.getNumberWordsOfLength(WordGame.rootlengths[diff]);
	}
	
	public WordGame(Config cfg)
	{
		this.config = cfg;
		
		do
		{
			this.setRootWord(WordDictionary.getRandomWordOfLength(cfg.rootLength));
		} while (!this.isAcceptableRoot());
		
		this.postSetup();
	}
	
	public WordGame(Config cfg, String rootword, String foundalready, int timeleft)
	{
		this.config = cfg;
		this.setRootWord(rootword);
		this.isAcceptableRoot();
		this.postSetup();
		this.processSavedState(foundalready);
		this.timeleft = timeleft;
	}
	
	private void processSavedState(String state)
	{
		String[] found = state.split(":");
		for (String s : found)
		{
			this.guess(s);
		}
	}
	
	public String getSaveState()
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SortedMap<String, WordToGuess> words : this.allwords)
		{
			for (WordToGuess w : words.values())
			{
				if (w.guessed)
				{
					if (!first)
						sb.append(':');
					sb.append(w.word);
					first = false;
				}
			}
		}
		return sb.toString();
	}
	
	private void setRootWord(String s)
	{
		System.out.println("setRootWord(" + s + ")");
		this.rootword = s;
		if (this.allwords == null)
		{
			this.allwords = new ArrayList<TreeMap<String, WordToGuess>>();
			for (int i = 0; i <= this.config.rootLength; i++)
			{
				this.allwords.add(i, new TreeMap<String, WordToGuess>());
			}
		} else {
			this.clearOthers();
		}
	}
	
	private void postSetup()
	{
		this.allwords.get(this.rootword.length()).put(this.rootword, new WordToGuess(this.rootword));
		this.letters = this.rootword.toCharArray();
		this.permute();
		this.avail = this.count();
		this.got = 0;
		this.timeleft = this.config.seconds;
	}
	
	private void clearOthers()
	{
		for (int i = 0; i <= this.config.rootLength; i++)
		{
			this.allwords.get(i).clear();
		}
	}
	
	private boolean isAcceptableRoot()
	{
		if (this.config.conundrum)
			return this.isValidConundrumGame();
		this.generate();
		return this.count() >= this.config.minOthers;
	}
	
	/* Valid if the root word has no valid permutations. */
	private boolean isValidConundrumGame()
	{
		PermuteIterator pi = new PermuteIterator(this.rootword, this.rootword.length());
		String s;
		while ((s = pi.next()) != null)
		{
			if (!s.equals(this.rootword))
				return false;
		}
		return true;
	}
	
	public int hashCode()
	{
		return this.rootword.hashCode();
	}
	
	private int count()
	{
		int rc = 0;
		for (Map<String, WordToGuess> words : this.allwords)
			rc += words.size();
		return rc;
	}
	
	public int guess(String word)
	{
		assert word.length() > 0;
		assert word.length() < this.allwords.size();
		word = word.toLowerCase(GlobalSettings.locale);
			
		WordToGuess wg = this.allwords.get(word.length()).get(word);
		
		if (wg == null)
		{
			return WordGame.INVALID;
		} else {
			if (wg.guessed)
				return WordGame.ALREADY_FOUND;
			
			wg.guessed = true;
			wg.guessedAt = Util.getTimeMillis();
			this.got += 1;
			return WordGame.FOUND_NEW;
		}
	}
	
	public void cheat()
	{
		for (TreeMap<String, WordToGuess> words : this.allwords)
			for (WordToGuess w : words.values())
				if (!w.guessed)
				{
					w.guessed = true;
					w.cheated = true;
				}
	}
	
	public boolean hasWon()
	{
		for (TreeMap<String, WordToGuess> words : this.allwords)
			for (WordToGuess w : words.values())
				if (!w.guessed)
					return false;
		return true;
	}
	
	public void permute()
	{
		Util.permute(this.letters);
	}
	
	private void generate()
	{
		for (int l = this.config.minWordLength; l <= this.rootword.length(); l++)
		{
			PermuteIterator pi = new PermuteIterator(this.rootword, l);
			String s;
			while ((s = pi.next()) != null)
			{
				this.allwords.get(s.length()).put(s, new WordToGuess(s));
			}
		}
	}
}
