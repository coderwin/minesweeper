package com.coderwin.minesweeper;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class MinesweeperApp {
    private MinesweeperApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("지뢰찾기");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GamePanel());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
