package com.ifihada.anagramic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class ExistingGame
{
  private static String TAG = "ExistingGame";
  public int type; 
  public int difficulty;
  public String rootword;
  public String found;
  int timeleft;
  
  public static boolean exists(Activity a, int mode)
  {
    Log.v(TAG, "exists " + mode + "?");
    String key = String.format("in-progress-%d-valid", mode);
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    
    boolean rc = (prefs.getInt(key, 0)) > 0;
    Log.v(TAG, " = " + rc);
    return rc;
  }
  
  public static void discard(Activity a, int mode)
  {
    Log.v(TAG, "discard " + mode);
    String key = String.format("in-progress-%d-valid", mode);
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    prefs.edit().putInt(key, 0).commit();
  }
  
  public static void store(Activity a, WordGame game)
  {
    Log.v(TAG, "store " + game.config.type + " with root " + game.rootword);
    String prefix = String.format("in-progress-%d-", game.config.type);
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    prefs.edit()
         .putString(prefix + "found", game.getSaveState())
         .putString(prefix + "rootword", game.rootword)
         .putInt(prefix + "difficulty", game.config.difficulty)
         .putInt(prefix + "timeleft", game.timeleft)
         .putInt(prefix + "valid", 1)
         .commit();
  }
  
  public static ExistingGame fetch(Activity a, int mode)
  {
    Log.v(TAG, "fetch " + mode);
    String prefix = String.format("in-progress-%d-", mode);
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    
    ExistingGame eg = new ExistingGame();
    eg.type = mode;
    eg.difficulty = prefs.getInt(prefix + "difficulty", 0);
    eg.timeleft = prefs.getInt(prefix + "timeleft", 0);
    eg.rootword = prefs.getString(prefix + "rootword", "");
    eg.found = prefs.getString(prefix + "found", "");
    ExistingGame.discard(a, mode);
    return eg;
  }
}
