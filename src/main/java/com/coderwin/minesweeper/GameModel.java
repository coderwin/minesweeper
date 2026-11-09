package com.coderwin.minesweeper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Game rules independent of Swing. Coordinates use row, then column. */
public final class GameModel {
    public enum State { READY, PLAYING, WON, LOST }

    private final int rows;
    private final int columns;
    private final int mineCount;
    private final Random random;
    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int[][] adjacent;
    private int flagCount;
    private int revealedCount;
    private State state;

    public GameModel(int rows, int columns, int mineCount) {
        this(rows, columns, mineCount, new Random());
    }

    public GameModel(int rows, int columns, int mineCount, Random random) {
        if (rows < 3 || columns < 3 || mineCount < 1
                || (long) rows * columns > Integer.MAX_VALUE
                || mineCount > (long) rows * columns - 9) {
            throw new IllegalArgumentException("Board must allow a safe 3x3 first click");
        }
        this.rows = rows;
        this.columns = columns;
        this.mineCount = mineCount;
        this.random = Objects.requireNonNull(random);
        reset();
    }

    public void reset() {
        mines = new boolean[rows][columns];
        revealed = new boolean[rows][columns];
        flagged = new boolean[rows][columns];
        adjacent = new int[rows][columns];
        flagCount = 0;
        revealedCount = 0;
        state = State.READY;
    }

    public void reveal(int row, int column) {
        checkBounds(row, column);
        if (isFinished() || revealed[row][column] || flagged[row][column]) return;
        if (state == State.READY) {
            placeMines(row, column);
            state = State.PLAYING;
        }
        if (mines[row][column]) {
            revealed[row][column] = true;
            state = State.LOST;
            return;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        open(row, column, queue);
        while (!queue.isEmpty()) {
            int cell = queue.removeFirst();
            int r = cell / columns;
            int c = cell % columns;
            for (int nr = Math.max(0, r - 1); nr <= Math.min(rows - 1, r + 1); nr++) {
                for (int nc = Math.max(0, c - 1); nc <= Math.min(columns - 1, c + 1); nc++) {
                    if (!revealed[nr][nc] && !flagged[nr][nc] && !mines[nr][nc]) open(nr, nc, queue);
                }
            }
        }
        if (revealedCount == rows * columns - mineCount) state = State.WON;
    }

    private void open(int row, int column, ArrayDeque<Integer> queue) {
        revealed[row][column] = true;
        revealedCount++;
        if (adjacent[row][column] == 0) queue.addLast(row * columns + column);
    }

    public void toggleFlag(int row, int column) {
        checkBounds(row, column);
        if (isFinished() || revealed[row][column]) return;
        flagged[row][column] = !flagged[row][column];
        flagCount += flagged[row][column] ? 1 : -1;
    }

    private void placeMines(int safeRow, int safeColumn) {
        List<Integer> candidates = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (Math.abs(r - safeRow) > 1 || Math.abs(c - safeColumn) > 1) candidates.add(r * columns + c);
            }
        }
        Collections.shuffle(candidates, random);
        for (int i = 0; i < mineCount; i++) {
            int cell = candidates.get(i);
            int r = cell / columns;
            int c = cell % columns;
            mines[r][c] = true;
            for (int nr = Math.max(0, r - 1); nr <= Math.min(rows - 1, r + 1); nr++) {
                for (int nc = Math.max(0, c - 1); nc <= Math.min(columns - 1, c + 1); nc++) adjacent[nr][nc]++;
            }
        }
    }

    private void checkBounds(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) throw new IndexOutOfBoundsException();
    }

    public int rows() { return rows; }
    public int columns() { return columns; }
    public int mineCount() { return mineCount; }
    public int flagsRemaining() { return mineCount - flagCount; }
    public State state() { return state; }
    public boolean isFinished() { return state == State.WON || state == State.LOST; }
    public boolean isMine(int row, int column) { checkBounds(row, column); return mines[row][column]; }
    public boolean isRevealed(int row, int column) { checkBounds(row, column); return revealed[row][column]; }
    public boolean isFlagged(int row, int column) { checkBounds(row, column); return flagged[row][column]; }
    public int adjacentMines(int row, int column) { checkBounds(row, column); return adjacent[row][column]; }
}
