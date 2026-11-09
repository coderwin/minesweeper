package com.coderwin.minesweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class GamePanel extends JPanel {
    private GameModel game = new GameModel(9, 9, 10);
    private final JPanel board = new JPanel();
    private final JLabel remaining = new JLabel();
    private final JLabel elapsed = new JLabel("시간: 0초");
    private final JLabel status = new JLabel();
    private final Timer timer;
    private JButton[][] cells;
    private long startedAt;

    public GamePanel() {
        super(new BorderLayout(0, 12));
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
        toolbar.add(restart);
        add(toolbar, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
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
            JButton cell = new JButton();
            cell.setName("cell-" + r + "-" + c);
            cell.setPreferredSize(new Dimension(38, 38));
            cell.setMargin(new java.awt.Insets(0, 0, 0, 0));
            cell.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            cell.addActionListener(e -> openCell(row, column));
            cell.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        game.toggleFlag(row, column);
                        render();
                    }
                }
            });
            cells[r][c] = cell;
            board.add(cell);
        }
        board.revalidate();
        board.repaint();
    }

    private void openCell(int row, int column) {
        GameModel.State before = game.state();
        game.reveal(row, column);
        if (before == GameModel.State.READY && game.state() != GameModel.State.READY) {
            startedAt = System.nanoTime();
            timer.start();
        }
        if (game.isFinished()) {
            updateTime();
            timer.stop();
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
        long seconds = startedAt == 0 ? 0 : (System.nanoTime() - startedAt) / 1_000_000_000L;
        elapsed.setText("시간: " + seconds + "초");
    }

    private void render() {
        remaining.setText("남은 지뢰: " + game.flagsRemaining());
        status.setText(switch (game.state()) {
            case READY -> "왼쪽 클릭: 열기 · 오른쪽 클릭: 깃발";
            case PLAYING -> "지뢰가 없는 모든 칸을 열어 보세요.";
            case WON -> "승리! 모든 안전한 칸을 열었습니다.";
            case LOST -> "패배! 지뢰를 밟았습니다. 새 게임으로 도전하세요.";
        });
        for (int r = 0; r < game.rows(); r++) for (int c = 0; c < game.columns(); c++) {
            JButton cell = cells[r][c];
            boolean open = game.isRevealed(r, c);
            boolean showMine = game.isFinished() && game.isMine(r, c);
            String text = showMine ? "*" : game.isFlagged(r, c) ? "F"
                    : open && game.adjacentMines(r, c) > 0 ? Integer.toString(game.adjacentMines(r, c)) : "";
            cell.setText(text);
            cell.setBackground(showMine ? new Color(255, 180, 180) : open ? new Color(230, 235, 240) : new Color(190, 205, 220));
            cell.setEnabled(!game.isFinished() && !open);
            String description = showMine ? "지뢰" : game.isFlagged(r, c) ? "깃발" : open ? "열림, 주변 지뢰 " + game.adjacentMines(r, c) : "닫힘";
            cell.getAccessibleContext().setAccessibleName((r + 1) + "행 " + (c + 1) + "열 " + description);
        }
    }

    @Override public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }
}
