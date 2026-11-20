package com.coderwin.minesweeper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CustomDifficultyTest {
    @TempDir Path directory;

    @Test void limitsAndInputFormatsReserveSafeArea() {
        assertEquals(new BoardSettings(3,4,3),BoardSettings.parse(" 3 ","4","3"));
        assertEquals(1191,new BoardSettings(30,40,1191).mines());
        for(int[] values:new int[][]{{2,9,1},{31,9,1},{9,2,1},{9,41,1},{9,9,0},{9,9,73},{3,3,1}})
            assertThrows(IllegalArgumentException.class,()->new BoardSettings(values[0],values[1],values[2]));
        for(String value:new String[]{"","abc","3.5","99999999999999999999"})
            assertThrows(IllegalArgumentException.class,()->BoardSettings.parse(value,"9","10"));
    }

    @Test void customModelsHaveExactMinesAndSafeFirstClicks() {
        for(BoardSettings settings:new BoardSettings[]{new BoardSettings(3,4,3),new BoardSettings(30,40,1191),new BoardSettings(12,20,50)}) {
            for(int[] first:new int[][]{{0,0},{settings.rows()/2,settings.columns()/2},{settings.rows()-1,settings.columns()-1}}) {
                GameModel game=settings.newGame(); game.reveal(first[0],first[1]); int count=0;
                for(int r=0;r<game.rows();r++) for(int c=0;c<game.columns();c++) if(game.isMine(r,c)) count++;
                assertEquals(settings.mines(),count);
                assertEquals(0,game.adjacentMines(first[0],first[1]));
                assertFalse(game.isMine(first[0],first[1]));
            }
        }
    }

    @Test void cancelAndInvalidSettingsPreserveCurrentGame() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GameModel game=new GameModel(9,9,10,new Random(42));
            GamePanel panel=new GamePanel(game,BestTimesStore.inMemory(),System::nanoTime,()->null);
            GamePanelTest.rightClick(GamePanelTest.cell(panel,0,0));
            GamePanelTest.cell(panel,4,4).doClick();
            JComboBox<?> selector=(JComboBox<?>)GamePanelTest.named(panel,"difficulty");
            selector.setSelectedItem(GamePanel.CUSTOM_CHOICE);
            assertSame(Difficulty.BEGINNER,selector.getSelectedItem());
            assertTrue(game.isFlagged(0,0)); assertEquals(GameModel.State.PLAYING,game.state());
            assertThrows(IllegalArgumentException.class,()->panel.applyCustomSettings(new BoardSettings(31,9,10)));
            assertTrue(game.isFlagged(0,0));
            assertNotNull(GamePanelTest.cell(panel,8,8));
            ((JButton)GamePanelTest.named(panel,"restart")).doClick();
        });
    }

    @Test void applyLargeBoardAndRestartResetOnlyGameState() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            BoardSettings settings=new BoardSettings(30,40,200);
            GamePanel panel=new GamePanel(Difficulty.BEGINNER.newGame(),BestTimesStore.inMemory(),System::nanoTime,()->settings);
            ((JComboBox<?>)GamePanelTest.named(panel,"difficulty")).setSelectedItem(GamePanel.CUSTOM_CHOICE);
            assertNotNull(GamePanelTest.cell(panel,29,39));
            assertNull(GamePanelTest.named(panel,"cell-30-0"));
            assertEquals("남은 지뢰: 200",((JLabel)GamePanelTest.named(panel,"remaining")).getText());
            GamePanelTest.rightClick(GamePanelTest.cell(panel,0,0));
            ((JButton)GamePanelTest.named(panel,"restart")).doClick();
            assertNotNull(GamePanelTest.cell(panel,29,39));
            assertNull(GamePanelTest.cell(panel,0,0).getIcon());
            assertEquals("시간: 0초",((JLabel)GamePanelTest.named(panel,"elapsed")).getText());
        });
    }

    @Test void customRecordsAreSeparateAndBackwardCompatible() throws Exception {
        Path file=directory.resolve("records.properties");
        Files.writeString(file,"BEGINNER=5000\nCUSTOM_invalid=123\n");
        BoardSettings a=new BoardSettings(3,4,3),b=new BoardSettings(12,20,50);
        BestTimesStore store=new BestTimesStore(file);
        store.record(a.recordKey(),1200); store.record(b.recordKey(),2200);
        assertFalse(store.record(a.recordKey(),1300));
        BestTimesStore restored=new BestTimesStore(file);
        assertEquals(5000,restored.best(Difficulty.BEGINNER).orElseThrow());
        assertEquals(1200,restored.best(a.recordKey()).orElseThrow());
        assertEquals(2200,restored.best(b.recordKey()).orElseThrow());
        assertThrows(IllegalArgumentException.class,()->store.record("CUSTOM_99_99_1",1));
    }

    @Test void customWinStoresAndRestoresItsOwnRecord() throws Exception {
        Path file=directory.resolve("ui.properties");
        SwingUtilities.invokeAndWait(()->{
            BoardSettings settings=new BoardSettings(3,4,3);
            BestTimesStore store=new BestTimesStore(file);
            GamePanel panel=new GamePanel(Difficulty.BEGINNER.newGame(),store,()->0L,()->settings);
            ((JComboBox<?>)GamePanelTest.named(panel,"difficulty")).setSelectedItem(GamePanel.CUSTOM_CHOICE);
            GamePanelTest.cell(panel,1,1).doClick();
            assertEquals(0,store.best(settings.recordKey()).orElseThrow());
            assertTrue(store.best(Difficulty.BEGINNER).isEmpty());
            GamePanel restored=new GamePanel(Difficulty.BEGINNER.newGame(),new BestTimesStore(file),()->0L,()->settings);
            restored.applyCustomSettings(settings);
            assertEquals("최고 기록: 0.000초",((JLabel)GamePanelTest.named(restored,"best-time")).getText());
        });
    }
}
