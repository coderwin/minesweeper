package com.coderwin.minesweeper;

public enum Difficulty {
    BEGINNER("초급", 9, 9, 10),
    INTERMEDIATE("중급", 16, 16, 40),
    EXPERT("고급", 16, 30, 99);

    private final String label;
    private final int rows;
    private final int columns;
    private final int mines;

    Difficulty(String label, int rows, int columns, int mines) {
        this.label = label;
        this.rows = rows;
        this.columns = columns;
        this.mines = mines;
    }

    public GameModel newGame() { return new GameModel(rows, columns, mines); }
    public int rows() { return rows; }
    public int columns() { return columns; }
    public int mines() { return mines; }
    @Override public String toString() { return label; }
}
