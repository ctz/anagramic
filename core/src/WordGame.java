import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Config
{
	public Config(int rl, int mo)
	{
		this.rootlength = rl;
		this.minothers = mo;
	}
	
	public final int rootlength;
	public final int minothers;
}

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
	
	public WordToGuess(String s)
	{
		this.word = s;
		this.guessed = false;
	}
}

public class WordGame
{
	public static final Config HardPreset = new Config(9, 20);
	public static final Config MediumPreset = new Config(7, 15);
	public static final Config EasyPreset = new Config(7, 15);
	
	public static final int FOUND_NEW = 0;
	public static final int ALREADY_FOUND = 1;
	public static final int INVALID = 2;
	
	public String rootword;
	public final Config config;
	public List< Map<String, WordToGuess> > allwords;
	
	public WordGame(Config cfg)
	{
		this.config = cfg;
		do
		{
			this.rootword = WordDictionary.getRandomWordOfLength(cfg.rootlength);
			this.allwords = new ArrayList<Map<String, WordToGuess>>();
			for (int i = 0; i <= cfg.rootlength; i++)
			{
				this.allwords.add(i, new HashMap<String, WordToGuess>());
			}
			this.generate();
		} while (this.count() < this.config.minothers);
		
		this.allwords.get(this.rootword.length()).put(this.rootword, new WordToGuess(this.rootword));
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
			
		WordToGuess wg = this.allwords.get(word.length()).get(word);
		
		if (wg == null)
		{
			return WordGame.INVALID;	
		} else {
			if (wg.guessed)
				return WordGame.ALREADY_FOUND;
			
			wg.guessed = true;
			return WordGame.FOUND_NEW;
		}
	}
	
	public boolean hasWon()
	{
		for (Map<String, WordToGuess> words : this.allwords)
			for (WordToGuess w : words.values())
				if (!w.guessed)
					return false;
		return true;
	}
	
	private void generate()
	{
		for (int l = 2; l < this.rootword.length(); l++)
		{
			PermuteIterator pi = new PermuteIterator(this.rootword, l);
			String s;
			while ((s = pi.next()) != null)
			{
				this.allwords.get(s.length()).put(s, new WordToGuess(s));
			}
		}
	}
	
	/*
	public static void main(String[] args)
	{
		System.out.println("candidate(6): " + WordDictionary.getRandomWordOfLength(6));
		System.out.println("candidate(7): " + WordDictionary.getRandomWordOfLength(7));
		System.out.println("candidate(8): " + WordDictionary.getRandomWordOfLength(8));
		System.out.println("candidate(9): " + WordDictionary.getRandomWordOfLength(9));
		System.out.println("occasional?: " + WordDictionary.isWord("occasional"));
		System.out.println("nocturnal?: " + WordDictionary.isWord("nocturnal"));
		System.out.println("abasdas?: " + WordDictionary.isWord("abasdas"));
		WordGame wg = new WordGame(WordGame.HardPreset);
		System.out.println("root word: " + wg.rootword);
		for (Map<String, WordToGuess> words : wg.allwords)
		{
			for (WordToGuess w : words.values())
				System.out.println("  other: " + w.word);
		}
	}
	*/
}
