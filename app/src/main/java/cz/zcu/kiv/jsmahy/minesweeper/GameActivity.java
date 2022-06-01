package cz.zcu.kiv.jsmahy.minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
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

import java.util.HashSet;
import java.util.Set;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;
import cz.zcu.kiv.jsmahy.minesweeper.game.GridRecyclerAdapter;
import cz.zcu.kiv.jsmahy.minesweeper.game.ItemClickListener;
import cz.zcu.kiv.jsmahy.minesweeper.game.MineGrid;
import cz.zcu.kiv.jsmahy.minesweeper.game.Tile;

public class GameActivity extends AppCompatActivity implements ItemClickListener {
    private RecyclerView recyclerView = null;
    private GridRecyclerAdapter adapter = null;
    private MineGrid mineGrid = null;
    private ImageButton flagButton = null;
    private Button restartButton = null;
    private int mineCount = 0;
    private int time = 0;
    private boolean started = false;
    private TextView mineCountTv = null;
    private TextView timeTv = null;
    private static final Game.Difficulty difficulty = Game.Difficulty.MEDIUM;

    private final Handler timerHandler = new Handler();
    private boolean gameFinish = false;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            time++;
            timeTv.setText(String.valueOf(time));
            timerHandler.postDelayed(this, 1000);
        }
    };

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.hide();
        }
        hideSystemBars();
        if (savedInstanceState == null) {
            mineGrid = new MineGrid(difficulty);
            switch (difficulty) {
                case EASY:
                    mineCount = 10;
                    break;
                case MEDIUM:
                    mineCount = 40;
                    break;
            }
        } else {
            mineGrid = savedInstanceState.getParcelable("minegrid");
            mineCount = savedInstanceState.getInt("mineCount");
            time = savedInstanceState.getInt("time");
            gameFinish = savedInstanceState.getBoolean("gameFinish");
            started = savedInstanceState.getBoolean("started");
            flagging = savedInstanceState.getBoolean("flagging");
        }
        mineGrid.generateMines(getApplicationContext());

        mineCountTv = findViewById(R.id.mineCount);
        mineCountTv.setText(String.valueOf(mineCount));

        timeTv = findViewById(R.id.time);
        timeTv.setText(String.valueOf(time));

        if (started) {
            timerHandler.post(timerRunnable);
        }

        this.flagButton = findViewById(R.id.imageButton);
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
            timerHandler.post(timerRunnable);
        }
        onClick(view, new HashSet<>());
    }

    public void onClick(GridRecyclerAdapter.TileViewHolder view, Set<MineGrid.Position> probedTiles) {
        MineGrid.Position position = mineGrid.getPosition(view.getAdapterPosition());
        handleStatus(position, probedTiles);
        checkForWin();
    }

    private boolean flagging = false;

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
        mineCountTv.setText(String.valueOf(mineCount));
    }

    private void checkForWin() {

    }
}