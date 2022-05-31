package cz.zcu.kiv.jsmahy.minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
    private ImageButton btn = null;
    private int mineCount = 0;
    private int time = 0;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
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
        }
        mineGrid.generateMines(getApplicationContext());

        mineCountTv = findViewById(R.id.mineCount);
        mineCountTv.setText(String.valueOf(mineCount));

        timeTv = findViewById(R.id.time);
        timeTv.setText(String.valueOf(time));
        timerHandler.post(timerRunnable);

        this.btn = findViewById(R.id.imageButton);
        this.btn.setOnClickListener(view -> {
            flagging = !flagging;
            btn.setImageResource(flagging ? R.drawable.ic_icon_tile_flagged_red : R.drawable.ic_icon_tile_flagged);
        });
        this.recyclerView = findViewById(R.id.game_grid);

        this.adapter = new GridRecyclerAdapter(this, mineGrid, this);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this, mineGrid.getRows()));
        this.recyclerView.setAdapter(adapter);

        if (gameFinish) {
            finishGame();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("minegrid", mineGrid);
        outState.putInt("mineCount", mineCount);
        outState.putInt("time", time);
        outState.putBoolean("gameFinish", gameFinish);
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
        btn.setClickable(false);
        for (Tile tile : mineGrid) {
            tile.setClickable(false);
        }
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            GridRecyclerAdapter.TileViewHolder tileViewHolder = (GridRecyclerAdapter.TileViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (tileViewHolder != null) {
                tileViewHolder.setClickable(false);
            }
        }
    }

    private void toggleFlag(Tile tile) {
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