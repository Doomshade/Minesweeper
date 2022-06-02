package cz.zcu.kiv.jsmahy.minesweeper.game;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

/**
 * The minesweeper game (engine)
 *
 * @author Jakub Šmrha
 * @version 30. 3. 2022
 * @since 30. 3. 2022
 */
public interface Game {
    /**
     * Sets/unsets a mine on a coord
     *
     * @param x the x coord
     * @param y the y coord
     * @throws IllegalArgumentException if the x or y is out of bounds
     */
    void setMine(int x, int y) throws IllegalArgumentException;

    /**
     * Reveals a field
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if there was <b>NO</b> mine (the reveal was successful), {@code false} if there was a mine (the reveal was unsuccessful)
     */
    boolean reveal(int x, int y);

    /**
     * Toggles a flag on a field
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if the flag was toggled, {@code false otherwise}
     */
    boolean toggleFlag(int x, int y);

    /**
     * Generates mines based on the difficulty
     */
    void generateMines();

    /**
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if x and y are in bounds, and a mine is present
     */
    boolean isMine(int x, int y);

    /**
     * @param x the x coord
     * @param y the y coord
     * @return the amount of mines on the given coord
     */
    int getNeighbourMines(int x, int y);

    /**
     * @return the string representation of the board
     */
    String printBoard();

    MineGrid getMineGrid();

    /**
     * The game difficulty
     */
    enum Difficulty {
        EASY(10, 9, 9),
        MEDIUM(40, 16, 16);

        private final int rows;
        private final int columns;
        private final int mineCount;
        private final String resName;

        Difficulty(int mineCount, int rows, int columns) {
            this.mineCount = mineCount;
            this.rows = rows;
            this.columns = columns;
            this.resName = String.format("difficulty_%d", ordinal());
        }

        public int getRows() {
            return rows;
        }

        public int getColumns() {
            return columns;
        }

        public int getMineCount() {
            return mineCount;
        }

        @NonNull
        public String toString(Context context) {
            final Resources resources = context.getResources();
            final int id = resources.getIdentifier(resName, "string", context.getPackageName());
            return resources.getString(id);
        }
    }
}
