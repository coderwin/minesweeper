package com.coderwin.minesweeper;

import java.util.Random;
import java.util.function.BiConsumer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChordTest {
    static GameModel game() {
        GameModel g=new GameModel(9,9,10,new Random(42));
        g.reveal(4,4);
        return g;
    }
    static void neighbors(GameModel g,int[] p,BiConsumer<Integer,Integer> action) {
        for(int r=Math.max(0,p[0]-1);r<=Math.min(g.rows()-1,p[0]+1);r++)
            for(int c=Math.max(0,p[1]-1);c<=Math.min(g.columns()-1,p[1]+1);c++)
                if(r!=p[0] || c!=p[1]) action.accept(r,c);
    }
    static int[] boundary(GameModel g) {
        for(int r=0;r<g.rows();r++) for(int c=0;c<g.columns();c++) {
            if(!g.isRevealed(r,c) || g.adjacentMines(r,c)==0) continue;
            boolean[] safe={false};
            int[] p={r,c};
            neighbors(g,p,(nr,nc)->{if(!g.isRevealed(nr,nc) && !g.isMine(nr,nc)) safe[0]=true;});
            if(safe[0]) return p;
        }
        throw new AssertionError("No boundary");
    }
    static void flagMines(GameModel g,int[] p) {
        neighbors(g,p,(r,c)->{if(g.isMine(r,c)) g.toggleFlag(r,c);});
    }
    static int revealed(GameModel g) {
        int count=0;
        for(int r=0;r<g.rows();r++) for(int c=0;c<g.columns();c++) if(g.isRevealed(r,c)) count++;
        return count;
    }

    @Test void matchingFlagsOpenNeighbors() {
        GameModel g=game(); int[] p=boundary(g); flagMines(g,p);
        int before=revealed(g);
        g.chord(p[0],p[1]);
        assertTrue(revealed(g)>before);
        assertNotEquals(GameModel.State.LOST,g.state());
        neighbors(g,p,(r,c)->{if(!g.isMine(r,c)) assertTrue(g.isRevealed(r,c));});
    }
    @Test void tooFewAndTooManyFlagsDoNothing() {
        GameModel g=game(); int[] p=boundary(g); int before=revealed(g);
        g.chord(p[0],p[1]); assertEquals(before,revealed(g));
        flagMines(g,p);
        boolean[] extra={false};
        neighbors(g,p,(r,c)->{if(!extra[0] && !g.isMine(r,c) && !g.isRevealed(r,c)) {
            g.toggleFlag(r,c); extra[0]=true;
        }});
        assertTrue(extra[0]); g.chord(p[0],p[1]); assertEquals(before,revealed(g));
    }
    @Test void misplacedFlagLosesAndLocksInput() {
        GameModel g=game(); int[] p=boundary(g); flagMines(g,p);
        boolean[] removed={false}, added={false};
        neighbors(g,p,(r,c)->{
            if(!removed[0] && g.isMine(r,c)) {g.toggleFlag(r,c);removed[0]=true;}
            if(!added[0] && !g.isMine(r,c) && !g.isRevealed(r,c)) {g.toggleFlag(r,c);added[0]=true;}
        });
        g.chord(p[0],p[1]); assertEquals(GameModel.State.LOST,g.state());
        int after=revealed(g); g.chord(p[0],p[1]); assertEquals(after,revealed(g));
    }
    @Test void chordCanWinAndDoesNotChangeFinishedGame() {
        GameModel g=game(); int[] p=boundary(g); flagMines(g,p);
        int[][] last={null};
        neighbors(g,p,(r,c)->{if(!g.isMine(r,c) && !g.isRevealed(r,c) && last[0]==null) last[0]=new int[]{r,c};});
        int[] safe=last[0]; g.toggleFlag(safe[0],safe[1]);
        for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(!g.isMine(r,c)) g.reveal(r,c);
        assertEquals(GameModel.State.PLAYING,g.state());
        g.toggleFlag(safe[0],safe[1]); g.chord(p[0],p[1]);
        assertEquals(GameModel.State.WON,g.state());
        int count=revealed(g); g.chord(p[0],p[1]); assertEquals(count,revealed(g));
    }
    @Test void chordExpandsEmptyRegion() {
        boolean expanded=false;
        for(int seed=0;seed<100 && !expanded;seed++) {
            GameModel g=new GameModel(9,9,10,new Random(seed)); g.reveal(4,4);
            for(int r=0;r<9 && !expanded;r++) for(int c=0;c<9 && !expanded;c++) {
                if(!g.isRevealed(r,c) || g.adjacentMines(r,c)==0) continue;
                int[] p={r,c}; boolean[] zero={false};
                neighbors(g,p,(nr,nc)->{if(!g.isRevealed(nr,nc) && !g.isMine(nr,nc) && g.adjacentMines(nr,nc)==0) zero[0]=true;});
                if(!zero[0]) continue;
                flagMines(g,p); g.chord(r,c);
                neighbors(g,p,(nr,nc)->{
                    if(!g.isMine(nr,nc) && g.adjacentMines(nr,nc)==0)
                        neighbors(g,new int[]{nr,nc},(ar,ac)->{if(!g.isMine(ar,ac)) assertTrue(g.isRevealed(ar,ac));});
                });
                expanded=true;
            }
        }
        assertTrue(expanded,"Must exercise a newly opened zero region");
    }
    @Test void clickingRevealedNumberUsesChordInSwing() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            GameModel g=game(); int[] p=boundary(g); flagMines(g,p);
            GamePanel panel=new GamePanel(g); int before=revealed(g);
            assertTrue(GamePanelTest.cell(panel,p[0],p[1]).isEnabled());
            GamePanelTest.cell(panel,p[0],p[1]).doClick();
            assertTrue(revealed(g)>before);
        });
    }
}
