package cz.zcu.kiv.jsmahy.minesweeper.game;

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

    /**
     * The game difficulty
     */
    enum Difficulty {
        EASY,
        MEDIUM,
        HARD;

        public double getMineProbability() {
            return (double) (ordinal() + 1) / 5;
        }
    }
}
