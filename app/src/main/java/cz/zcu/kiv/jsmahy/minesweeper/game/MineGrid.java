package cz.zcu.kiv.jsmahy.minesweeper.game;

import static cz.zcu.kiv.jsmahy.minesweeper.game.ItemClickListener.UNKNOWN;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.os.ConfigurationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;

public class MineGrid implements Parcelable {
    private static final String L_TAG = "minegrid";
    private Tile[][] tiles;
    private final int size;
    private final int columns;
    private final int rows;

    private boolean generatedMines = false;

    public MineGrid(Game.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                this.rows = 9;
                this.columns = 9;
                break;
            case MEDIUM:
                this.rows = 16;
                this.columns = 16;
                break;
            default:
                throw new RuntimeException("Invalid difficulty");
        }
        this.size = rows * columns;
    }

    public MineGrid(Parcel in) {
        size = in.readInt();
        columns = in.readInt();
        rows = in.readInt();
        generatedMines = in.readByte() != 0;
        tiles = new Tile[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                tiles[y][x] = in.readParcelable(Tile.class.getClassLoader());
            }
        }
    }

    public static final Creator<MineGrid> CREATOR = new Creator<MineGrid>() {
        @Override
        public MineGrid createFromParcel(Parcel in) {
            return new MineGrid(in);
        }

        @Override
        public MineGrid[] newArray(int size) {
            return new MineGrid[size];
        }
    };

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    public void generateMines(Context context) {
        if (generatedMines) {
            return;
        }
        Log.i("minegrid", "Generating mines...");
        final String fileName = String.format(ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0),
                "%dx%d.txt", columns, rows);
        final Scanner file;
        try {
            file = new Scanner(context.getAssets().open(fileName));
        } catch (IOException e) {
            Log.e("minegrid", "Nepodarilo se otevrit soubor " + fileName, e);
            return;
        }

        final List<Tile[][]> availableGrids = new ArrayList<>();
        while (file.hasNextLine()) {
            final Tile[][] grid = new Tile[rows][columns];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    grid[y][x] = new Tile();
                }
            }
            final String line = file.nextLine();
            final String[] strPositions = line.split(" ");
            for (String strPos : strPositions) {
                String[] xy = strPos.split(",");
                final int x = Integer.parseInt(xy[0]);
                final int y = Integer.parseInt(xy[1]);
                grid[y][x].setMine();
            }
            availableGrids.add(grid);
        }

        final Random random = new Random();
        this.tiles = availableGrids.get(random.nextInt(availableGrids.size()));
        this.generatedMines = true;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getSize() {
        return size;
    }

    public int[] toXY(int pos) {
        int x = pos % columns;
        int y = pos / rows;
        return new int[]{x, y};
    }

    public boolean isInBounds(Position position) {
        int x = position.x;
        int y = position.y;
        return x >= 0 && x < columns &&
                y >= 0 && y < rows;
    }

    public Tile tile(final Position position) {
        return tiles[position.y][position.x];
    }

    public void setTile(Tile tile, Position position) {
        this.tiles[position.y][position.x] = tile;
    }

    public Tile[][] getNeighbours(final Position position) {
        final Tile[][] neighbours = new Tile[3][3];
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                final int gridX = position.x + x;
                final int gridY = position.y + y;
                final Position newPos = getPosition(gridX, gridY);
                if (!isInBounds(newPos)) {
                    continue;
                }
                neighbours[y + 1][x + 1] = tile(newPos);
            }
        }
        return neighbours;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(size);
        parcel.writeInt(columns);
        parcel.writeInt(rows);
        parcel.writeByte((byte) (generatedMines ? 1 : 0));
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                parcel.writeParcelable(tiles[y][x], i);
            }
        }
    }

    public class Position {
        public final int x;
        public final int y;

        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position add(Position position) {
            return new Position(this.x + position.x, this.y + position.y);
        }

        private Position(int position) {
            this(position % columns, position / rows);
        }

        public int getRawPosition() {
            return x + y * rows;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Position.class.getSimpleName() + "[", "]")
                    .add("x=" + x)
                    .add("y=" + y)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public Position getPosition(int position) {
        return new Position(position);
    }

    public Position getPosition(int x, int y) {
        return new Position(x, y);
    }

    public int getNearbyMineCount(final Position position) {
        if (!isInBounds(position)) {
            return UNKNOWN;
        }
        Log.i("minegrid", "Searching neighbours at " + position);
        int bombCount = 0;
        Tile[][] neighbours = getNeighbours(position);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Tile tile = neighbours[y][x];
                if (tile != null && tile.isMine()) {
                    bombCount++;
                    Log.i("minegrid", "Found bomb at " + new Position(position.x + x - 1, position.y + y - 1));
                }
            }
        }
        return bombCount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MineGrid.class.getSimpleName() + "[", "]")
                .add("tiles=" + Arrays.toString(tiles))
                .add("size=" + size)
                .add("columns=" + columns)
                .add("rows=" + rows)
                .add("generatedMines=" + generatedMines)
                .toString();
    }
}
