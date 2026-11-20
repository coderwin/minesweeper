package com.coderwin.minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class GamePanel extends JPanel {
    private GameModel game;
    static final String CUSTOM_CHOICE="사용자 지정…";
    private final JComboBox<Object> difficulty = new JComboBox<>(Difficulty.values());
    private Difficulty activeDifficulty=Difficulty.BEGINNER;
    private BoardSettings customSettings;
    private boolean changingSelection;
    private final java.util.function.Supplier<BoardSettings> customPrompt;
    private final JPanel board = new JPanel();
    private final JScrollPane boardScroll = new JScrollPane(board);
    private static final Color[] NUMBER_COLORS = {
        Color.DARK_GRAY, new Color(35, 80, 185), new Color(25, 120, 65),
        new Color(190, 40, 40), new Color(90, 45, 150), new Color(140, 60, 20),
        new Color(15, 120, 135), Color.BLACK, Color.GRAY
    };
    private final JLabel remaining = new JLabel();
    private final JLabel elapsed = new JLabel("시간: 0초");
    private final JLabel status = new JLabel();
    private final JLabel bestTime = new JLabel();
    private final JLabel recordNotice = new JLabel();
    private final BestTimesStore records;
    private final java.util.function.LongSupplier clock;
    private final Timer timer;
    private JButton[][] cells;
    private long startedAt;

    public GamePanel() {
        this(Difficulty.BEGINNER.newGame(), BestTimesStore.userDefault(), System::nanoTime);
    }

    GamePanel(GameModel initialGame) {
        this(initialGame, BestTimesStore.inMemory(), System::nanoTime);
    }

    GamePanel(GameModel initialGame, BestTimesStore records, java.util.function.LongSupplier clock) {
        this(initialGame,records,clock,null);
    }

    GamePanel(GameModel initialGame, BestTimesStore records, java.util.function.LongSupplier clock,
            java.util.function.Supplier<BoardSettings> customPrompt) {
        super(new BorderLayout(0, 12));
        game = initialGame;
        this.records = records;
        this.clock = clock;
        this.customPrompt = customPrompt == null ? this::promptCustomSettings : customPrompt;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        JButton restart = new JButton("새 게임");
        restart.setName("restart");
        restart.addActionListener(e -> newGame());
        remaining.setName("remaining");
        elapsed.setName("elapsed");
        status.setName("status");
        toolbar.add(remaining);
        toolbar.add(elapsed);
        difficulty.setName("difficulty");
        difficulty.addItem(CUSTOM_CHOICE);
        difficulty.getAccessibleContext().setAccessibleName("난이도");
        difficulty.addActionListener(e -> changeDifficulty());
        toolbar.add(difficulty);
        toolbar.add(restart);
        add(toolbar, BorderLayout.NORTH);
        boardScroll.setBorder(BorderFactory.createEmptyBorder());
        boardScroll.setName("board-scroll");
        add(boardScroll, BorderLayout.CENTER);
        bestTime.setName("best-time");
        recordNotice.setName("record-notice");
        JPanel footer = new JPanel(new GridLayout(0, 1, 0, 4));
        footer.add(status);
        footer.add(bestTime);
        footer.add(recordNotice);
        add(footer, BorderLayout.SOUTH);
        timer = new Timer(200, e -> updateTime());
        buildBoard();
        render();
    }

    private void buildBoard() {
        board.removeAll();
        board.setLayout(new GridLayout(game.rows(), game.columns(), 2, 2));
        cells = new JButton[game.rows()][game.columns()];
        for (int r = 0; r < game.rows(); r++) for (int c = 0; c < game.columns(); c++) {
            final int row = r;
            final int column = c;
            JButton cell = new CellButton();
            cell.setName("cell-" + r + "-" + c);
            cell.setPreferredSize(new Dimension(38, 38));
            cell.setMargin(new java.awt.Insets(0, 0, 0, 0));
            cell.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            cell.addActionListener(e -> openCell(row, column));
            cell.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke("F"),"toggleFlag");
            cell.getActionMap().put("toggleFlag",new javax.swing.AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                    toggleFlag(row,column);
                }
            });
            cell.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if(cell.isEnabled()) cell.requestFocusInWindow();
                    if (SwingUtilities.isRightMouseButton(e)) {
                        toggleFlag(row,column);
                    }
                }
            });
            cells[r][c] = cell;
            board.add(cell);
        }
        Dimension screen=java.awt.GraphicsEnvironment.isHeadless() ? new Dimension(1280,900)
                : java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(Math.max(280,screen.width-80),Math.max(510,game.columns()*40));
        int height = Math.min(Math.max(160,screen.height-220),game.rows()*40);
        boardScroll.setPreferredSize(new Dimension(width, height));
        board.revalidate();
        board.repaint();
    }

    private void toggleFlag(int row,int column) {
        game.toggleFlag(row,column);
        render();
    }

    private void changeDifficulty() {
        if(changingSelection) return;
        Object choice=difficulty.getSelectedItem();
        if(!(choice instanceof Difficulty selected)) {
            BoardSettings settings=customPrompt.get();
            if(settings==null) {
                selectQuietly(customSettings==null ? activeDifficulty : CUSTOM_CHOICE);
            } else applyCustomSettings(settings);
            return;
        }
        activeDifficulty=selected;
        customSettings=null;
        game = selected.newGame();
        rebuildGame();
    }

    void applyCustomSettings(BoardSettings settings) {
        java.util.Objects.requireNonNull(settings);
        GameModel next=settings.newGame();
        customSettings=settings;
        game=next;
        selectQuietly(CUSTOM_CHOICE);
        rebuildGame();
    }

    private void selectQuietly(Object choice) {
        changingSelection=true;
        try {difficulty.setSelectedItem(choice);} finally {changingSelection=false;}
    }

    private void rebuildGame() {
        buildBoard();
        newGame();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.pack();
            window.setLocationRelativeTo(null);
        }
    }

    private BoardSettings promptCustomSettings() {
        javax.swing.JTextField columns=new javax.swing.JTextField(Integer.toString(game.columns()));
        javax.swing.JTextField rows=new javax.swing.JTextField(Integer.toString(game.rows()));
        javax.swing.JTextField mines=new javax.swing.JTextField(Integer.toString(game.mineCount()));
        columns.setName("custom-columns"); rows.setName("custom-rows"); mines.setName("custom-mines");
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        addField(form,"가로 (3~40)",columns);
        addField(form,"세로 (3~30)",rows);
        addField(form,"지뢰 (1~전체 칸 수 - 9)",mines);
        while(true) {
            int result=javax.swing.JOptionPane.showOptionDialog(this,form,"사용자 지정 난이도",
                    javax.swing.JOptionPane.OK_CANCEL_OPTION,javax.swing.JOptionPane.PLAIN_MESSAGE,
                    null,new Object[]{"적용","취소"},"적용");
            if(result!=0) return null;
            try {return BoardSettings.parse(rows.getText(),columns.getText(),mines.getText());}
            catch(IllegalArgumentException ex) {
                javax.swing.JOptionPane.showMessageDialog(this,ex.getMessage(),"입력 확인",javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static void addField(JPanel form,String text,javax.swing.JTextField field) {
        JLabel label=new JLabel(text); label.setLabelFor(field); form.add(label); form.add(field);
    }

    private String recordKey() {
        return customSettings==null ? activeDifficulty.name() : customSettings.recordKey();
    }

    private void openCell(int row, int column) {
        GameModel.State before = game.state();
        if (game.isRevealed(row, column)) game.chord(row, column);
        else game.reveal(row, column);
        if (before == GameModel.State.READY && game.state() != GameModel.State.READY) {
            startedAt = clock.getAsLong();
            timer.start();
        }
        if (game.isFinished()) {
            updateTime();
            timer.stop();
            if (before != GameModel.State.WON && game.state() == GameModel.State.WON) {
                records.record(recordKey(), elapsedMillis());
            }
        }
        render();
    }

    private void newGame() {
        timer.stop();
        game.reset();
        startedAt = 0;
        elapsed.setText("시간: 0초");
        render();
    }

    private void updateTime() {
        long seconds = elapsedMillis() / 1000;
        elapsed.setText("시간: " + seconds + "초");
    }

    private long elapsedMillis() {
        return game.state() == GameModel.State.READY ? 0 : Math.max(0, (clock.getAsLong() - startedAt) / 1_000_000L);
    }

    private void render() {
        remaining.setText("남은 지뢰: " + game.flagsRemaining());
        java.util.OptionalLong best = records.best(recordKey());
        bestTime.setText("최고 기록: " + (best.isPresent()
                ? String.format(java.util.Locale.ROOT, "%.3f초", best.getAsLong() / 1000.0) : "없음"));
        recordNotice.setText(records.warning());
        status.setText(switch (game.state()) {
            case READY -> "왼쪽 클릭: 열기 · 오른쪽 클릭 / F 키: 깃발";
            case PLAYING -> "숫자 클릭: 주변 칸 열기 · 오른쪽 클릭 / F 키: 깃발";
            case WON -> "승리! 모든 안전한 칸을 열었습니다.";
            case LOST -> "패배! 지뢰를 밟았습니다. 새 게임으로 도전하세요.";
        });
        for (int r = 0; r < game.rows(); r++) for (int c = 0; c < game.columns(); c++) {
            JButton cell = cells[r][c];
            boolean open = game.isRevealed(r, c);
            boolean showMine = game.isFinished() && game.isMine(r, c);
            String text = !showMine && !game.isFlagged(r,c) && open && game.adjacentMines(r,c)>0
                    ? Integer.toString(game.adjacentMines(r,c)) : "";
            cell.setText(text);
            javax.swing.Icon icon=showMine ? CellIcon.MINE : game.isFlagged(r,c) ? CellIcon.FLAG : null;
            cell.setIcon(icon);
            cell.setDisabledIcon(icon);
            cell.setForeground(showMine ? new Color(155, 20, 20) : game.isFlagged(r, c)
                    ? new Color(160, 65, 10) : NUMBER_COLORS[game.adjacentMines(r, c)]);
            cell.setBackground(showMine ? new Color(255, 180, 180) : open ? new Color(230, 235, 240) : new Color(190, 205, 220));
            cell.setEnabled(!game.isFinished() && (!open || game.adjacentMines(r, c) > 0));
            String description = showMine ? "지뢰" : game.isFlagged(r, c) ? "깃발" : open ? "열림, 주변 지뢰 " + game.adjacentMines(r, c) : "닫힘";
            cell.getAccessibleContext().setAccessibleName((r + 1) + "행 " + (c + 1) + "열 " + description);
            cell.setToolTipText(description + " · F 키: 깃발 표시/해제");
        }
    }

    @Override public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    /** Preserve number colors when revealed buttons are disabled. */
    private static final class CellButton extends JButton {
        CellButton() {
            setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override protected void paintText(java.awt.Graphics g, javax.swing.AbstractButton button,
                        java.awt.Rectangle textRect, String text) {
                    g.setColor(button.getForeground());
                    javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, -1,
                            textRect.x, textRect.y + g.getFontMetrics().getAscent());
                }
            });
        }
    }
}
