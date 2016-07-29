package com.eyalin.snakes;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.eyalin.snakes.BL.Game;
import com.eyalin.snakes.BL.Player;
import com.eyalin.snakes.Listeners.GameListener;
import com.eyalin.snakes.Listeners.PawnListener;
import com.eyalin.snakes.Listeners.ShortListener;
import com.eyalin.snakes.UI.BoardAdapter;
import com.eyalin.snakes.UI.PawnManager;
import com.eyalin.snakes.UI.ShortcutManager;
import com.eyalin.snakes.UI.ShortcutView;

public class GameActivity extends AppCompatActivity implements GameListener,
        PawnListener, ShortListener {

    final static String tag = "GameActivity";

    private Game game;
    private Player[] players;
    private GridView boardGrid;
    private BoardAdapter boardAdapter;
    private ImageView pawn1;
    private ImageView pawn2;
    private PawnManager pManager1;
    private PawnManager pManager2;
    private ImageView dice;
    private ImageView fakeDice;
    private MediaPlayer diceSound;
    private AnimationDrawable rollAnimation;
    private int player;
    private ShortcutManager shortcuts;
    private ShortcutView[] shortcutImages;
    private boolean gameStarted = false;
    private boolean shortsInplace;
    private boolean pawnInPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(tag, "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.i(tag, "Layout set.");

        players = new Player[]{new Player("Player"), new Player("Enemy")};
        Log.i(tag, "Players set.");
        game = new Game(players);
        Log.i(tag, "Game generated.");
        pawn1 = (ImageView) findViewById(R.id.pawn1);
        pawn2 = (ImageView) findViewById(R.id.pawn2);
        Log.i(tag, "Game generated.");

        boardAdapter = new BoardAdapter(this, game.getBoard());
        boardGrid = (GridView) findViewById(R.id.board_grid);
        boardGrid.setAdapter(boardAdapter);
        Log.i(tag, "Board generated.");

        dice = (ImageView) findViewById(R.id.dice);
        dice.setBackgroundResource(R.drawable.dice_animation);
        rollAnimation = (AnimationDrawable) dice.getBackground();
        fakeDice = (ImageView) findViewById(R.id.fake_dice);
        fakeDice.setVisibility(View.INVISIBLE);

        setShortcuts();
        shortcuts = new ShortcutManager(boardGrid, shortcutImages, game.getBoard());
        shortsInplace = true;
        pawnInPlace = false;

        diceSound = MediaPlayer.create(this, R.raw.roll_dice);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && dice.isClickable()) {
            fakeDice.setVisibility(View.INVISIBLE);
            rollAnimation.stop();
            rollAnimation.start();
            diceSound.start();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && dice.isClickable()) {
            rollAnimation.stop();
            dice.setClickable(false);
            gameStarted = true;
            int steps = (int)(1 + Math.random() * 6);
            switch (steps) {
                case 1: fakeDice.setImageResource(R.drawable.dice1);
                    break;
                case 2: fakeDice.setImageResource(R.drawable.dice2);
                    break;
                case 3: fakeDice.setImageResource(R.drawable.dice3);
                    break;
                case 4: fakeDice.setImageResource(R.drawable.dice4);
                    break;
                case 5: fakeDice.setImageResource(R.drawable.dice5);
                    break;
                default: fakeDice.setImageResource(R.drawable.dice6);
            }
            fakeDice.setVisibility(View.VISIBLE);
            Log.i(tag, "Player steps: " + steps);
            game.play(steps);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setShortcuts() {
        shortcutImages = new ShortcutView[8];
        shortcutImages[0] = (ShortcutView) findViewById(R.id.shortcut0);
        shortcutImages[1] = (ShortcutView) findViewById(R.id.shortcut1);
        shortcutImages[2] = (ShortcutView) findViewById(R.id.shortcut2);
        shortcutImages[3] = (ShortcutView) findViewById(R.id.shortcut3);
        shortcutImages[4] = (ShortcutView) findViewById(R.id.shortcut4);
        shortcutImages[5] = (ShortcutView) findViewById(R.id.shortcut5);
        shortcutImages[6] = (ShortcutView) findViewById(R.id.shortcut6);
        shortcutImages[7] = (ShortcutView) findViewById(R.id.shortcut7);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        shortcuts.initShortcuts();
        shortcuts.addListener(GameActivity.this);
        pManager1 = new PawnManager(pawn1, boardGrid, players[0], 0);
        pManager2 = new PawnManager(pawn2, boardGrid, players[1], 20);
        pManager1.addListener(GameActivity.this);
        pManager2.addListener(GameActivity.this);
        game.addListener(GameActivity.this);
        dice.setClickable(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast t = Toast.makeText(this, "Long Press on the white area to roll the dice.",
                Toast.LENGTH_LONG);
        t.show();
    }

    private void play() {
        if (!gameStarted)
            return;
        shortsInplace = true;
        pawnInPlace = false;
        Log.i(tag, "Play: " + player);
        if (player == 1) {
            int steps = players[1].makeMove();
            game.play(steps);
        } else {
            dice.setClickable(true);
        }
    }

    @Override
    public void updateEndOfMovment() {
        Log.i(tag, "End of pawn movment.");
        pawnInPlace = true;
        if (shortsInplace)
            play();
    }

    @Override
    public void makeSteps(int steps) {
        Log.i(tag, "Player: " + player + "; Steps: " + steps);
        if (player == 0)
            if (steps > 0)
                pManager1.makeStep(steps);
            else
                pManager1.makeReverseStep(steps);
        else
        if (steps > 0)
            pManager2.makeStep(steps);
        else
            pManager2.makeReverseStep(steps);

    }

    @Override
    public void turnChanged() {
        if (player == 0)
            ++player;
        else
            --player;
    }

    @Override
    public void makeShortcut(int surtcut) {
        if (player == 0)
            pManager1.makeShortcut(surtcut);
        else
            pManager2.makeShortcut(surtcut);
    }

    @Override
    public void gameOver(Player winner) {
        Log.i(tag, "The winner:" + winner.getName());
        dice.setClickable(false);
        gameStarted = false;
        Toast t = Toast.makeText(this, winner.getName() + " won!", Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    public void shortcutChange(final int index) {
        if (!gameStarted)
            return;
        Log.i(tag, "Shortcut changed.");
        shortsInplace = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!pawnInPlace) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GameActivity.this.setShortcut(index);
            }
        });
        thread.start();
    }

    private void setShortcut(final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shortcuts.updateShortcut(index);
            }
        });
    }

    @Override
    public void shortcutMoving() {

    }

    @Override
    public void shortcutInPlace() {
        Log.i(tag, "New shortcut is in place.");
        if(gameStarted)
            play();
    }

}