package cz.zcu.kiv.jsmahy.minesweeper.game;

public interface ItemClickListener {
    void onClick(GridRecyclerAdapter.TileViewHolder viewHolder);

    int MINE_REVEAL = -1;
    int FLAGGED = -2;
    int NOTHING = -3;
    int UNKNOWN = -4;

}
