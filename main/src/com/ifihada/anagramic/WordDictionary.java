package com.ifihada.anagramic;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.util.Log;

public class WordDictionary {
	static final Random rand = new Random();
	static Set<String> recentlyChosen = new LinkedHashSet<String>();
	static final int recentlyChosenLimit = 50;
	public static int reduction = 25;
	
	static public String allOfLength(int len)
	{
		return RawDictionary.words[len];
	}
	
	static public int getAdvertisedNumberWordsOfLength(int len)
	{
		return getNumberWordsOfLength(len) / reduction;
	}
	
	static public int getNumberWordsOfLength(int len)
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
    int tries = 50;
    String candidate = null;
    
    while (tries > 0)
    {
      candidate = WordDictionary.getCandidateRandomWordOfLength(len);
      if (!WordDictionary.recentlyChosen.contains(candidate))
        break;

      Log.v("WordDictionary", "Discard dup " + candidate);
      tries--;
    }
    
    WordDictionary.recentlyChosen.add(candidate);
    
    while (WordDictionary.recentlyChosen.size() > recentlyChosenLimit)
    {
      String discard = WordDictionary.recentlyChosen.iterator().next();
      Log.v("WordDictionary", "Trim cache " + discard);
      WordDictionary.recentlyChosen.remove(discard);
    }
    
    return candidate;
  }
  
  static private String getCandidateRandomWordOfLength(int len)
	{
		int count = getNumberWordsOfLength(len);
		int idx;
		do {
			idx = rand.nextInt(count / reduction) * reduction;
		} while (idx >= count);
		return getWordOfLength(len, idx);
	}
	
	static public boolean isWord(String s)
	{
		String all = allOfLength(s.length());
		return all.contains("|" + s + "|");
	}

	public static void upgrade()
	{
		WordDictionary.reduction = 1;
	}
}
