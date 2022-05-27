package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;
import cz.zcu.kiv.jsmahy.minesweeper.game.impl.GameImpl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences.Editor edit = getSharedPreferences(GameImpl.GAME_VALUES, 0).edit();
        edit.putInt(GameImpl.S_DIFFICULTY, Game.Difficulty.HARD.ordinal());
        edit.putInt(GameImpl.S_WIDTH, 8);
        edit.apply();

        Game g = GameImpl.instantiateGame(this);
        g.generateMines();

        Log.d("game", "Game: " + g);
        Log.d("game", "Mines:\n" + g.printBoard());
    }
}