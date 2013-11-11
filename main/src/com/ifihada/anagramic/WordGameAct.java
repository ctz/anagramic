package com.ifihada.anagramic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

class TimerUpdateAction implements Runnable
{
  private WordGameAct act;
  
  public TimerUpdateAction(WordGameAct act)
  {
    this.act = act;
  }
  
  public void run()
  {
    this.act.tick();
    this.act.postDelayed(this, 1000);
  }
}

class EndGameAction implements Runnable
{
  private WordGameAct act;
  
  public EndGameAction(WordGameAct act)
  {
    this.act = act;
  }
  
  public void run()
  {
    this.act.askFinish();
  }
}

public class WordGameAct extends Activity
{
  private static final String TAG = "WordGameAct";
  public static final String GAMETYPE = "gametype";
  public static final String RESUME = "resume";
  public static final String DIFFICULTY = "difficulty";
  public static final String ROOTWORD = "root-wood";
  public static final String FOUNDWORDS = "found-words";
  public static final String TIMELEFT = "time-left";
  
  public WordGame game;
  private WordButtonView buttonContainer;
  private WordButtonView selectedContainer;
  private FoundView foundView;
  private TextView scoreView;
  private TextView timeView;
  private boolean finished;
  private boolean paused;
  private boolean won;
  private int requestedDifficulty;
  private int requestedGame;
  
  public void postDelayed(Runnable runner, int time)
  {
    /* Choose any view... */
    this.foundView.postDelayed(runner, time);
  }
  
  private void startTimer()
  {
    TimerUpdateAction tua = new TimerUpdateAction(this);
    tua.run();
  }
  
  @SuppressLint("DefaultLocale")
  public void tick()
  {
    if (this.game.config.hasTime && !this.paused && !this.finished)
    {
      if (this.game.timeleft <= 0)
      {
        this.finishWithSuccess(false);
        return;
      }
      this.game.timeleft -= 1;
      String s = String.format("%02d:%02d", this.game.timeleft / 60,
          this.game.timeleft % 60);
      this.timeView.setText(s);
    }
    this.foundView.invalidate();
  }
  
  @Override
  public void onCreate(Bundle state)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(state);
    setContentView(R.layout.game_layout);
    Util.setBackground(this);
    
    this.buttonContainer = (WordButtonView) this
        .findViewById(R.id.SelectLetterButtonLayout);
    this.selectedContainer = (WordButtonView) this
        .findViewById(R.id.SelectedLetterButtonLayout);
    this.foundView = (FoundView) this.findViewById(R.id.FoundView);
    this.foundView.setActivity(this);
    this.scoreView = (TextView) this.findViewById(R.id.ScoreText);
    this.timeView = (TextView) this.findViewById(R.id.TimeText);
    this.paused = true;
        
    if (this.getIntent() != null && this.getIntent().getBooleanExtra(WordGameAct.RESUME, false))
    {
      Intent i = this.getIntent();
      this.requestedGame = i.getIntExtra(WordGameAct.GAMETYPE, 0);
      ExistingGame eg = ExistingGame.fetch(this, this.requestedGame);
      this.requestedDifficulty = eg.difficulty;
      this.buildGame(this.requestedGame,
                     this.requestedDifficulty,
                     eg.rootword,
                     eg.found,
                     eg.timeleft);
    } else if (state != null) {
      this.requestedGame = state.getInt(WordGameAct.GAMETYPE);
      this.requestedDifficulty = state.getInt(WordGameAct.DIFFICULTY);
      this.buildGame(state);
    } else {
      Intent i = this.getIntent();
      if (i != null)
      {
        this.requestedGame = i.getIntExtra(WordGameAct.GAMETYPE, 0);
        this.requestedDifficulty = i.getIntExtra(WordGameAct.DIFFICULTY, 0);
      }
      this.chooseGame();
    }
    this.postSetup();
    this.startTimer();
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    Stats.timerStart();
    if (this.finished)
      ExistingGame.discard(this, this.game.config.type);
    else
      ExistingGame.store(this, this.game);
    this.paused = false;
  }
  
  @Override
  public void onPause()
  {
    super.onPause();
    Stats.timerPause();
    Stats.save(this.getBaseContext());
    if (this.finished)
      ExistingGame.discard(this, this.game.config.type);
    else
      ExistingGame.store(this, this.game);
    this.paused = true;
  }
  
  private void buildGame(Bundle state)
  {
    this.buildGame(this.requestedGame,
                   this.requestedDifficulty,
                   state.getString(WordGameAct.ROOTWORD),
                   state.getString(WordGameAct.FOUNDWORDS),
                   state.getInt(WordGameAct.TIMELEFT));
  }
  
  private void buildGame(int type, int difficulty, String root, String found, int timeleft)
  {
    this.game = new WordGame(WordGame.makeConfig(type, difficulty),
                             root, found, timeleft);
  }
  
  private void chooseGame()
  {
    this.game = new WordGame(WordGame.makeConfig(this.requestedGame,
        this.requestedDifficulty));
    Stats.gameStart(this.requestedGame, this.requestedDifficulty);
  }
  
  @Override
  public void onSaveInstanceState(Bundle state)
  {
    super.onSaveInstanceState(state);
    state.putInt(WordGameAct.GAMETYPE, this.game.config.type);
    state.putInt(WordGameAct.DIFFICULTY, this.game.config.difficulty);
    state.putString(WordGameAct.ROOTWORD, this.game.rootword);
    state.putString(WordGameAct.FOUNDWORDS, this.game.getSaveState());
    state.putInt(WordGameAct.TIMELEFT, this.game.timeleft);
  }
  
  private void postSetup()
  {
    this.buildButtons();
    this.foundView.invalidate();
    this.onConfigurationChanged(null);
    this.updateScore();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo)
  {
    this.getMenuInflater().inflate(R.menu.options, menu);
    SharedPreferences prefs = WordGameFront.getPrefs(this);
    WordGameFront.initOptionsMenu(menu, prefs);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle item selection
    switch (item.getItemId())
    {
    case R.id.menu_give_up:
      this.game.cheat();
      Stats.giveUp();
      this.foundView.invalidate();
      item.setVisible(false);
      return true;
      
    case R.id.menu_options:
      this.registerForContextMenu(this.foundView);
      this.openContextMenu(this.foundView);
      return true;
      
    case R.id.menu_end_game:
      this.setResult(Activity.RESULT_CANCELED);
      this.finish();
      return true;
      
    default:
      return super.onOptionsItemSelected(item);
    }
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
    case R.id.menu_options_sound:
      WordGameFront.toggleSoundOption(this);
      return true;
    case R.id.menu_options_animations:
      WordGameFront.toggleAnimationOption(this);
      return true;
    case R.id.menu_options_upper:
      WordGameFront.toggleUppercaseOption(this);
      this.foundView.invalidate();
      this.selectedContainer.fixCase();
      this.buttonContainer.fixCase();
      return true;
    }
    return super.onContextItemSelected(item);
  }
  
  @Override
  public void onConfigurationChanged(Configuration config)
  {
    this.reflow();
    if (config != null)
      super.onConfigurationChanged(config);
  }
  
  private void buildButtons()
  {
    LayoutInflater li = this.getLayoutInflater();
    this.buttonContainer.fillForLetters(li, this.game.letters);
    this.selectedContainer.fillForSelect(li, this.game.letters.length);
    this.reflow();
  }
  
  public int getScreenWidth()
  {
    Display display = this.getWindowManager().getDefaultDisplay();
    return display.getWidth();
  }
  
  static final int maxFont = 32;
  
  private void reflow()
  {
    int width = this.getScreenWidth();
    this.buttonContainer.setScreenWidth(width);
    this.selectedContainer.setScreenWidth(width);
  }
  
  private void resetButtons()
  {
    this.selectedContainer.hideAll();
    this.buttonContainer.showAll();
  }
  
  public void onSelectLetter(View v)
  {
    Button b = (Button) v;
    assert b != null;
    if (b.getParent() == this.buttonContainer)
    {
      int returnIndex = this.buttonContainer.hideButton(b);
      this.selectedContainer.pushButton(b, returnIndex);
    } else
    {
      int returnIndex = this.selectedContainer.returnButton(b);
      this.buttonContainer.reshowButton(returnIndex);
    }
  }
  
  public void onCancelButton(View v)
  {
    this.resetButtons();
  }
  
  public void onSubmitButton(View v)
  {
    String g = this.selectedContainer.getWord();
    if (g.length() == 0 || this.finished)
      return;
    if (this.game.config.conundrum && g.length() != this.game.letters.length)
      return;
    
    switch (this.game.guess(g))
    {
    case WordGame.ALREADY_FOUND:
      Util.toast(this.getApplicationContext(), "Found again: " + g + ".");
      break;
    
    case WordGame.FOUND_NEW:
      Util.toast(this.getApplicationContext(), "Found: " + g + "!", true);
      this.foundView.invalidate();
      Stats.foundWord();
      if (this.game.avail != this.game.got)
        SoundUtil.playJingle(g.length());
      break;
    
    case WordGame.INVALID:
      Util.toast(this.getApplicationContext(), "Not in dictionary: " + g + ".",
          false);
      this.foundView.invalidate();
      Stats.invalidWord();
      break;
    }
    
    if (this.game.avail == this.game.got)
    {
      this.finishWithSuccess(true);
    }
    
    this.updateScore();
    this.resetButtons();
  }
  
  private void finishWithSuccess(boolean won)
  {
    if (this.finished)
      return;
    
    this.won = won;
    
    if (won)
    {
      Stats.gameWin(this.game.config.type, this.game.config.difficulty);
      SoundUtil.playGoodFinishSound();
      Util.toast(this.getApplicationContext(), "Good job!", true);
    } else
    {
      SoundUtil.playBadFinishSound();
      Util.toast(this.getApplicationContext(), "Time up! Game over!", false);
      this.game.cheat();
      this.foundView.invalidate();
    }
    this.postDelayed(new EndGameAction(this), this.won ? 3000 : 5000);
    this.finished = true;
  }
  
  private void updateScore()
  {
    if (this.game.config.conundrum)
    {
      this.scoreView.setVisibility(View.INVISIBLE);
    } else
    {
      this.scoreView.setText(String.format("%2.0f%% found",
          100 * (this.game.got / (double) this.game.avail)));
    }
  }
  
  public void onPermuteButton(View v)
  {
    this.buttonContainer.showAll();
    this.selectedContainer.hideAll();
    this.buttonContainer.permute();
  }
  
  @Override
  public boolean onKeyDown(int keycode, KeyEvent ev)
  {
    char c = ev.getDisplayLabel();
    if (c >= 'A' && c <= 'Z')
    {
      String s = String.valueOf(c).toLowerCase(GlobalSettings.locale);
      if (GlobalSettings.useUpperCase)
        s = s.toUpperCase(GlobalSettings.locale);
      Button b = this.buttonContainer.findWithLabel(s);
      if (b == null)
      {
        Log.v(TAG, "Reject unhandled keypress " + c);
        return super.onKeyDown(keycode, ev);
      }
      this.onSelectLetter(b);
      return true;
    } else if (ev.getKeyCode() == KeyEvent.KEYCODE_ENTER)
    {
      this.onSubmitButton(null);
      return true;
    } else if (ev.getKeyCode() == KeyEvent.KEYCODE_DEL)
    {
      this.onCancelButton(null);
      return true;
    } else if (ev.getKeyCode() == KeyEvent.KEYCODE_SPACE)
    {
      this.onPermuteButton(null);
      return true;
    } else
    {
      Log.v(TAG, "Reject weird keypress " + c);
      return super.onKeyDown(keycode, ev);
    }
  }
  
  public void reopen()
  {
    Intent i = new Intent(this, WordGameAct.class);
    i.putExtra(WordGameAct.GAMETYPE, this.requestedGame);
    i.putExtra(WordGameAct.DIFFICULTY, this.requestedDifficulty);
    this.startActivityForResult(i, 0);
    this.finish();
  }
  
  public void askFinish()
  {
    if (this.paused)
      return;
    this.showDialog(0);
  }
  
  @Override
  public Dialog onCreateDialog(int id)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Another game?").setCancelable(false)
        .setTitle(this.won ? "Congratulations" : "Try again?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id)
          {
            WordGameAct.this.reopen();
          }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id)
          {
            WordGameAct.this.finish();
          }
        });
    return builder.create();
  }
}