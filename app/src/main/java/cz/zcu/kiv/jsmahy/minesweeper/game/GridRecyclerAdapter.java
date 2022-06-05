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
        // holder.setIsRecyclable(false);
        holder.update(mineGrid.tile(mineGrid.getPosition(position)));
    }

    @Override
    public int getItemCount() {
        return mineGrid.getSize();
    }


    public class TileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final ImageView imageTile;
        private final ImageView number;

        public TileViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView.setOnClickListener(this);
            this.itemView.setLongClickable(true);
            this.itemView.setOnLongClickListener(this);
            this.imageTile = itemView.findViewById(R.id.tile);
            this.number = itemView.findViewById(R.id.number);
        }

        private void update(final Tile tile) {
            int mineCount = tile.getNearbyMineCount();
            if (tile.isFlagged()) {
                number.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_flagged));
                return;
            }

            if (!tile.isRevealed()) {
                number.setImageDrawable(null);
                imageTile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_hidden));
                return;
            }

            // it's a mine or sth else
            if (tile.isMine()) {
                imageTile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_white));
                if (tile.isClickedMine()) {
                    number.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_mine_found_red));
                } else {
                    number.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_mine_found));
                }
                return;
            }

            if (mineCount < 0) {
                return;
            }
            // only update if the mine has been revealed
            imageTile.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tile_white));
            if (mineCount == 0) {
                return;
            }

            final Resources resources = context.getResources();
            final String drawableStr = String.format("ic_icon_number_%d", mineCount);
            final int id = resources.getIdentifier(drawableStr, "drawable", context.getPackageName());

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
        }

        @Override
        public void onClick(View view) {
            if (mineGrid.tile(mineGrid.getPosition(getAdapterPosition())).isClickable()) {
                clickListener.onClick(this, false);
            }
        }

        public void setClickable(boolean clickable) {
            this.itemView.setClickable(clickable);
        }

        @Override
        public boolean onLongClick(View view) {
            if (mineGrid.tile(mineGrid.getPosition(getAdapterPosition())).isClickable()) {
                clickListener.onClick(this, true);
                return true;
            }
            return false;
        }
    }
}
