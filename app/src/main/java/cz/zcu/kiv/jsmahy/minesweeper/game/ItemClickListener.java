package cz.zcu.kiv.jsmahy.minesweeper.game;

/**
 * A listener for clicks on the grid
 */
public interface ItemClickListener {
    // item tile states
    int MINE_REVEAL = -1;
    int FLAGGED = -2;
    int NOTHING = -3;
    int UNKNOWN = -4;

    /**
     * @param viewHolder the view holder
     * @param longClick  whether it was a long click
     */
    void onClick(GridRecyclerAdapter.TileViewHolder viewHolder, boolean longClick);
}
