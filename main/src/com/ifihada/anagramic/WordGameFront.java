package com.ifihada.anagramic;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

class CheckUpgradeAction implements Runnable
{
  private WordGameFront act;
  static final int TIME_MS = 10 * 1000;
  
  public CheckUpgradeAction(WordGameFront act)
  {
    this.act = act;
  }
  
  public void run()
  {
    this.act.possiblyUpgrade();
    this.act.postDelayed(this, CheckUpgradeAction.TIME_MS);
  }
}

public class WordGameFront extends Activity
{
  private static final String TAG = "WordGameFront";
  public static final String SOUND_SETTING = "sound-on";
  public static final String ANIMATION_SETTING = "animation-on";
  public static final String FIRSTRUN_SETTING = "first-run";
  public static final String DIFFICULTY_SETTING = "difficulty";
  public static final String UPPERCASE_SETTING = "upper-case";
  private static final String PREFS = "AnagramicPrefs";
  private SharedPreferences prefs;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.front);
    Util.setBackground(this);
    this.prefs = WordGameFront.getPrefs(this);
    
    if (this.prefs.getBoolean(FIRSTRUN_SETTING, true))
    {
      Editor e = this.prefs.edit();
      e.putBoolean(FIRSTRUN_SETTING, false);
      e.putInt(DIFFICULTY_SETTING, WordGame.EASY);
      e.putBoolean(SOUND_SETTING, true);
      e.putBoolean(ANIMATION_SETTING, true);
      e.commit();
      Util.toast(getApplicationContext(), "Welcome to Anagramic!");
    }
    
    this.syncPrefs();
    Stats.load(this.getBaseContext());
    if (Upgrade.Debug)
      this.debugShowGames();
  }
  
  private void debugShowGames()
  {
    int standard = 0;
    int upgraded = 0;
    for (int i = WordGame.BEGINNER; i < WordGame.DIFF_MAX; i++)
    {
      standard += WordGame.countAdvertisedGamesWithDifficulty(i);
      upgraded += WordGame.countGamesWithDifficulty(i);
    }
    Log.v(TAG, "standard games = " + standard);
    Log.v(TAG, "upgraded games = " + upgraded);
  }
  
  public void postDelayed(Runnable runner, int time)
  {
    /* Choose any view... */
    this.findViewById(R.id.UpgradeButton).postDelayed(runner, time);
  }
  
  public static SharedPreferences getPrefs(Activity a)
  {
    return a.getSharedPreferences(WordGameFront.PREFS, Activity.MODE_PRIVATE);
  }
  
  private void syncPrefs()
  {
    WordGameFront.syncPrefs(this, this.prefs);
  }
  
  static void syncPrefs(Activity act, SharedPreferences prefs)
  {
    AudioManager audio = (AudioManager) act
        .getSystemService(Context.AUDIO_SERVICE);
    if (prefs.getBoolean(SOUND_SETTING, false))
    {
      SoundUtil.loadSounds(act.getBaseContext());
      audio.loadSoundEffects();
    } else
    {
      SoundUtil.unloadSounds();
      audio.unloadSoundEffects();
    }
    
    AnimationUtil.animationEnabled = prefs.getBoolean(ANIMATION_SETTING, false);
    GlobalSettings.useUpperCase = prefs.getBoolean(UPPERCASE_SETTING, false);
  }
  
  @Override
  public void onPause()
  {
    super.onPause();
    Stats.save(this.getBaseContext());
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    this.possiblyUpgrade();
    new CheckUpgradeAction(this).run();
  }
  
  public void possiblyUpgrade()
  {
    if (Upgrade.OK || Upgrade.check(this.getApplicationContext()))
    {
      Upgrade.perform();
      this.findViewById(R.id.UpgradeButton).setVisibility(View.GONE);
    }
  }
  
  public void onStartConundrum(View v)
  {
    Intent i = new Intent(this, WordGameAct.class);
    i.putExtra(WordGameAct.GAMETYPE, WordGame.CONUNDRUM);
    i.putExtra(WordGameAct.DIFFICULTY,
        this.prefs.getInt(DIFFICULTY_SETTING, WordGame.EASY));
    this.startActivityForResult(i, 0);
  }
  
  public void onStartBeatTheClock(View v)
  {
    Intent i = new Intent(this, WordGameAct.class);
    i.putExtra(WordGameAct.GAMETYPE, WordGame.AGAINST_CLOCK);
    i.putExtra(WordGameAct.DIFFICULTY,
        this.prefs.getInt(DIFFICULTY_SETTING, WordGame.EASY));
    this.startActivityForResult(i, 0);
  }
  
  public void onStartFreePlay(View v)
  {
    Intent i = new Intent(this, WordGameAct.class);
    i.putExtra(WordGameAct.GAMETYPE, WordGame.FREE_PLAY);
    i.putExtra(WordGameAct.DIFFICULTY,
        this.prefs.getInt(DIFFICULTY_SETTING, WordGame.EASY));
    this.startActivityForResult(i, 0);
  }
  
  public static void initOptionsMenu(ContextMenu menu, SharedPreferences prefs)
  {
    menu.findItem(R.id.menu_options_sound).setChecked(
        prefs.getBoolean(SOUND_SETTING, true));
    menu.findItem(R.id.menu_options_animations).setChecked(
        prefs.getBoolean(ANIMATION_SETTING, true));
    menu.findItem(R.id.menu_options_upper).setChecked(
        prefs.getBoolean(UPPERCASE_SETTING, false));
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo)
  {
    if (v == this.findViewById(R.id.LevelButton))
    {
      this.getMenuInflater().inflate(R.menu.difficulty, menu);
      menu.setHeaderTitle(Upgrade.OK ? R.string.upgraded_title
          : R.string.upgrade_title);
      menu.getItem(this.prefs.getInt(DIFFICULTY_SETTING, WordGame.EASY))
          .setChecked(true);
      
      menu.getItem(WordGame.BEGINNER).setTitle(
          String.format("Beginner (%d games)",
              WordGame.countAdvertisedGamesWithDifficulty(WordGame.BEGINNER)));
      menu.getItem(WordGame.EASY).setTitle(
          String.format("Easy (%d games)",
              WordGame.countAdvertisedGamesWithDifficulty(WordGame.EASY)));
      menu.getItem(WordGame.MEDIUM).setTitle(
          String.format("Normal (%d games)",
              WordGame.countAdvertisedGamesWithDifficulty(WordGame.MEDIUM)));
      menu.getItem(WordGame.HARD).setTitle(
          String.format("Hard (%d games)",
              WordGame.countAdvertisedGamesWithDifficulty(WordGame.HARD)));
      menu.getItem(WordGame.INSANE).setTitle(
          String.format("Insane (%d games)",
              WordGame.countAdvertisedGamesWithDifficulty(WordGame.INSANE)));
    } else if (v == this.findViewById(R.id.OptionsButton))
    {
      this.getMenuInflater().inflate(R.menu.options, menu);
      WordGameFront.initOptionsMenu(menu, this.prefs);
    		  	
    } else
    {
      assert false;
    }
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
    case R.id.menu_diff_beginner:
      this.prefs.edit().putInt(DIFFICULTY_SETTING, WordGame.BEGINNER).commit();
      return true;
    case R.id.menu_diff_easy:
      this.prefs.edit().putInt(DIFFICULTY_SETTING, WordGame.EASY).commit();
      return true;
    case R.id.menu_diff_medium:
      this.prefs.edit().putInt(DIFFICULTY_SETTING, WordGame.MEDIUM).commit();
      return true;
    case R.id.menu_diff_hard:
      this.prefs.edit().putInt(DIFFICULTY_SETTING, WordGame.HARD).commit();
      return true;
    case R.id.menu_diff_insane:
      this.prefs.edit().putInt(DIFFICULTY_SETTING, WordGame.INSANE).commit();
      return true;
    case R.id.menu_options_sound:
      this.prefs
          .edit()
          .putBoolean(SOUND_SETTING,
              !this.prefs.getBoolean(SOUND_SETTING, true)).commit();
      this.syncPrefs();
      return true;
    case R.id.menu_options_animations:
      this.prefs
          .edit()
          .putBoolean(ANIMATION_SETTING,
              !this.prefs.getBoolean(ANIMATION_SETTING, true)).commit();
      this.syncPrefs();
      return true;
    case R.id.menu_options_upper:
      this.prefs
          .edit()
          .putBoolean(UPPERCASE_SETTING,
              !this.prefs.getBoolean(UPPERCASE_SETTING, false))
          .commit();
      this.syncPrefs();
      return true;
    }
    return super.onContextItemSelected(item);
  }
  
  public static void toggleSoundOption(Activity a)
  {
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    prefs
      .edit()
      .putBoolean(WordGameFront.SOUND_SETTING,
          !prefs.getBoolean(WordGameFront.SOUND_SETTING, true))
      .commit();
    WordGameFront.syncPrefs(a, prefs);
  }
  
  public static void toggleAnimationOption(Activity a)
  {
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    prefs
      .edit()
      .putBoolean(WordGameFront.ANIMATION_SETTING,
          !prefs.getBoolean(WordGameFront.ANIMATION_SETTING, true))
      .commit();
    WordGameFront.syncPrefs(a, prefs);
  }
  
  public static void toggleUppercaseOption(Activity a)
  {
    SharedPreferences prefs = WordGameFront.getPrefs(a);
    prefs
      .edit()
      .putBoolean(WordGameFront.UPPERCASE_SETTING,
          !prefs.getBoolean(WordGameFront.UPPERCASE_SETTING, true))
      .commit();
    WordGameFront.syncPrefs(a, prefs);
  }
  
  public void onChangeDifficulty(View v)
  {
    this.registerForContextMenu(v);
    this.openContextMenu(v);
  }
  
  public void onChangeOptions(View v)
  {
    this.registerForContextMenu(v);
    this.openContextMenu(v);
  }
  
  public void onShowStatistics(View v)
  {
    Intent i = new Intent(this, WordGameStats.class);
    this.startActivityForResult(i, 0);
  }
  
  public void onUpgrade(View v)
  {
    Intent i = new Intent(Intent.ACTION_VIEW,
        Uri.parse("market://search?q=pname:" + Upgrade.WORDGAME_UPGRADE));
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try
    {
      this.startActivity(i);
    } catch (ActivityNotFoundException ax)
    {
      Util.toast(this.getApplicationContext(),
          "Cannot launch market for purchase");
    }
    
    Upgrade.cheat(this.getApplicationContext());
    this.possiblyUpgrade();
  }
}