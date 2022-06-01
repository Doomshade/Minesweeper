package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.MessageFormat;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;

public class ScoreboardActivity extends AppCompatActivity {
    public static final String PREFS = "prefs";
    private TableLayout tableLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);
        tableLayout = findViewById(R.id.table);

        SharedPreferences prefs = getSharedPreferences(PREFS, 0);
        int gameCount = prefs.getInt("gameCount", -1) + 1;
        Log.i("game", "gameCount = " + gameCount);
        for (int i = 0; i < gameCount; i++) {
            TableRow row = new TableRow(getApplicationContext());
            addGameId(i, row);
            addDifficulty(prefs, i, row);
            addTime(prefs, i, row);
            tableLayout.addView(row);
        }
    }

    private void addGameId(int i, TableRow row) {
        TextView gameIdTv = new TextView(getApplicationContext());
        String game = getResources().getString(R.string.game);
        gameIdTv.setText(MessageFormat.format("{0} #{1}", game, i));
        row.addView(gameIdTv);
    }

    private void addDifficulty(SharedPreferences prefs, int i, TableRow row) {
        TextView difficultyTv = new TextView(getApplicationContext());
        String difficulty = getResources().getString(R.string.difficulty);
        difficultyTv.setText(MessageFormat.format("{0}: {1}", difficulty, Game.Difficulty.values()[prefs.getInt("difficulty-" + i, 0)]));
        row.addView(difficultyTv);
    }

    private void addTime(SharedPreferences prefs, int i, TableRow row) {
        TextView timeTv = new TextView(getApplicationContext());
        String time = getResources().getString(R.string.time);
        timeTv.setText(MessageFormat.format("{0}: {1}", time, prefs.getInt("time-" + i, 0)));
        row.addView(timeTv);
    }
}