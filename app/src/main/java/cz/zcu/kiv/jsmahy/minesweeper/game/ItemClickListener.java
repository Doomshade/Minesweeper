package cz.zcu.kiv.jsmahy.minesweeper.game;

public interface ItemClickListener {
    int MINE_REVEAL = -1;
    int FLAGGED = -2;
    int NOTHING = -3;
    int UNKNOWN = -4;

    void onClick(GridRecyclerAdapter.TileViewHolder viewHolder, boolean longClick);

}
