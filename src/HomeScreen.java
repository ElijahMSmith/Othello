import enums.Difficulty;
import enums.Mode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeScreen extends JPanel {

    private Runner runner;
    private Difficulty diff = Difficulty.NONE; //Default: No computer

    public HomeScreen(Runner runner) {
        this.runner = runner;
        //Sets up homescreen panel
        setBackground(Color.WHITE);
        setLayout(null);
        setFocusable(true);
        setBackground(new Color(181, 255, 191));

        //I realize these widths/heights are excessive, but it doesn't change anything visually (it's extra safe)
        //Long list of components added to the home panel
        JLabel welcome1 = new JLabel("Welcome to the Game of Othello!");
        welcome1.setBounds(168, 90, 600, 200);
        welcome1.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        add(welcome1);

        JLabel welcome2 = new JLabel("How would you like to play?");
        welcome2.setBounds(207, 190, 600, 200);
        welcome2.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        add(welcome2);

        JButton buttonpvp = new JButton("Player v Player");
        buttonpvp.setBounds(235, 390, 400, 100);
        buttonpvp.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonpvp.setVerticalTextPosition(SwingConstants.CENTER);
        buttonpvp.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        buttonpvp.setBackground(Color.WHITE);
        buttonpvp.setOpaque(true);
        add(buttonpvp);

        JButton buttonpvc = new JButton("Player v Computer");
        buttonpvc.setBounds(235, 590, 400, 100);
        buttonpvc.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonpvc.setVerticalTextPosition(SwingConstants.CENTER);
        buttonpvc.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        buttonpvc.setBackground(Color.WHITE);
        buttonpvc.setOpaque(true);
        add(buttonpvc);

        //If the user selects PVP, go straight to the game
        buttonpvp.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                //Computer color doesn't matter B/C there is no computer
                runner.startGame(Mode.PVP, diff, -1);
            }
        });
        //If the user selects PVC, now we have to ask how hard the computer should be
        buttonpvc.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                askDifficulty();
            }
        });
    }

    //Mixes up the panel to now ask whether the computer should be easy/medium/hard
    private void askDifficulty(){
        removeAll(); //Removes all previous components and applies changes
        repaint();

        //New long list of components to be added to the panel
        JLabel choose = new JLabel("What level computer?");
        choose.setBounds(260, 100, 400, 100);
        choose.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        add(choose);

        JButton easy = new JButton("Easy");
        easy.setBounds(335, 240, 200, 100);
        easy.setHorizontalTextPosition(SwingConstants.CENTER);
        easy.setVerticalTextPosition(SwingConstants.CENTER);
        easy.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        easy.setBackground(Color.WHITE);
        easy.setOpaque(true);
        add(easy);

        JButton medium = new JButton("Medium");
        medium.setBounds(335, 440, 200, 100);
        medium.setHorizontalTextPosition(SwingConstants.CENTER);
        medium.setVerticalTextPosition(SwingConstants.CENTER);
        medium.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        medium.setBackground(Color.WHITE);
        medium.setOpaque(true);
        add(medium);

        JButton hard = new JButton("Hard");
        hard.setBounds(335, 640, 200, 100);
        hard.setHorizontalTextPosition(SwingConstants.CENTER);
        hard.setVerticalTextPosition(SwingConstants.CENTER);
        hard.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        hard.setBackground(Color.WHITE);
        hard.setOpaque(true);
        add(hard);

        //When any button is clicked, update difficulty and then ask for which color the player wants
        easy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                diff = Difficulty.EASY;
                playerColor();
            }
        });
        medium.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                diff = Difficulty.MEDIUM;
                playerColor();
            }
        });
        hard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                diff = Difficulty.HARD;
                playerColor();
            }
        });
    }

    //Asks which color the player wants, then goes into game
    private void playerColor(){
        removeAll(); //You know the drill
        repaint();
        //You also know this drill
        JLabel playerChooses = new JLabel("Pick Your Color");
        playerChooses.setBounds(310, 180, 400, 100);
        playerChooses.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        add(playerChooses);

        JButton black = new JButton("Black - 2nd Move");
        black.setBounds(200, 540, 470, 100);
        black.setHorizontalTextPosition(SwingConstants.CENTER);
        black.setVerticalTextPosition(SwingConstants.CENTER);
        black.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        black.setBackground(Color.WHITE);
        black.setOpaque(true);
        add(black);

        JButton white = new JButton("White - 1st Move");
        white.setBounds(200, 340, 470, 100);
        white.setHorizontalTextPosition(SwingConstants.CENTER);
        white.setVerticalTextPosition(SwingConstants.CENTER);
        white.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        white.setBackground(Color.WHITE);
        white.setOpaque(true);
        add(white);

        //When either button is clicked, get into game
        //Tells the board the mode, difficulty, and which color the COMPUTER is (opposite of picked)
        black.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runner.startGame(Mode.PVC, diff, 1);
            }
        });
        white.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runner.startGame(Mode.PVC, diff, -1);
            }
        });
    }
}