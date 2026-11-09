package com.coderwin.minesweeper;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {
    private GameModel game() { return new GameModel(9, 9, 10, new Random(42)); }

    @Test void allDifficultiesHaveExactMineCountsAndSafeStarts() {
        for (Difficulty d : Difficulty.values()) for (int seed=0;seed<30;seed++) {
            GameModel g=new GameModel(d.rows(),d.columns(),d.mines(),new Random(seed));
            int row=seed%d.rows(), col=(seed*7)%d.columns();
            g.reveal(row,col);
            assertEquals(0,g.adjacentMines(row,col));
            int count=0;
            for(int r=0;r<g.rows();r++) for(int c=0;c<g.columns();c++) {
                if(g.isMine(r,c)) count++;
                assertFalse(g.isMine(r,c) && g.isRevealed(r,c));
            }
            assertEquals(d.mines(),count);
        }
    }

    @Test void everyCellCanBeASafeFirstClick() {
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            GameModel g = game();
            g.reveal(r, c);
            assertEquals(0, g.adjacentMines(r, c));
            for (int nr = Math.max(0, r - 1); nr <= Math.min(8, r + 1); nr++)
                for (int nc = Math.max(0, c - 1); nc <= Math.min(8, c + 1); nc++) assertFalse(g.isMine(nr, nc));
            int count = 0;
            for (int nr = 0; nr < 9; nr++) for (int nc = 0; nc < 9; nc++) if (g.isMine(nr, nc)) count++;
            assertEquals(10, count);
        }
    }

    @Test void numbersMatchNeighboringMines() {
        GameModel g = game();
        g.reveal(4, 4);
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            if (g.isMine(r, c)) continue;
            int expected = 0;
            for (int nr = Math.max(0, r - 1); nr <= Math.min(8, r + 1); nr++)
                for (int nc = Math.max(0, c - 1); nc <= Math.min(8, c + 1); nc++) if (g.isMine(nr, nc)) expected++;
            assertEquals(expected, g.adjacentMines(r, c));
        }
    }

    @Test void floodOpensConnectedZerosAndTheirBorderButNotFlags() {
        GameModel g = game();
        g.toggleFlag(4, 5);
        g.reveal(4, 4);
        assertFalse(g.isRevealed(4, 5));
        boolean[][] expected = new boolean[9][9];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        expected[4][4] = true;
        queue.add(new int[]{4, 4});
        while (!queue.isEmpty()) {
            int[] p = queue.remove();
            if (g.adjacentMines(p[0], p[1]) != 0) continue;
            for (int r = Math.max(0, p[0]-1); r <= Math.min(8, p[0]+1); r++)
                for (int c = Math.max(0, p[1]-1); c <= Math.min(8, p[1]+1); c++)
                    if (!expected[r][c] && !g.isFlagged(r,c) && !g.isMine(r,c)) {
                        expected[r][c] = true;
                        queue.add(new int[]{r,c});
                    }
        }
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) assertEquals(expected[r][c], g.isRevealed(r,c));
    }

    @Test void flagsBlockOpeningAndCanBeRemoved() {
        GameModel g = game();
        g.toggleFlag(0, 0);
        assertEquals(9, g.flagsRemaining());
        g.reveal(0, 0);
        assertEquals(GameModel.State.READY, g.state());
        g.toggleFlag(0, 0);
        assertEquals(10, g.flagsRemaining());
        g.reveal(0, 0);
        g.toggleFlag(0, 0);
        assertFalse(g.isFlagged(0, 0));
    }

    @Test void openingAMineLosesAndLocksInput() {
        GameModel g = game();
        g.reveal(4, 4);
        outer: for (int r=0;r<9;r++) for(int c=0;c<9;c++) if(g.isMine(r,c)) {
            g.reveal(r,c);
            assertEquals(GameModel.State.LOST,g.state());
            g.toggleFlag(r,c);
            assertFalse(g.isFlagged(r,c));
            break outer;
        }
        assertEquals(GameModel.State.LOST,g.state());
    }

    @Test void openingAllSafeCellsWinsWithoutFlags() {
        GameModel g=game();
        g.reveal(4,4);
        for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(!g.isMine(r,c)) g.reveal(r,c);
        assertEquals(GameModel.State.WON,g.state());
        for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(g.isMine(r,c)) {
            g.reveal(r,c);
            g.toggleFlag(r,c);
            assertFalse(g.isRevealed(r,c));
            assertFalse(g.isFlagged(r,c));
        }
        assertEquals(GameModel.State.WON,g.state());
    }

    @Test void resetClearsAllState() {
        GameModel g=game();
        g.toggleFlag(0,0);
        g.reveal(4,4);
        g.reset();
        assertEquals(GameModel.State.READY,g.state());
        assertEquals(10,g.flagsRemaining());
        for(int r=0;r<9;r++) for(int c=0;c<9;c++) {
            assertFalse(g.isMine(r,c));
            assertFalse(g.isRevealed(r,c));
            assertFalse(g.isFlagged(r,c));
            assertEquals(0,g.adjacentMines(r,c));
        }
    }

    @Test void rejectsInvalidBoardsAndCoordinates() {
        assertThrows(IllegalArgumentException.class,()->new GameModel(3,3,1));
        assertThrows(IllegalArgumentException.class,()->new GameModel(9,9,73));
        assertThrows(IndexOutOfBoundsException.class,()->game().reveal(-1,0));
        assertThrows(IndexOutOfBoundsException.class,()->game().toggleFlag(9,0));
    }
}
