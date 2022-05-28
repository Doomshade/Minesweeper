package cz.zcu.kiv.jsmahy.minesweeper.game;

public class MineGrid {
    private Tile[][] tiles;
    private int size;
    private int rowSize;

    public MineGrid(Tile[][] tiles) {
        this.tiles = tiles;
        this.size = tiles.length * tiles.length;
    }

    public MineGrid(int rowSize) {
        this.rowSize = rowSize;
        this.size = rowSize * rowSize;
        this.tiles = new Tile[rowSize][rowSize];

        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                tiles[i][j] = new Tile();
            }
        }
    }

    public int rowSize() {
        return rowSize;
    }

    public int size() {
        return size;
    }

    public Tile tile(int pos) {
        return tile(pos % rowSize, pos / rowSize);
    }

    public Tile tile(int x, int y) {
        return tiles[y][x];
    }

    public void setTile(Tile tile, int x, int y) {
        this.tiles[y][x] = tile;
    }
}
