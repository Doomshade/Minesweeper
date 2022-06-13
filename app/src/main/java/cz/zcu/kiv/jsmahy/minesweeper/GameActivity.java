package cz.zcu.kiv.jsmahy.minesweeper;

import static cz.zcu.kiv.jsmahy.minesweeper.ScoreboardActivity.PREFS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;
import cz.zcu.kiv.jsmahy.minesweeper.game.GridRecyclerAdapter;
import cz.zcu.kiv.jsmahy.minesweeper.game.ItemClickListener;
import cz.zcu.kiv.jsmahy.minesweeper.game.MineGrid;
import cz.zcu.kiv.jsmahy.minesweeper.game.Tile;

public class GameActivity extends AppCompatActivity implements ItemClickListener {
    private final Handler timerHandler = new Handler();
    private Game.Difficulty difficulty = null;
    private RecyclerView recyclerView = null;
    private GridRecyclerAdapter adapter = null;
    private MineGrid mineGrid = null;
    private ImageButton flagButton = null;
    private ImageButton restartButton = null;
    private ImageButton backButton = null;
    private MineGrid.Position clickedMine = null;
    private int mineCount = 0;
    private int time = 0;
    private long score = 0;
    private boolean started = false;
    private TextView mineCountTv = null;
    private TextView timeTv = null;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            time++;
            updateTime();
            timerHandler.postDelayed(this, 1000L);
        }
    };
    private boolean gameFinish = false;
    private boolean flagging = false;

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTheme(PreferenceManager.getDefaultSharedPreferences(this).getInt("themeId", R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Fulbo));
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        hideSystemBars();
        int difficultyOrdinal = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("difficulty", 0);
        this.difficulty = Game.Difficulty.values()[difficultyOrdinal];

        if (state == null) {
            mineGrid = new MineGrid(this.difficulty);
            mineCount = this.difficulty.getMineCount();
            mineGrid.generateMines(getApplicationContext());
        } else {
            mineGrid = state.getParcelable("minegrid");
            mineCount = state.getInt("mineCount");
            time = state.getInt("time");
            gameFinish = state.getBoolean("gameFinish");
            started = state.getBoolean("started");
            flagging = state.getBoolean("flagging");
            clickedMine = state.getParcelable("clickedBomb");
        }

        mineCountTv = findViewById(R.id.mineCount);
        updateMines();

        timeTv = findViewById(R.id.time);
        updateTime();
        if (started) {
            startTimer();
        }

        this.flagButton = findViewById(R.id.flag);
        this.flagButton.setOnClickListener(view -> {
            flagging = !flagging;
            flagButton.setImageResource(flagging ? R.drawable.ic_icon_tile_flagged_red : R.drawable.ic_icon_tile_flagged);
        });
        this.flagButton.setImageResource(flagging ? R.drawable.ic_icon_tile_flagged_red : R.drawable.ic_icon_tile_flagged);
        this.recyclerView = findViewById(R.id.game_grid);

        this.adapter = new GridRecyclerAdapter(this, mineGrid, this);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this, mineGrid.getRows()));
        this.recyclerView.setAdapter(adapter);

        this.restartButton = findViewById(R.id.restart);
        this.restartButton.setOnClickListener(view -> {
            if (gameFinish) {
                restartActivity();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.restart_prompt_title)
                    .setMessage(R.string.restart_prompt_message)
                    .setPositiveButton(R.string.answer_yes, (dialogInterface, i) -> restartActivity())
                    .setNegativeButton(R.string.answer_no, ((dialogInterface, i) -> dialogInterface.cancel()))
                    .show();
        });

        this.backButton = findViewById(R.id.back);
        this.backButton.setOnClickListener(view -> finish());

        if (gameFinish) {
            finishGame();
        }

    }

    // TODO timer se stopne po změnu UI na landscape

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    private void stopTimer() {
        started = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started) {
            startTimer();
        }
    }

    private void startTimer() {
        started = true;
        timerHandler.postDelayed(timerRunnable, 1000L);
    }

    private void updateMines() {
        mineCountTv.setText(String.valueOf(mineCount));
    }

    private void updateTime() {
        timeTv.setText(String.valueOf(time));
    }

    private void restartActivity() {
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable("minegrid", mineGrid);
        state.putInt("mineCount", mineCount);
        state.putInt("time", time);
        state.putBoolean("gameFinish", gameFinish);
        state.putBoolean("started", started);
        state.putBoolean("flagging", flagging);
        state.putParcelable("clickedBomb", clickedMine);
    }


    private int getStatus(MineGrid.Position position) {
        if (!mineGrid.isInBounds(position)) {
            return UNKNOWN;
        }

        final Tile clickedTile = mineGrid.tile(position);
        if (clickedTile == null) {
            return UNKNOWN;
        }
        if (clickedTile.isFlagged()) {
            return FLAGGED;
        }
        if (clickedTile.isMine()) {
            return MINE_REVEAL;
        }
        if (clickedTile.isRevealed()) {
            return NOTHING;
        }

        return mineGrid.getNearbyMineCount(position);
    }

    @Override
    public void onClick(GridRecyclerAdapter.TileViewHolder view, boolean longClick) {
        if (!started) {
            startTimer();
        }
        onClick(view, new HashSet<>(), longClick);
    }

    public void onClick(GridRecyclerAdapter.TileViewHolder view, Set<MineGrid.Position> probedTiles, boolean longClick) {
        MineGrid.Position position = mineGrid.getPosition(view.getAdapterPosition());
        handleStatus(position, probedTiles, longClick);
        checkForWin();
    }

    private void handleStatus(MineGrid.Position position, Set<MineGrid.Position> probedTiles, boolean longClick) {
        if (!mineGrid.isInBounds(position) || !probedTiles.add(position)) {
            return;
        }

        final boolean recursing = probedTiles.size() > 1;
        final int status = getStatus(position);
        final boolean prevFlagging = flagging;
        if (longClick) {
            flagging = true;
        }
        final Tile tile = mineGrid.tile(position);
        switch (status) {
            case UNKNOWN:
            case NOTHING:
                break;
            case FLAGGED:
                if (flagging && !recursing) {
                    toggleFlag(tile);
                }
                break;
            case MINE_REVEAL:
                if (!flagging) {
                    clickedMine = position;
                    finishGame();
                    showEndAlertDialog();
                }
            default:
                if (!flagging) {
                    tile.reveal(status);
                    // no mines found, explore the other ones as well
                    if (status == 0) {
                        for (int y = -1; y <= 1; y++) {
                            for (int x = -1; x <= 1; x++) {
                                handleStatus(position.add(x, y), probedTiles, false);
                            }
                        }
                    }
                } else {
                    if (!recursing) {
                        toggleFlag(tile);
                    }
                }
                break;
        }

        // back to the previous state
        if (longClick) {
            flagging = prevFlagging;
        }
        adapter.notifyItemChanged(position.getRawPosition(), tile);

        // update the status to the "future"
        // the tile (maybe) WAS NOT revealed or (maybe) WAS NOT flagged prior to this call
        // if it was revealed now, the wasRevealed would return false prior to this call
        // same goes for wasFlagged
        new Handler(Looper.getMainLooper()).postDelayed(tile::update, 1L);
    }

    private void showEndAlertDialog() {
        AlertDialog.Builder alertDialogBase = getAlertDialogBase();
        alertDialogBase.show();
    }

    private AlertDialog.Builder getAlertDialogBase() {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.game_finished)
                .setIcon(R.drawable.ic_icon_mine_found_red)
                .setNegativeButton(R.string.close, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.again, (dialogInterface, i) -> restartActivity());
    }

    private void finishGame() {
        gameFinish = true;
        stopTimer();
        flagButton.setClickable(false);
        final Tile[][] tiles = mineGrid.getTiles();
        for (int y = 0; y < mineGrid.getRows(); y++) {
            for (int x = 0; x < mineGrid.getColumns(); x++) {
                final Tile tile = tiles[y][x];
                if (tile.isMine()) {
                    tile.reveal(0);
                    adapter.notifyItemChanged(mineGrid.getPosition(x, y).getRawPosition(), tile);
                }
            }
        }
        if (clickedMine != null) {
            Tile tile = mineGrid.tile(clickedMine);
            tile.setClickedMine();
            adapter.notifyItemChanged(clickedMine.getRawPosition(), tile);
        }
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            GridRecyclerAdapter.TileViewHolder tileViewHolder = (GridRecyclerAdapter.TileViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (tileViewHolder != null) {
                tileViewHolder.setClickable(false);
            }
        }
        score = Math.round((double) calculateBV3() * 100d / time);
    }

    private void toggleFlag(Tile tile) {
        if (!tile.isFlagged() && mineCount <= 0) {
            return;
        }
        tile.toggleFlag();

        if (tile.isFlagged()) {
            mineCount--;
        } else {
            mineCount++;
        }
        updateMines();
    }

    @SuppressLint("DefaultLocale")
    private void checkForWin() {
        if (mineCount > 0) {
            return;
        }
        for (Tile tile : mineGrid) {
            if (!tile.isRevealed() && !tile.isMine()) {
                return;
            }
        }
        finishGame();
        saveTime();

        getAlertDialogBase()
                .setMessage(String.format(getString(R.string.score_msg), score))
                .show();
    }

    private int calculateBV3() {
        int bv3 = 0;
        boolean[][] marks = new boolean[mineGrid.getRows()][mineGrid.getColumns()];
        for (int y = 0; y < mineGrid.getRows(); y++) {
            for (int x = 0; x < mineGrid.getColumns(); x++) {
                if (marks[y][x]) {
                    continue;
                }
                marks[y][x] = true;
                bv3++;
                floodFillMark(marks, x, y);
            }
        }
        for (int y = 0; y < mineGrid.getRows(); y++) {
            for (int x = 0; x < mineGrid.getColumns(); x++) {
                if (!marks[y][x] && !mineGrid.tile(mineGrid.getPosition(x, y)).isMine()) {
                    bv3++;
                }
            }
        }
        Log.i("game", "BV3 = " + bv3);
        return bv3;
    }

    private void floodFillMark(boolean[][] marks, int x, int y) {
        for (int nbY = -1; nbY <= 1; nbY++) {
            for (int nbX = -1; nbX <= 1; nbX++) {
                int markX = x + nbX;
                int markY = y + nbY;
                if (markX < 0 || markX >= mineGrid.getColumns() ||
                        markY < 0 || markY >= mineGrid.getRows()) {
                    continue;
                }
                if (marks[markY][markX]) {
                    continue;
                }

                marks[markY][markX] = true;
                if (mineGrid.getNearbyMineCount(mineGrid.getPosition(markX, markY)) == 0) {
                    floodFillMark(marks, markX, markY);
                }
            }
        }
    }

    private void saveTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS, 0);
        int gameCount = prefs.getInt("gameCount", -1) + 1;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("gameCount", gameCount);
        edit.putInt("difficulty-" + gameCount, difficulty.ordinal());
        edit.putInt("time-" + gameCount, time);
        edit.putString("date-" + gameCount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss")));
        edit.putLong("score-" + gameCount, score);
        edit.apply();
        // logsth();
    }

    private void logsth() {
        SharedPreferences prefs = getSharedPreferences(PREFS, 0);
        int gameCount = prefs.getInt("gameCount", -1) + 1;
        Log.i("game", "gameCount = " + gameCount);
        for (int i = 0; i < gameCount; i++) {
            Log.i("game", "Game #" + i);
            Log.i("game", "difficulty = " + Game.Difficulty.values()[prefs.getInt("difficulty-" + i, 0)]);
            Log.i("game", "time = " + prefs.getInt("time-" + i, 0) + "\n");
        }
    }
}