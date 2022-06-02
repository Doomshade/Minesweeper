package cz.zcu.kiv.jsmahy.minesweeper;

import static cz.zcu.kiv.jsmahy.minesweeper.ScoreboardActivity.PREFS;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
    private static final Game.Difficulty difficulty = Game.Difficulty.MEDIUM;
    private final Handler timerHandler = new Handler();
    private RecyclerView recyclerView = null;
    private GridRecyclerAdapter adapter = null;
    private MineGrid mineGrid = null;
    private ImageButton flagButton = null;
    private ImageButton restartButton = null;
    private MineGrid.Position clickedMine = null;
    private int mineCount = 0;
    private int time = 0;
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
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.hide();
        }
        hideSystemBars();
        if (state == null) {
            mineGrid = new MineGrid(difficulty);
            mineCount = difficulty.getMineCount();
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
            timerHandler.postDelayed(timerRunnable, 1000L);
        }

        this.flagButton = findViewById(R.id.flag);
        this.flagButton.setOnClickListener(view -> {
            flagging = !flagging;
            flagButton.setImageResource(flagging ? R.drawable.ic_icon_tile_flagged_red : R.drawable.ic_icon_tile_flagged);
        });
        flagButton.setImageResource(flagging ? R.drawable.ic_icon_tile_flagged_red : R.drawable.ic_icon_tile_flagged);
        this.recyclerView = findViewById(R.id.game_grid);

        // this.restartButton = findViewById(R.id.restart);
        // restartButton.setOnClickListener(x -> triggerRebirth());
        this.adapter = new GridRecyclerAdapter(this, mineGrid, this);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this, mineGrid.getRows()));
        this.recyclerView.setAdapter(adapter);

        if (gameFinish) {
            finishGame();
        }

        restartButton = findViewById(R.id.restart);
        restartButton.setOnClickListener(view -> {
            if (gameFinish) {
                triggerRebirth();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.restart_prompt_title)
                        .setMessage(R.string.restart_prompt_message)
                        .setPositiveButton(R.string.answer_yes, (dialogInterface, i) -> triggerRebirth())
                        .setNegativeButton(R.string.answer_no, ((dialogInterface, i) -> dialogInterface.cancel()))
                        .show();
            }
        });
    }

    private void updateMines() {
        mineCountTv.setText(String.valueOf(mineCount));
    }

    private void updateTime() {
        timeTv.setText(String.valueOf(time));
    }

    private void triggerRebirth() {
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

        // scan neighbouring mines
        return mineGrid.getNearbyMineCount(position);
    }

    @Override
    public void onClick(GridRecyclerAdapter.TileViewHolder view) {
        if (!started) {
            started = true;
            timerHandler.postDelayed(timerRunnable, 1000L);
        }
        onClick(view, new HashSet<>());
    }

    public void onClick(GridRecyclerAdapter.TileViewHolder view, Set<MineGrid.Position> probedTiles) {
        MineGrid.Position position = mineGrid.getPosition(view.getAdapterPosition());
        handleStatus(position, probedTiles);
        checkForWin();
    }

    private void handleStatus(MineGrid.Position position, Set<MineGrid.Position> probedTiles) {
        if (!mineGrid.isInBounds(position) || !probedTiles.add(position)) {
            return;
        }

        final boolean recursing = probedTiles.size() > 1;
        final int status = getStatus(position);
        final Tile tile = mineGrid.tile(position);
        switch (status) {
            case UNKNOWN:
                break;
            case FLAGGED:
                if (flagging && !recursing) {
                    toggleFlag(tile);
                }
                break;
            case NOTHING:
                // nothing happens
                break;
            case MINE_REVEAL:
                if (!flagging) {
                    clickedMine = position;
                    Toast.makeText(getApplicationContext(), "Konec hry!", Toast.LENGTH_LONG).show();
                    finishGame();
                }
            default:
                if (!flagging) {
                    tile.reveal(status);
                    // no mines found, explore the other ones as well
                    if (status == 0) {
                        for (int y = -1; y <= 1; y++) {
                            for (int x = -1; x <= 1; x++) {
                                handleStatus(position.add(x, y), probedTiles);
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
        adapter.notifyItemChanged(position.getRawPosition(), mineGrid.tile(position));
    }

    private void finishGame() {
        gameFinish = true;
        timerHandler.removeCallbacks(timerRunnable);
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
        Toast.makeText(getApplicationContext(), R.string.the_end, Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        builder.setTitle(R.string.the_end)
                .setMessage(R.string.you_won)
                .setNeutralButton(R.string.confirm, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
    }

    private void saveTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS, 0);
        int gameCount = prefs.getInt("gameCount", -1) + 1;
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("gameCount", gameCount);
        edit.putInt("difficulty-" + gameCount, difficulty.ordinal());
        edit.putInt("time-" + gameCount, time);
        edit.putString("date-" + gameCount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss")));
        edit.apply();
        logsth();
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