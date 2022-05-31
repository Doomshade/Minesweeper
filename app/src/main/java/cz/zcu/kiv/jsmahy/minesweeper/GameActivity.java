package cz.zcu.kiv.jsmahy.minesweeper;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;
import cz.zcu.kiv.jsmahy.minesweeper.game.GridRecyclerAdapter;
import cz.zcu.kiv.jsmahy.minesweeper.game.ItemClickListener;
import cz.zcu.kiv.jsmahy.minesweeper.game.MineGrid;
import cz.zcu.kiv.jsmahy.minesweeper.game.Tile;

public class GameActivity extends AppCompatActivity implements ItemClickListener {
    private RecyclerView recyclerView = null;
    private MineGrid mineGrid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        Log.i("log-game", "current grid: " + mineGrid);
        Log.i("log-game", "instance state = " + savedInstanceState);
        Log.i("log-game", "aaaaaa");
        if (savedInstanceState == null){
            mineGrid = new MineGrid(Game.Difficulty.MEDIUM);
        } else {
            mineGrid = savedInstanceState.getParcelable("minegrid");
        }
        mineGrid.generateMines(getApplicationContext());

        this.recyclerView = findViewById(R.id.game_grid);
        // this.game = GameImpl.instantiateGame(getApplicationContext());
        GridRecyclerAdapter gridRecyclerAdapter = new GridRecyclerAdapter(this, mineGrid, this);

        this.recyclerView.setLayoutManager(new GridLayoutManager(this, mineGrid.getRows()));
        this.recyclerView.setAdapter(gridRecyclerAdapter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("minegrid", mineGrid);
    }

    private boolean solving = false;

    private int getStatus(MineGrid.Position position) {
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
        final MineGrid.Position position = mineGrid.getPosition(view.getAdapterPosition());
        if (!probedTiles.add(position)) {
            return;
        }

        Log.i("grid", "Click at " + position);
        handleStatus(view, position);
        checkForWin();
    }

    private boolean flagging = false;

    private void handleStatus(GridRecyclerAdapter.TileViewHolder view, MineGrid.Position position) {
        final int status = getStatus(position);
        final Tile tile = mineGrid.tile(position);
        switch (status) {
            case UNKNOWN:
                Log.i("grid", "hit unknown??");
                break;
            case FLAGGED:
                if (flagging) {
                    tile.toggleFlag();
                    notifyItemChanged(view);
                }
                break;
            case NOTHING:
                // nothing happens
                Log.i("grid", "hit nothing");
                break;
            case MINE_REVEAL:
            default:
                // no mines found, reveal other neighbouring tiles that have no mines
                // yes this is recursive
                if (!flagging) {
                    mineGrid.tile(position).reveal(status);
                    notifyItemChanged(view);
                }
                break;
        }
        if (!solving) {
            solving = true;
            //solve();
        }
    }

    private void notifyItemChanged(GridRecyclerAdapter.TileViewHolder view) {
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(view.getAdapterPosition());
    }

    private void solve() {
        final Set<MineGrid.Position> probedTiles = new HashSet<>();
        for (int y = 0; y < mineGrid.getRows(); y++) {
            for (int x = 0; x < mineGrid.getColumns(); x++) {
                final MineGrid.Position newPos = mineGrid.getPosition(x, y);
                Log.i("grid", "Recursing to " + newPos);
                onClick((GridRecyclerAdapter.TileViewHolder) Objects.requireNonNull(recyclerView.findViewHolderForLayoutPosition(newPos.getRawPosition())), probedTiles);
            }
        }
    }

    private void checkForWin() {

    }
}