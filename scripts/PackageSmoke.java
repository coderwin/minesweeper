package com.coderwin.minesweeper;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Standalone packaged-runtime check; intentionally has no JUnit dependency. */
public final class PackageSmoke {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static Component named(Container root,String name) {
        for(Component component:root.getComponents()) {
            if(name.equals(component.getName())) return component;
            if(component instanceof Container child) {
                Component found=named(child,name);
                if(found!=null) return found;
            }
        }
        return null;
    }
    private static JButton cell(GamePanel panel,int row,int column) {
        return (JButton)named(panel,"cell-"+row+"-"+column);
    }

    public static void main(String[] args) throws Exception {
        BestTimesStore records=BestTimesStore.userDefault();
        if(args.length>0 && args[0].equals("restore")) {
            require(records.best(Difficulty.BEGINNER).orElse(-1)==1234,"Record must survive a second process");
            SwingUtilities.invokeAndWait(()->{
                GamePanel panel=new GamePanel(Difficulty.BEGINNER.newGame(),records,System::nanoTime);
                require(((JLabel)named(panel,"best-time")).getText().equals("최고 기록: 1.234초"),"Restored UI record");
            });
            System.out.println("Packaged runtime: cross-process record restoration passed");
            return;
        }
        JFrame[] window={null};
        try {
            SwingUtilities.invokeAndWait(()->{
                AtomicLong clock=new AtomicLong(0);
                GameModel game=new GameModel(9,9,10,new Random(42));
                GamePanel panel=new GamePanel(game,records,clock::get);
                window[0]=new JFrame("패키지 기능 검증");
                window[0].setContentPane(panel);
                window[0].pack(); window[0].setLocationRelativeTo(null); window[0].setVisible(true);
                JButton flagged=cell(panel,0,0);
                flagged.dispatchEvent(new MouseEvent(flagged,MouseEvent.MOUSE_PRESSED,System.currentTimeMillis(),
                        MouseEvent.BUTTON3_DOWN_MASK,5,5,1,true,MouseEvent.BUTTON3));
                require(flagged.getIcon()!=null && flagged.getAccessibleContext().getAccessibleName().contains("깃발"),"Flag input");
                ((JButton)named(panel,"restart")).doClick();
                require(game.state()==GameModel.State.READY && game.flagsRemaining()==10,"Restart");
                cell(panel,4,4).doClick();
                require(game.isRevealed(4,4),"Left click");
                clock.set(1_234_000_000L);
                for(int r=0;r<9;r++) for(int c=0;c<9;c++) if(!game.isMine(r,c)) cell(panel,r,c).doClick();
                require(game.state()==GameModel.State.WON,"Win");
                require(records.best(Difficulty.BEGINNER).orElse(-1)==1234,"Best time save");
                require(records.warning().isEmpty(),"Record write must succeed");
                JComboBox<?> selector=(JComboBox<?>)named(panel,"difficulty");
                for(Difficulty difficulty:Difficulty.values()) {
                    selector.setSelectedItem(difficulty);
                    require(cell(panel,difficulty.rows()-1,difficulty.columns()-1)!=null,"Board dimensions");
                    require(((JLabel)named(panel,"remaining")).getText().equals("남은 지뢰: "+difficulty.mines()),"Mine counter");
                    require(((JLabel)named(panel,"elapsed")).getText().equals("시간: 0초"),"Timer reset");
                    cell(panel,0,0).doClick();
                    ((JButton)named(panel,"restart")).doClick();
                    require(cell(panel,0,0).isEnabled() && cell(panel,0,0).getText().isEmpty(),"New board");
                }
            });
            System.out.println("Packaged runtime: input, restart, difficulty, win and persistence passed");
        } finally {
            SwingUtilities.invokeAndWait(()->{if(window[0]!=null) window[0].dispose();});
        }
    }
}
