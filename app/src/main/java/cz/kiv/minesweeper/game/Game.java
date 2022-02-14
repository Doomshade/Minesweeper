package cz.kiv.minesweeper.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;
import java.util.StringJoiner;

public class Game {
    public static final String GAME_VALUES = "game-values";
    public static final String S_WIDTH_AND_HEIGHT = "width-and-height";
    public static final String S_DIFFICULTY = "difficulty";
    private static final int MAX_WIDTH_AND_HEIGHT = 16;
    private static final String L_TAG = "game";
    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.EASY;
    private static final int DEFAULT_WIDTH_AND_HEIGHT = 8;
    private static final int MAX_RECURSION_CALLS = 10;
    private static final int FLAG = -3;
    private static final int REVEALED = -2;
    private static final int MINE = -1;
    private static final int NOT_REVEALED = 0;
    private final int widthAndHeight;
    private final boolean[][] mines;
    private final Difficulty gameDifficulty;
    private final int maxMines;
    private final int[][] state;

    /**
     * Creates a new game
     *
     * @param widthAndHeight the width and height of the mine grid
     * @param gameDifficulty the game difficulty
     * @throws IllegalArgumentException if the width and height is invalid
     */
    public Game(int widthAndHeight, Difficulty gameDifficulty) throws IllegalArgumentException {
        if (widthAndHeight < 0 || widthAndHeight > MAX_WIDTH_AND_HEIGHT) {
            throw new IllegalArgumentException(String.format(
                    "Invalid width and height value %d! Value must be (%d, %d>",
                    widthAndHeight, 0, MAX_WIDTH_AND_HEIGHT));
        }

        this.gameDifficulty = gameDifficulty;
        this.widthAndHeight = widthAndHeight;
        this.mines = new boolean[widthAndHeight][widthAndHeight];
        this.state = new int[widthAndHeight][widthAndHeight];
        this.maxMines = (int) (widthAndHeight * widthAndHeight * 0.75);
    }

    /**
     * Creates a new game
     *
     * @param context the context of the application
     * @return a new game
     */
    public static Game createGame(Context context) {
        Log.i(L_TAG, "Creating a new game");
        SharedPreferences prefs = context.getSharedPreferences(GAME_VALUES, 0);

        Difficulty difficulty;
        int diff = prefs.getInt(S_DIFFICULTY, DEFAULT_DIFFICULTY.ordinal());
        try {
            difficulty = Difficulty.values()[diff];
        } catch (Exception e) {
            Log.wtf(L_TAG, "Received an invalid game difficulty: " + diff);
            difficulty = DEFAULT_DIFFICULTY;
        }

        return new Game(prefs.getInt(S_WIDTH_AND_HEIGHT, DEFAULT_WIDTH_AND_HEIGHT), difficulty);
    }

    /**
     * Validates the coord
     *
     * @param x the x coord
     * @param y the y coord
     * @throws IllegalArgumentException if the coord is invalid
     */
    private void validateBounds(int x, int y) throws IllegalArgumentException {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException(String.format("Invalid x and y coords! (%d, %d)", x, y));
        }
    }

    /**
     * Sets/unsets a mine on a coord
     *
     * @param x      the x coord
     * @param y      the y coord
     * @param isMine whether to set the coord as a mine
     * @return the boolean set or false
     * @throws IllegalArgumentException if the x or y is out of bounds
     */
    public boolean setMine(int x, int y, boolean isMine) throws IllegalArgumentException {
        validateBounds(x, y);
        mines[y][x] = isMine;
        state[y][x] = MINE;
        return isMine;
    }

    /**
     * Reveals a field
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if there was <b>NO</b> mine (the reveal was successful), {@code false} if there was a mine (the reveal was unsuccessful)
     */
    public boolean reveal(int x, int y) {
        if (isMine(x, y)) {
            return false;
        }
        state[y][x] = REVEALED;
        return true;
    }

    /**
     * Toggles a flag on a field
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if the flag was toggled, {@code false otherwise}
     */
    public boolean toggleFlag(int x, int y) {
        switch (state[y][x]) {
            case FLAG:
                state[y][x] = NOT_REVEALED;
                break;
            case NOT_REVEALED:
                state[y][x] = FLAG;
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Generates mines based on the difficulty
     */
    public void generateMines() {
        Log.i(L_TAG, "Generating mines...");
        generateMines(0);
    }

    private void generateMines(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Invalid depth " + depth);
        }
        if (depth >= MAX_RECURSION_CALLS) {
            Log.e(L_TAG, "Failed to generate mines way too many times!");
            return;
        }

        final double mineProbability = gameDifficulty.getMineProbability();
        Log.v(L_TAG, "Mine probability: " + mineProbability);
        final Random r = new Random();
        int mineCount = 0;
        for (int i = 0; i < widthAndHeight; i++) {
            for (int j = 0; j < widthAndHeight; j++) {
                double generatedValue = r.nextDouble();
                Log.v(L_TAG, "Generated value: " + generatedValue);
                if (setMine(i, j, generatedValue <= mineProbability)) {
                    mineCount++;

                    Log.v(L_TAG, String.format("Set a mine on %d,%d (%.1f <= %.1f)",
                            j, i, generatedValue, mineProbability));
                    // too many mines, generate again
                    if (mineCount > maxMines) {
                        Log.d(L_TAG, "Generated too many mines, generating them again...");
                        generateMines(depth + 1);
                        return;
                    }
                }
            }
        }
        Log.d(L_TAG, "Successfully generated mines: " + Arrays.deepToString(mines));
        Log.d(L_TAG, "Mine count: " + mineCount);
    }

    /**
     * Checks whether a coord is in bounds
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if x, y {@literal >} 0 && x, y {@literal <} widthAndHeight
     */
    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 &&
                x < widthAndHeight && y < widthAndHeight;
    }

    /**
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if x and y are in bounds, and a mine is present
     */
    public boolean isMine(int x, int y) {
        return isInBounds(x, y) && mines[y][x];
    }

    /**
     * @param x the x coord
     * @param y the y coord
     * @return the amount of mines on the given coord
     */
    public int getNeighbourMines(int x, int y) {
        if (isMine(x, y)) {
            return MINE;
        }
        int mines = 0;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; j++) {
                if (isMine(x + j, y + i)) {
                    mines++;
                }
            }
        }
        return mines;
    }

    public StringBuilder printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < widthAndHeight; y++) {
            sb.append("| ");
            for (int x = 0; x < widthAndHeight; x++) {
                int mines = getNeighbourMines(x, y);
                switch (mines) {
                    case MINE:
                        sb.append("B");
                        break;
                    case FLAG:
                        sb.append("F");
                        break;
                    case REVEALED:
                        sb.append("R");
                        break;
                    default:
                        sb.append(mines);
                        break;
                }
                sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb;
    }

    public int getWidthAndHeight() {
        return widthAndHeight;
    }

    public Difficulty getGameDifficulty() {
        return gameDifficulty;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Game.class.getSimpleName() + "[", "]")
                .add("widthAndHeight=" + widthAndHeight)
                .add("mines=" + Arrays.deepToString(mines))
                .add("gameDifficulty=" + gameDifficulty)
                .add("maxMines=" + maxMines)
                .add("state=" + Arrays.deepToString(state))
                .toString();
    }

    public int getMaxMines() {
        return maxMines;
    }

    /**
     * The game difficulty
     */
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD;

        public double getMineProbability() {
            return (double) (ordinal() + 1) / 5;
        }
    }
}
