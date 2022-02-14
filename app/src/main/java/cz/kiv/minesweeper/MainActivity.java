package cz.kiv.minesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import cz.kiv.minesweeper.game.Game;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences.Editor edit = getSharedPreferences(Game.GAME_VALUES, 0).edit();
        edit.putInt(Game.S_DIFFICULTY, Game.Difficulty.HARD.ordinal());
        edit.putInt(Game.S_WIDTH_AND_HEIGHT, 8);
        edit.apply();

        Game g = Game.createGame(this);
        g.generateMines();

        Log.d("game", "Game: " + g);
        Log.d("game", "Mines:\n" + g.printBoard().toString());
    }
}