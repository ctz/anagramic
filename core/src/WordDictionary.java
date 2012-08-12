import java.util.Random;

public class WordDictionary {
	static final Random rand = new Random();
	
	static public String allOfLength(int len)
	{
		return RawDictionary.words[len];
	}
	
	static private int getNumberWordsOfLength(int len)
	{
		int al = allOfLength(len).length();
		return (al - 1) / (len + 1);
	}
	
	static private String getWordOfLength(int len, int index)
	{
		int offs = index * (len + 1);
		return allOfLength(len).substring(offs + 1, offs + len + 1);
	}
	
	static public String getRandomWordOfLength(int len)
	{
		int count = getNumberWordsOfLength(len);
		int idx = rand.nextInt(count);
		return getWordOfLength(len, idx);
	}
	
	static public boolean isWord(String s)
	{
		String all = allOfLength(s.length());
		return all.contains("|" + s + "|");
	}
}
