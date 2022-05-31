package cz.zcu.kiv.jsmahy.minesweeper.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import cz.zcu.kiv.jsmahy.minesweeper.R;

public class GridRecyclerAdapter extends RecyclerView.Adapter<GridRecyclerAdapter.TileViewHolder> {
    public static final String TAG_HOLDER = "holder";
    private final MineGrid mineGrid;
    private final LayoutInflater inflater;
    private final ItemClickListener clickListener;
    private final Context context;

    public GridRecyclerAdapter(Context context, MineGrid mineGrid, ItemClickListener clickListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mineGrid = mineGrid;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.cell, parent, false);
        return new TileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        holder.update(mineGrid.tile(mineGrid.getPosition(position)));
    }

    @Override
    public int getItemCount() {
        return mineGrid.getSize();
    }


    public class TileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imageTile;
        private final ImageView number;

        public TileViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView.setOnClickListener(this);
            this.imageTile = itemView.findViewById(R.id.tile);
            this.number = itemView.findViewById(R.id.number);
        }

        public ImageView getNumberImageView() {
            return number;
        }

        private void update(final Tile tile) {
            int mineCount = tile.getNearbyMineCount();

            // it's a mine or sth else
            if (tile.isMine() && tile.isRevealed()) {
                imageTile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_white));
                number.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_mine_found));
                Log.i(TAG_HOLDER, "Game ends!");
                return;
            }

            if (tile.isFlagged()) {

            }

            if (mineCount < 0) {
                return;
            }

            // only update if the mine has been revealed
            imageTile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_white));
            Log.i(TAG_HOLDER, "hit an empty tile which wasn't a mine");
            // the tile was empty, reveal it
            // the neighbouring mine count is the status
            Log.i(TAG_HOLDER, "Status = " + mineCount);
            if (mineCount == 0) {
                return;
            }

            final Resources resources = context.getResources();
            final String drawableStr = String.format("ic_icon_number_%d", mineCount);
            final int id = resources.getIdentifier(drawableStr, "drawable", context.getPackageName());
            Log.i(TAG_HOLDER, "drawableStr = " + drawableStr);
            Log.i(TAG_HOLDER, "ID = " + id);

            final Drawable drawable;
            try {
                drawable = ContextCompat.getDrawable(context, id);
                if (drawable == null) {
                    return;
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG_HOLDER, "Nenasel se resource :(", e);
                return;
            }

            number.setImageDrawable(drawable);
            Log.i(TAG_HOLDER, String.format("BIND %s (%d)", mineGrid.getPosition(getAdapterPosition()), getAdapterPosition()));
        }

        @Override
        public void onClick(View view) {
            Log.i("recycler", "CLICK");
            clickListener.onClick(this);
        }
    }
}
