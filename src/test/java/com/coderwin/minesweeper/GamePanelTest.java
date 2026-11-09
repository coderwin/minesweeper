package com.coderwin.minesweeper;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GamePanelTest {
    static Component named(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container container) {
                Component found=named(container,name);
                if(found!=null) return found;
            }
        }
        return null;
    }

    static JButton cell(GamePanel p,int r,int c) { return (JButton)named(p,"cell-"+r+"-"+c); }
    static void rightClick(JButton b) {
        b.dispatchEvent(new MouseEvent(b,MouseEvent.MOUSE_PRESSED,System.currentTimeMillis(),
                MouseEvent.BUTTON3_DOWN_MASK,5,5,1,true,MouseEvent.BUTTON3));
    }

    @Test void clickFlagAndRestart() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GameModel g=new GameModel(9,9,10,new Random(42));
            GamePanel p=new GamePanel(g);
            rightClick(cell(p,0,0));
            assertEquals("F",cell(p,0,0).getText());
            assertEquals("남은 지뢰: 9",((JLabel)named(p,"remaining")).getText());
            cell(p,0,0).doClick();
            assertEquals(GameModel.State.READY,g.state());
            rightClick(cell(p,0,0));
            cell(p,0,0).doClick();
            assertTrue(g.isRevealed(0,0));
            ((JButton)named(p,"restart")).doClick();
            assertEquals(GameModel.State.READY,g.state());
            assertEquals("시간: 0초",((JLabel)named(p,"elapsed")).getText());
            assertEquals("남은 지뢰: 10",((JLabel)named(p,"remaining")).getText());
        });
    }

    @Test void difficultyChangesRebuildAndReset() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GamePanel p=new GamePanel();
            JComboBox<?> selector=(JComboBox<?>)named(p,"difficulty");
            for (Difficulty d:Difficulty.values()) {
                cell(p,0,0).doClick();
                selector.setSelectedItem(d);
                assertNotNull(cell(p,d.rows()-1,d.columns()-1));
                assertNull(named(p,"cell-"+d.rows()+"-0"));
                assertNull(named(p,"cell-0-"+d.columns()));
                assertEquals("",cell(p,0,0).getText());
                assertTrue(cell(p,0,0).isEnabled());
                assertEquals("남은 지뢰: "+d.mines(),((JLabel)named(p,"remaining")).getText());
                assertEquals("시간: 0초",((JLabel)named(p,"elapsed")).getText());
            }
        });
    }

    @Test void winAndLossShowResultAndDisableBoard() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            for(boolean win:new boolean[]{true,false}) {
                GameModel g=new GameModel(9,9,10,new Random(42));
                GamePanel p=new GamePanel(g);
                cell(p,4,4).doClick();
                for(int r=0;r<9;r++) for(int c=0;c<9;c++) {
                    if(win && !g.isMine(r,c) || !win && g.isMine(r,c)) cell(p,r,c).doClick();
                }
                assertEquals(win?GameModel.State.WON:GameModel.State.LOST,g.state());
                assertTrue(((JLabel)named(p,"status")).getText().startsWith(win?"승리":"패배"));
                for(int r=0;r<9;r++) for(int c=0;c<9;c++) assertFalse(cell(p,r,c).isEnabled());
                int remaining=g.flagsRemaining();
                rightClick(cell(p,0,0));
                assertEquals(remaining,g.flagsRemaining());
            }
        });
    }
}
