package com.coderwin.minesweeper;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class MinesweeperApp {
    private MinesweeperApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("지뢰찾기");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel("지뢰찾기를 준비하고 있습니다.", SwingConstants.CENTER));
            frame.setSize(420, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
