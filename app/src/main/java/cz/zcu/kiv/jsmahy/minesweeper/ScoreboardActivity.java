package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

        TableRow baseRow = (TableRow) getLayoutInflater().inflate(R.layout.scoreboard_table_row, tableLayout, false);
        tableLayout.addView(baseRow);
        tableLayout.addView(getLayoutInflater().inflate(R.layout.horizontal_line, baseRow, false));

        for (int i = 0; i < gameCount; i++) {
            TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.scoreboard_table_row, tableLayout, false);
            // addGameId(i, row);
            addDifficulty(prefs, i, row);
            addTime(prefs, i, row);
            addDate(prefs, i, row);
            addScore(prefs, i, row);
            tableLayout.addView(row);
            // tableLayout.addView(getLayoutInflater().inflate(R.layout.horizontal_line, tableLayout, false));
        }
    }

    private void addScore(SharedPreferences prefs, int i, TableRow row) {
        TextView scoreTv = findTextViewByIdAndRescale(R.id.row_score, row);
        scoreTv.setText(String.valueOf(prefs.getLong("score-" + i, 0L)));
    }

    private <T extends TextView> T findTextViewByIdAndRescale(@IdRes int id, TableRow row) {
        T tv = row.findViewById(id);
        rescaleTextView(tv);
        return tv;
    }

    private void addDate(SharedPreferences prefs, int i, TableRow row) {
        TextView dateTv = findTextViewByIdAndRescale(R.id.row_date, row);
        dateTv.setText(prefs.getString("date-" + i, LocalDateTime.MIN.format(DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss"))));
    }

    private void rescaleTextView(TextView tv) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getApplicationContext().getResources().getDimension(R.dimen.row_text_size));
    }

    private void addDifficulty(SharedPreferences prefs, int i, TableRow row) {
        TextView difficultyTv = findTextViewByIdAndRescale(R.id.row_difficulty, row);
        Game.Difficulty difficulty = Game.Difficulty.values()[prefs.getInt("difficulty-" + i, 0)];
        difficultyTv.setText(difficulty.getResId());
        // tableLayout.addView(getLayoutInflater().inflate(R.layout.vertical_line, tableLayout, false));
    }

    private void addTime(SharedPreferences prefs, int i, TableRow row) {
        TextView timeTv = row.findViewById(R.id.row_time);
        rescaleTextView(timeTv);
        timeTv.setText(LocalTime.ofSecondOfDay(prefs.getInt("time-" + i, 0)).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}