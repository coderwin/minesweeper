package com.coderwin.minesweeper;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in test that uses an actual desktop and mouse. */
@EnabledIfSystemProperty(named="desktop.tests",matches="true")
class DesktopTest {
    @Test void realMouseClicksRevealedNumber() throws Exception {
        JFrame[] frame={null};
        GameModel g=ChordTest.game();
        int[] p=ChordTest.boundary(g);
        ChordTest.flagMines(g,p);
        int before=ChordTest.revealed(g);
        Point[] point={null};
        Robot robot=new Robot(); robot.setAutoDelay(150);
        try {
            SwingUtilities.invokeAndWait(()->{
                frame[0]=new JFrame("주변 칸 열기 검증");
                GamePanel panel=new GamePanel(g);
                frame[0].setContentPane(panel);
                frame[0].setAlwaysOnTop(true);
                frame[0].pack(); frame[0].setLocation(40,40); frame[0].setVisible(true);
                JButton cell=GamePanelTest.cell(panel,p[0],p[1]);
                point[0]=cell.getLocationOnScreen();
                point[0].translate(cell.getWidth()/2,cell.getHeight()/2);
            });
            robot.delay(1200); robot.waitForIdle();
            activate(robot,frame[0]);
            moveTo(robot,point[0]);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.waitForIdle();
            SwingUtilities.invokeAndWait(()->assertTrue(ChordTest.revealed(g)>before));
        } finally {
            SwingUtilities.invokeAndWait(()->{if(frame[0]!=null) frame[0].dispose();});
        }
    }

    @Test void actualWindowMouseTimerRestartAndDifficulty() throws Exception {
        JFrame[] frame={null};
        GamePanel[] panel={null};
        Robot robot=new Robot();
        robot.setAutoDelay(150);
        try {
            SwingUtilities.invokeAndWait(()->{
                frame[0]=new JFrame("지뢰찾기 검증");
                panel[0]=new GamePanel();
                frame[0].setContentPane(panel[0]);
                frame[0].setAlwaysOnTop(true);
                frame[0].pack();
                frame[0].setLocation(40,40);
                frame[0].setVisible(true);
                frame[0].toFront();
            });
            robot.waitForIdle();
            robot.delay(1200);
            activate(robot,frame[0]);
            Point[] point={null};
            SwingUtilities.invokeAndWait(()->{
                JButton b=GamePanelTest.cell(panel[0],0,0);
                point[0]=b.getLocationOnScreen();
                point[0].translate(b.getWidth()/2,b.getHeight()/2);
            });
            moveTo(robot,point[0]);
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            robot.waitForIdle();
            SwingUtilities.invokeAndWait(()->assertEquals("F",GamePanelTest.cell(panel[0],0,0).getText()));
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.waitForIdle();
            Thread.sleep(1300);
            SwingUtilities.invokeAndWait(()->{
                assertFalse(GamePanelTest.cell(panel[0],0,0).isEnabled());
                assertNotEquals("시간: 0초",((JLabel)GamePanelTest.named(panel[0],"elapsed")).getText());
                ((JButton)GamePanelTest.named(panel[0],"restart")).doClick();
                assertTrue(GamePanelTest.cell(panel[0],0,0).isEnabled());
                assertEquals("시간: 0초",((JLabel)GamePanelTest.named(panel[0],"elapsed")).getText());
                JComboBox<?> selector=(JComboBox<?>)GamePanelTest.named(panel[0],"difficulty");
                for(Difficulty d:Difficulty.values()) {
                    selector.setSelectedItem(d);
                    assertNotNull(GamePanelTest.cell(panel[0],d.rows()-1,d.columns()-1));
                }
                selector.setSelectedItem(Difficulty.BEGINNER);
            });
            robot.waitForIdle();
            Files.createDirectories(Path.of("target"));
            java.awt.Rectangle[] bounds={null};
            SwingUtilities.invokeAndWait(()->bounds[0]=frame[0].getBounds());
            ImageIO.write(robot.createScreenCapture(bounds[0]),"png",Path.of("target/desktop-smoke.png").toFile());
        } finally {
            SwingUtilities.invokeAndWait(()->{if(frame[0]!=null) frame[0].dispose();});
        }
    }

    private static void activate(Robot robot,JFrame frame) throws Exception {
        Point[] title={null};
        SwingUtilities.invokeAndWait(()->{
            frame.toFront();
            title[0]=frame.getLocationOnScreen(); title[0].translate(80,12);
        });
        moveTo(robot,title[0]);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle(); robot.delay(300);
    }

    // Some Windows multi-monitor layouts offset Robot's absolute coordinates.
    private static void moveTo(Robot robot,Point target) {
        int x=target.x,y=target.y;
        for(int attempt=0;attempt<4;attempt++) {
            robot.mouseMove(x,y);
            Point actual=java.awt.MouseInfo.getPointerInfo().getLocation();
            if(actual.distance(target)<2) return;
            x+=target.x-actual.x; y+=target.y-actual.y;
        }
        assertTrue(java.awt.MouseInfo.getPointerInfo().getLocation().distance(target)<2,
                "Mouse must reach the test window before clicking");
    }
}
