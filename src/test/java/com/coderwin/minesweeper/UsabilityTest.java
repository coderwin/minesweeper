package com.coderwin.minesweeper;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsabilityTest {
    static void pressF(JButton cell) {
        Object binding=cell.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("F"));
        assertNotNull(binding);
        cell.getActionMap().get(binding).actionPerformed(new ActionEvent(cell,ActionEvent.ACTION_PERFORMED,"F"));
    }

    @Test void keyboardFlagAndAccessibilityFollowState() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GameModel game=ChordTest.game();
            GamePanel panel=new GamePanel(game);
            JButton closed=null;
            for(int r=0;r<9 && closed==null;r++) for(int c=0;c<9 && closed==null;c++)
                if(!game.isRevealed(r,c)) closed=GamePanelTest.cell(panel,r,c);
            assertNotNull(closed);
            pressF(closed);
            assertSame(CellIcon.FLAG,closed.getIcon());
            assertTrue(closed.getAccessibleContext().getAccessibleName().contains("깃발"));
            closed.doClick(); assertSame(CellIcon.FLAG,closed.getIcon());
            pressF(closed); assertNull(closed.getIcon());
            int remaining=game.flagsRemaining(); pressF(GamePanelTest.cell(panel,4,4));
            assertEquals(remaining,game.flagsRemaining());
            for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(game.isMine(r,c)) GamePanelTest.cell(panel,r,c).doClick();
            pressF(closed); assertEquals(remaining,game.flagsRemaining());
            assertEquals(GameModel.State.LOST,game.state());
        });
    }

    @Test void vectorIconsPaintAtMultipleScales() {
        for(CellIcon icon:CellIcon.values()) for(int scale:new int[]{1,2,3}) {
            BufferedImage image=new BufferedImage(20*scale,20*scale,BufferedImage.TYPE_INT_ARGB);
            var graphics=image.createGraphics();
            try {graphics.scale(scale,scale);icon.paintIcon(null,graphics,0,0);} finally {graphics.dispose();}
            int painted=0;
            for(int y=0;y<image.getHeight();y++) for(int x=0;x<image.getWidth();x++) if((image.getRGB(x,y)>>>24)!=0) painted++;
            assertTrue(painted>20*scale*scale);
        }
    }
}
