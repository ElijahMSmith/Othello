import enums.Difficulty;
import enums.Mode;
import javax.swing.*;
import java.awt.*;

public class Runner extends JFrame {

    private Board board;
    private CardLayout cardLayout;

    public static void main(String[] args){
        //The supposed best way to start this
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Runner r = new Runner();
                r.setVisible(true);
            }
        });
    }

    private Runner(){
        //Sets up frame
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(885, 905));
        setTitle("Othello");
        //Starts up home screen panel and adds it to the layout
        HomeScreen home = new HomeScreen(this);
        cardLayout = new CardLayout();
        getContentPane().setLayout(cardLayout);
        getContentPane().add(home);
        validate();
        pack();
    }

    //When HomeScreen is ready to start game, calls this method, which starts up the board/settings panels
    public void startGame(Mode m, Difficulty d, int computerColor){
        board = new Board(this, m, d, computerColor);
        Settings settings = new Settings(this, m, d, computerColor);
        //Removes homescreen panel and adds board/settings to the layout (board on top, settings behind/inactive until later)
        getContentPane().removeAll();
        getContentPane().add(board);
        getContentPane().add(settings);
    }

    //Goes to the next panel in the layout, and because board calls this, only other panel is settings
    public void goToSettings(){
        cardLayout.next(getContentPane());
        board.setInFocus(false);
    }

    //Goes to next panel (board) from previous (settings) and applies all changes made
    public void returnToGame(Difficulty diff, boolean showBlack, boolean showWhite){
        board.setDifficulty(diff);
        board.setShowMovesBlack(showBlack);
        board.setShowMovesWhite(showWhite);

        cardLayout.next(getContentPane());
        board.setInFocus(true);
    }
}