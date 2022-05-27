package cz.zcu.kiv.jsmahy.minesweeper.game.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static cz.zcu.kiv.jsmahy.minesweeper.game.impl.GameImpl.DEFAULT_WIDTH_AND_HEIGHT;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cz.zcu.kiv.jsmahy.minesweeper.game.Game;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class GameImplTest {
    private static final int WIDTH = 8;
    private static final int HEIGHT = 8;

    private Game game;

    @Mock
    private Context context;
    @Mock
    private SharedPreferences sharedPrefs;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        game = new GameImpl(WIDTH, HEIGHT, Game.Difficulty.EASY);

        PowerMockito.mockStatic(Log.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        when(sharedPrefs.getInt(anyString(), anyInt())).thenReturn(DEFAULT_WIDTH_AND_HEIGHT);
    }

    @Test
    public void testSetMinesAndValidateIsMines() {
        game.setMine(1, 2);
        game.setMine(2, 4);
        game.setMine(2, 1);
        game.setMine(WIDTH - 1, 0);
        game.setMine(0, HEIGHT - 1);

        assertTrue(game.isMine(1, 2));
        assertTrue(game.isMine(2, 4));
        assertTrue(game.isMine(2, 1));
        assertTrue(game.isMine(WIDTH - 1, 0));
        assertTrue(game.isMine(0, HEIGHT - 1));
        assertFalse(game.isMine(4, 2));
    }


    @Test
    public void testMineBounds() {
        assertThrows(IllegalArgumentException.class, () -> game.setMine(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> game.setMine(0, -2));
        assertThrows(IllegalArgumentException.class, () -> game.setMine(WIDTH, 0));
        assertThrows(IllegalArgumentException.class, () -> game.setMine(0, HEIGHT));
    }

    @Test
    public void testCtor() {
        assertThrows(IllegalArgumentException.class, () -> new GameImpl(0, 5, GameImpl.Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new GameImpl(-5, 5, GameImpl.Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new GameImpl(5, 0, GameImpl.Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new GameImpl(5, -5, GameImpl.Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new GameImpl(5, 5, null));
    }

    @Test
    public void testGameFactoryMethod() {
        final Game g = GameImpl.instantiateGame(context);
        assertThrows(IllegalArgumentException.class, () -> g.setMine(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> g.setMine(0, -1));

        g.setMine(0, 0);
        assertTrue(g.isMine(0, 0));
        assertFalse(g.isMine(1, 0));
        assertFalse(g.isMine(0, 1));
    }
}
