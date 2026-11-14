package com.coderwin.minesweeper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class BestTimesStoreTest {
    @TempDir Path directory;

    @Test void storesOnlyBetterTimesAndRestoresEachDifficulty() {
        Path file=directory.resolve("nested/best-times.properties");
        BestTimesStore store=new BestTimesStore(file);
        assertTrue(store.best(Difficulty.BEGINNER).isEmpty());
        assertEquals("",store.warning());
        assertTrue(store.record(Difficulty.BEGINNER,1200));
        assertFalse(store.record(Difficulty.BEGINNER,1500));
        assertFalse(store.record(Difficulty.BEGINNER,1200));
        assertTrue(store.record(Difficulty.BEGINNER,1000));
        store.record(Difficulty.INTERMEDIATE,2000);
        store.record(Difficulty.EXPERT,3000);
        BestTimesStore restored=new BestTimesStore(file);
        assertEquals(1000,restored.best(Difficulty.BEGINNER).orElseThrow());
        assertEquals(2000,restored.best(Difficulty.INTERMEDIATE).orElseThrow());
        assertEquals(3000,restored.best(Difficulty.EXPERT).orElseThrow());
        assertThrows(IllegalArgumentException.class,()->store.record(Difficulty.EXPERT,-1));
    }

    @Test void ignoresInvalidValuesButKeepsHealthyRecords() throws Exception {
        Path file=directory.resolve("records.properties");
        Files.writeString(file,"BEGINNER=invalid\nINTERMEDIATE=1234\nEXPERT=-5\n");
        BestTimesStore store=new BestTimesStore(file);
        assertTrue(store.best(Difficulty.BEGINNER).isEmpty());
        assertEquals(1234,store.best(Difficulty.INTERMEDIATE).orElseThrow());
        assertTrue(store.best(Difficulty.EXPERT).isEmpty());
        assertFalse(store.warning().isEmpty());
        store.record(Difficulty.BEGINNER,42);
        assertEquals(42,new BestTimesStore(file).best(Difficulty.BEGINNER).orElseThrow());
    }

    @Test void malformedPropertiesAndReadFailuresDoNotCrash() throws Exception {
        Path file=directory.resolve("invalid.properties");
        Files.writeString(file,"BEGINNER=\\uZZZZ");
        assertFalse(new BestTimesStore(file).warning().isEmpty());
        assertFalse(new BestTimesStore(directory).warning().isEmpty());
    }

    @Test void failedWriteRetainsSessionTimeAndExistingFile() throws Exception {
        Path parent=directory.resolve("blocked"); Files.writeString(parent,"keep");
        BestTimesStore store=new BestTimesStore(parent.resolve("records.properties"));
        assertTrue(store.record(Difficulty.BEGINNER,500));
        assertEquals(500,store.best(Difficulty.BEGINNER).orElseThrow());
        assertFalse(store.warning().isEmpty());
        assertEquals("keep",Files.readString(parent));
    }

    @Test void swingRecordsOnlyWinsAndRestoresDisplay() throws Exception {
        Path file=directory.resolve("ui.properties");
        SwingUtilities.invokeAndWait(()->{
            AtomicLong nanos=new AtomicLong(0);
            GameModel game=new GameModel(9,9,10,new Random(42));
            BestTimesStore store=new BestTimesStore(file);
            GamePanel panel=new GamePanel(game,store,nanos::get);
            GamePanelTest.cell(panel,4,4).doClick();
            nanos.set(2_345_000_000L);
            for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(!game.isMine(r,c)) GamePanelTest.cell(panel,r,c).doClick();
            assertEquals(GameModel.State.WON,game.state());
            assertEquals(2345,store.best(Difficulty.BEGINNER).orElseThrow());
            GamePanel restored=new GamePanel(Difficulty.BEGINNER.newGame(),new BestTimesStore(file),nanos::get);
            assertEquals("최고 기록: 2.345초",((JLabel)GamePanelTest.named(restored,"best-time")).getText());
            GameModel losing=new GameModel(9,9,10,new Random(42));
            GamePanel loser=new GamePanel(losing,store,nanos::get);
            GamePanelTest.cell(loser,4,4).doClick();
            for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(losing.isMine(r,c)) GamePanelTest.cell(loser,r,c).doClick();
            assertEquals(GameModel.State.LOST,losing.state());
            assertEquals(2345,new BestTimesStore(file).best(Difficulty.BEGINNER).orElseThrow());
        });
    }
}
