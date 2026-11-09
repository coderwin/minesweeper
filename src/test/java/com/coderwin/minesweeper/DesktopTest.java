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
                frame[0].setLocationRelativeTo(null);
                frame[0].setVisible(true);
                frame[0].toFront();
            });
            robot.waitForIdle();
            robot.delay(1200);
            Point[] point={null};
            SwingUtilities.invokeAndWait(()->{
                JButton b=GamePanelTest.cell(panel[0],0,0);
                point[0]=b.getLocationOnScreen();
                point[0].translate(b.getWidth()/2,b.getHeight()/2);
            });
            robot.mouseMove(point[0].x,point[0].y);
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
}
