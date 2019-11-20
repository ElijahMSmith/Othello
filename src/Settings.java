import enums.Difficulty;
import enums.Mode;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Settings extends JPanel {

    private Runner runner;
    private Mode mode;
    private Difficulty diff;

    private int computerColor;
    private boolean showMovesBlack = true;
    private boolean showMovesWhite = true;

    private BufferedImage toggleOn;
    private BufferedImage toggleOff;
    private BufferedImage disabledToggle;

    private JButton easy;
    private JButton medium;
    private JButton hard;

    public Settings(Runner runner, Mode m, Difficulty diff, int computerColor){
        this.computerColor = computerColor;
        this.mode = m;
        this.diff = diff;
        this.runner = runner;
        //Sets up the settings panel
        setBackground(new Color(181, 255, 191));
        setLayout(null);
        setFocusable(true);

        //Long list of components to be added to the panel
        JLabel header = new JLabel("Settings Page - Adjust on the Fly");
        header.setBounds(215, 50, 770, 100);
        header.setFont(new Font("Book Antiqua", Font.BOLD, 28));
        add(header);

        JLabel showMoves = new JLabel("Show  moves after 3 attempts");
        showMoves.setBounds(270, 150, 770, 100);
        showMoves.setFont(new Font("Book Antiqua", Font.PLAIN, 24));
        add(showMoves);

        JLabel changeDifficulty = new JLabel("Change Computer Difficulty");
        changeDifficulty.setBounds(277, 250, 670, 100);
        changeDifficulty.setFont(new Font("Book Antiqua", Font.PLAIN, 24));
        add(changeDifficulty);

        easy = new JButton("Easy");
        easy.setBounds(370, 350, 130, 60);
        easy.setHorizontalTextPosition(SwingConstants.CENTER);
        easy.setVerticalTextPosition(SwingConstants.CENTER);
        easy.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        add(easy);

        medium = new JButton("Medium");
        medium.setBounds(370, 450, 130, 60);
        medium.setHorizontalTextPosition(SwingConstants.CENTER);
        medium.setVerticalTextPosition(SwingConstants.CENTER);
        medium.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        add(medium);

        hard = new JButton("Hard");
        hard.setBounds(370, 550, 130, 60);
        hard.setHorizontalTextPosition(SwingConstants.CENTER);
        hard.setVerticalTextPosition(SwingConstants.CENTER);
        hard.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        add(hard);

        JButton exit = new JButton("Back to Game");
        exit.setBounds(300, 650, 270, 60);
        exit.setHorizontalTextPosition(SwingConstants.CENTER);
        exit.setVerticalTextPosition(SwingConstants.CENTER);
        exit.setFont(new Font("Book Antiqua", Font.BOLD, 20));
        exit.setBackground(Color.WHITE);
        add(exit);

        //Allows for coloring on Mac computers (no idea why it's required, but it is)
        easy.setOpaque(true);
        medium.setOpaque(true);
        hard.setOpaque(true);
        //Disables difficulty buttons when no computer in the game
        if(mode == Mode.PVP){
            easy.setForeground(Color.RED);
            medium.setForeground(Color.RED);
            hard.setForeground(Color.RED);
            easy.setText("N/A");
            medium.setText("N/A");
            hard.setText("N/A");
        }
        //If there is a computer, takes the difficulty and applies color to the respective button
        //If difficulty is set to none, no effect will be had (because there is no computer)
        updateButtonColors();

        //Reads in toggle button images
        //The reason these are images and not real buttons is because adding an image to the background of a button
        //Causes the button to change to a 3-D style, and there is gray around the edges where the button extends in the z plane
        //To avoid that, we just draw them as images and when the user clicks where an image is, it updates things accordingly
        try {
            toggleOn = ImageIO.read(new File("assets/onToggle.png"));
            toggleOff = ImageIO.read(new File("assets/offToggle.png"));
            disabledToggle = ImageIO.read(new File("assets/disabledToggle.png"));
        } catch (IOException e){
            System.out.println("The necessary images for this game could not be loaded.");
        }
        //Tracks the user's mouse for when it presses a toggle (button clicks are handled separately
        MouseListener listener = new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1){ //Left click
                    int x = e.getX();
                    int y = e.getY();
                    if(x >= 90 && x <= 190 && y >= 170 && y <= 230){
                        //If the computer is black, don't do anything when black toggle clicked
                        //Otherwise, toggle the correct button
                        if(!(mode == Mode.PVC && computerColor == -1))
                            toggleShowMovesBlack();
                    }
                    if(x >= 680 && x <= 780 && y >= 170 && y <= 230){
                        //If the computer is white, don't do anything when white toggle clicked
                        //Otherwise, toggle the correct button
                        if(!(mode == Mode.PVC && computerColor == 1))
                            toggleShowMovesWhite();
                    }
                    repaint(); //Apply changes
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        };
        //Activates this listener (only works when the panel is first in the layout, when board is up it doesn't register clicks
        addMouseListener(listener);
        //Adds listeners to the difficulty buttons to update the difficulty if there is in fact a computer
        //If no computer, tells the user their efforts are futile
        easy.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(mode == Mode.PVP){
                    JOptionPane.showMessageDialog(runner, "You are not playing a computer,\n so these buttons do nothing!", "You Can't Choose This", JOptionPane.ERROR_MESSAGE);
                } else {
                    setDiff(Difficulty.EASY);
                    updateButtonColors();
                }
            }
        });
        medium.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(mode == Mode.PVP){
                    JOptionPane.showMessageDialog(runner, "You are not playing a computer,\n so these buttons do nothing!", "You Can't Choose This", JOptionPane.ERROR_MESSAGE);
                } else {
                    setDiff(Difficulty.MEDIUM);
                    updateButtonColors();
                }
            }
        });
        hard.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(mode == Mode.PVP){
                    JOptionPane.showMessageDialog(runner, "You are not playing a computer,\n so these buttons do nothing!", "You Can't Choose This", JOptionPane.ERROR_MESSAGE);
                } else {
                    setDiff(Difficulty.HARD);
                    updateButtonColors();
                }
            }
        });
        //Sets the exit button to leave the settings panel and go back to the game
        exit.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                backToGame();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        requestFocus();
        g.setFont(new Font("Book Antiqua", Font.PLAIN, 20));
        //If the game has a computer, disable whichever toggle is the computer's color
        //If white/black has their setting to show moves when they get stuck set to ON, draw an on toggle. Otherwise, draw an off toggle.
        if(mode == Mode.PVC){
            if(computerColor == -1) {
                g.drawImage(disabledToggle, 90, 170, null);
                g.drawImage(showMovesWhite ? toggleOn : toggleOff, 680, 170, null);
                g.drawString("Computer", 95, 150);
                g.drawString("You", 715, 150);
            } else {
                g.drawImage(showMovesBlack ? toggleOn : toggleOff, 90, 170, null);
                g.drawImage(disabledToggle, 680, 170, null);
                g.drawString("You", 125, 150);
                g.drawString("Computer", 685, 150);
            }
        } else {
            //2 player game, so draw both toggles normally
            g.drawImage(showMovesBlack ? toggleOn : toggleOff, 90, 170, null);
            g.drawImage(showMovesWhite ? toggleOn : toggleOff, 680, 170, null);
            g.drawString("Black", 115, 150);
            g.drawString("White", 705, 150);
        }
    }
    //Returns to the board
    private void backToGame(){
        runner.returnToGame(diff, showMovesBlack, showMovesWhite);
    }
    //Changes the difficulty in settings, which will eventually be sent back to the board with backToGame()
    private void setDiff(Difficulty newDiff) {
        diff = newDiff;
    }
    //Changes whether black gets moves suggested to them to whatever it wasn't
    private void toggleShowMovesBlack() {
        showMovesBlack = !showMovesBlack;
    }
    //Changes whether white gets moves suggested to them to whatever it wasn't
    private void toggleShowMovesWhite() {
        showMovesWhite = !showMovesWhite;
    }
    //If there is a computer, paints the button respective to the current computer difficulty a different color
    private void updateButtonColors(){
        Color light = new Color(181, 240, 255);
        //All buttons are white until painted otherwise
        easy.setBackground(Color.WHITE);
        medium.setBackground(Color.WHITE);
        hard.setBackground(Color.WHITE);

        if(diff == Difficulty.EASY) {
            easy.setBackground(light);
        } else if(diff == Difficulty.MEDIUM){
            medium.setBackground(light);
        } else if(diff == Difficulty.HARD){
            hard.setBackground(light);
        }
        //If difficulty == Difficulty.NONE, there is no computer, so don't paint any buttons special.
        //Apply changes
        repaint();
    }

}
