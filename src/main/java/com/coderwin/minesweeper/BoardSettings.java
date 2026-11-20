package com.coderwin.minesweeper;

/** UI limits keep custom boards playable and reserve the largest first-click safe area. */
public record BoardSettings(int rows, int columns, int mines) {
    public BoardSettings {
        if(rows<3 || rows>30 || columns<3 || columns>40 || mines<1 || mines>rows*columns-9)
            throw new IllegalArgumentException("가로 3~40, 세로 3~30, 지뢰 1~(전체 칸 수 - 9)로 입력해 주세요.");
    }

    public static BoardSettings parse(String rows,String columns,String mines) {
        try {
            return new BoardSettings(Integer.parseInt(rows.trim()),Integer.parseInt(columns.trim()),Integer.parseInt(mines.trim()));
        } catch(NumberFormatException ex) {
            throw new IllegalArgumentException("가로·세로·지뢰 수는 정수로 입력해 주세요.",ex);
        }
    }

    public String recordKey() { return "CUSTOM_"+rows+"_"+columns+"_"+mines; }
    public GameModel newGame() { return new GameModel(rows,columns,mines); }

    static boolean isRecordKey(String key) {
        String[] parts=key.split("_");
        if(parts.length!=4 || !parts[0].equals("CUSTOM")) return false;
        try { return parse(parts[1],parts[2],parts[3]).recordKey().equals(key); }
        catch(IllegalArgumentException ex) { return false; }
    }
}
