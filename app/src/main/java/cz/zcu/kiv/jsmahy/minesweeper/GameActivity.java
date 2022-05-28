package cz.zcu.kiv.jsmahy.minesweeper;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cz.zcu.kiv.jsmahy.minesweeper.game.ClickListener;
import cz.zcu.kiv.jsmahy.minesweeper.game.Game;
import cz.zcu.kiv.jsmahy.minesweeper.game.GridRecyclerAdapter;
import cz.zcu.kiv.jsmahy.minesweeper.game.MineGrid;
import cz.zcu.kiv.jsmahy.minesweeper.game.Tile;
import cz.zcu.kiv.jsmahy.minesweeper.game.impl.GameImpl;

public class GameActivity extends AppCompatActivity implements ClickListener {
    private GridRecyclerAdapter gridRecyclerAdapter;
    private RecyclerView recyclerView;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        this.recyclerView = findViewById(R.id.game_grid);
        this.game = GameImpl.instantiateGame(getApplicationContext());
        MineGrid mineGrid = game.getMineGrid();
        this.gridRecyclerAdapter = new GridRecyclerAdapter(mineGrid, this);

        // TODO spanCount nechat jako var
        this.recyclerView.setLayoutManager(new GridLayoutManager(this, mineGrid.rowSize()));
        this.recyclerView.setAdapter(this.gridRecyclerAdapter);
        // TODO tohle použít na layering
        Log.i("GameActivity", String.valueOf(AppCompatResources.getDrawable(this, R.drawable.ic_icon_tile_white)));
//        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
    }

    @Override
    public void onClick(Tile tile) {
        Log.i("game", "CLICK");
        Toast.makeText(getApplicationContext(), "Clickkk", Toast.LENGTH_LONG).show();
    }
}