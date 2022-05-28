package cz.zcu.kiv.jsmahy.minesweeper.game;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cz.zcu.kiv.jsmahy.minesweeper.R;

public class GridRecyclerAdapter extends RecyclerView.Adapter<GridRecyclerAdapter.TileViewHolder> {
    private MineGrid mineGrid;
    private final ClickListener clickListener;

    public GridRecyclerAdapter(MineGrid mineGrid, ClickListener clickListener) {
        this.mineGrid = mineGrid;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell, parent, false);
        return new TileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        holder.bind(mineGrid.tile(position));
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return mineGrid.size();
    }

    public void setTiles(Tile[][] tiles) {
        this.mineGrid = new MineGrid(tiles);
        notifyDataSetChanged();
    }


    protected class TileViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton imageTile;
        private final ImageView number;

        public TileViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageTile = itemView.findViewById(R.id.tile);
            this.number = itemView.findViewById(R.id.number);
        }


        private void bind(final Tile tile) {
            Log.i("recycler", "BIND");
            itemView.setBackgroundColor(Color.GRAY);
            this.imageTile.setOnClickListener(view -> clickListener.onClick(tile));
            itemView.setOnClickListener(view -> clickListener.onClick(tile));
        }
    }
}
