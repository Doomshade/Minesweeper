package cz.zcu.kiv.jsmahy.minesweeper.game.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Random;
import java.util.StringJoiner;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;

/**
 * Implementation of the game
 *
 * @author Jakub Šmrha
 * @version 30. 3. 2022
 * @since 30. 3. 2022
 */
public class GameImpl implements Game {
    //<editor-fold desc="Shared preferences constants">
    public static final String GAME_VALUES = "game-values";
    public static final String S_WIDTH = "width";
    public static final String S_HEIGHT = "height";
    public static final String S_DIFFICULTY = "difficulty";
    //</editor-fold>

    //<editor-fold desc="Defaults">
    static final Game.Difficulty DEFAULT_DIFFICULTY = Game.Difficulty.EASY;
    static final int DEFAULT_WIDTH_AND_HEIGHT = 8;
    //</editor-fold>

    //<editor-fold desc="Constants">
    private static final String L_TAG = "game";
    private static final int MAX_WIDTH_AND_HEIGHT = 16;
    private static final int MAX_RECURSION_CALLS = 10;
    private static final int FLAG = -3;
    private static final int REVEALED = -2;
    private static final int MINE = -1;
    private static final int NOT_REVEALED = 0;
    //</editor-fold>

    //<editor-fold desc="Fields">
    private final int width;
    private final int height;
    private final boolean[][] mineLocations;
    private final Game.Difficulty gameDifficulty;
    private final int maxMines;
    private final int[][] state;
    private boolean generatedMines;
    //</editor-fold>

    /**
     * Creates a new game
     *
     * @param width          the width and height of the mine grid
     * @param gameDifficulty the game difficulty
     * @throws IllegalArgumentException if the width and height is invalid
     */
    GameImpl(int width, int height, Game.Difficulty gameDifficulty) throws IllegalArgumentException {
        if (width <= 0 || width > MAX_WIDTH_AND_HEIGHT || height <= 0 || height > MAX_WIDTH_AND_HEIGHT) {
            throw new IllegalArgumentException(String.format(
                    "Invalid width/height value %d/%d! Value must be <%d, %d>",
                    width, height, 1, MAX_WIDTH_AND_HEIGHT));
        }
        if (gameDifficulty == null) {
            throw new IllegalArgumentException("Game difficulty cannot be null!");
        }

        this.gameDifficulty = gameDifficulty;
        this.width = width;
        this.height = height;
        this.mineLocations = new boolean[height][width];
        this.state = new int[height][width];
        this.maxMines = (int) (width * height * 0.75);
        this.generatedMines = false;
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
     * Creates a new game
     *
     * @param context the context of the application
     * @return a new game
     */
    public static Game instantiateGame(Context context) {
        Log.i(L_TAG, "Instantiating a new game");
        SharedPreferences prefs = context.getSharedPreferences(GAME_VALUES, 0);

        Game.Difficulty difficulty;
        int diff = prefs.getInt(S_DIFFICULTY, DEFAULT_DIFFICULTY.ordinal());
        try {
            difficulty = Game.Difficulty.values()[diff];
        } catch (Exception e) {
            Log.wtf(L_TAG, "Received an invalid game difficulty: " + diff, e);
            difficulty = DEFAULT_DIFFICULTY;
        }

        return new GameImpl(prefs.getInt(S_WIDTH, DEFAULT_WIDTH_AND_HEIGHT), prefs.getInt(S_HEIGHT, DEFAULT_WIDTH_AND_HEIGHT), difficulty);
    }


    @Override
    public void setMine(int x, int y) throws IllegalArgumentException {
        Log.v(L_TAG, String.format("Setting mine at x=%d, y=%d", x, y));
        validateBounds(x, y);
        mineLocations[y][x] = true;
        state[y][x] = MINE;
    }

    @Override
    public boolean reveal(int x, int y) {
        Log.v(L_TAG, String.format("Revealing a field at x=%d, y=%d", x, y));
        if (isMine(x, y)) {
            Log.d(L_TAG, "Revealed a mine. Game over!");
            return false;
        }
        state[y][x] = REVEALED;
        return true;
    }

    @Override
    public boolean toggleFlag(int x, int y) {
        switch (state[y][x]) {
            case FLAG:
                state[y][x] = NOT_REVEALED;
                break;
            case NOT_REVEALED:
                state[y][x] = FLAG;
                break;
            default:
                Log.v(L_TAG, String.format("Could not toggle flag because the field is in state %s. Valid states are: %s, %s",
                        state[y][x],
                        FLAG,
                        NOT_REVEALED));
                return false;
        }
        Log.v(L_TAG, String.format("Toggled flag to %s", state[y][x]));
        return true;
    }


    @Override
    public void generateMines() {
        Log.i(L_TAG, "Generating mines...");
        generateMines(MAX_RECURSION_CALLS);
    }

    /**
     * Generates mines until the depth
     *
     * @param depth the max depth
     */
    private void generateMines(int depth) {
        if (generatedMines) {
            Log.e(L_TAG, "Mines have already been generated!");
            return;
        }
        if (depth == 0) {
            Log.e(L_TAG, "Failed to generate mines way too many times!");
            return;
        }

        if (depth < 0 || depth > MAX_RECURSION_CALLS) {
            throw new IllegalArgumentException(String.format("Invalid depth %d. Must be <%d, %d>", depth, 0, MAX_RECURSION_CALLS));
        }

        final double mineProbability = gameDifficulty.getMineProbability();
        final Random r = new Random();
        int mineCount = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double generatedValue = r.nextDouble();
                Log.v(L_TAG, "Generated value: " + generatedValue);
                if (generatedValue <= mineProbability) {
                    setMine(i, j);
                    mineCount++;

                    Log.v(L_TAG, String.format("Set a mine on %d,%d (%.1f <= %.1f)",
                            j, i, generatedValue, mineProbability));
                    // too many mines, generate again
                    if (mineCount > maxMines) {
                        Log.d(L_TAG, "Generated too many mines, generating them again...");
                        generateMines(depth - 1);
                        return;
                    }
                }
            }
        }
        Log.d(L_TAG, "Successfully generated mines: " + Arrays.deepToString(mineLocations));
        Log.d(L_TAG, "Mine count: " + mineCount);
        generatedMines = true;
    }

    /**
     * Checks whether a coord is in bounds
     *
     * @param x the x coord
     * @param y the y coord
     * @return {@code true} if x, y {@literal >} 0 && x, y {@literal <} widthAndHeight
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 &&
                x < width && y < height;
    }

    @Override
    public boolean isMine(int x, int y) {
        return isInBounds(x, y) && mineLocations[y][x];
    }

    @Override
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

    @Override
    public String printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < width; y++) {
            sb.append("| ");
            for (int x = 0; x < width; x++) {
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
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", GameImpl.class.getSimpleName() + "[", "]")
                .add("width=" + width)
                .add("height=" + height)
                .add("mines=" + Arrays.deepToString(mineLocations))
                .add("gameDifficulty=" + gameDifficulty)
                .add("maxMines=" + maxMines)
                .add("state=" + Arrays.deepToString(state))
                .toString();
    }

}
