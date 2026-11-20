package com.coderwin.minesweeper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Vector shapes in logical pixels; Swing's graphics transform handles display scaling. */
enum CellIcon implements Icon {
    FLAG, MINE;

    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }

    @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.translate(x,y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if(this==FLAG) {
                g.setColor(new Color(190,40,40));
                g.fillPolygon(new int[]{6,16,6},new int[]{2,6,10},3);
                g.setColor(new Color(35,45,55));
                g.drawLine(6,2,6,16);
                g.drawLine(3,17,13,17);
            } else {
                g.setColor(new Color(35,45,55));
                g.drawLine(10,1,10,19); g.drawLine(1,10,19,10);
                g.drawLine(4,4,16,16); g.drawLine(4,16,16,4);
                g.fillOval(4,4,12,12);
                g.setColor(Color.WHITE); g.fillOval(7,6,3,3);
            }
        } finally { g.dispose(); }
    }
}
