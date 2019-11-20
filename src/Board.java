import enums.Difficulty;
import enums.Mode;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Board extends JPanel {

    //-1 is black, 0 is nothing, 1 is white
    private int[][] board = new int[8][8];
    //Stores most recent move, starts invalid as no moves yet
    private int[] mostRecentPlay = new int[]{-1, -1};
    //Stores move before last move, for use in keeping move history, starts invalid for same reason
    private int[] twoMovesAgo = new int[]{-1, -1};
    private Board instance; //Used for message dialogs
    //Tells whether to register clicks or not
    //True if board is shown, false if settings is shown
    private boolean inFocus = true;

    //Holds a history of every single move made, for use in undoing a previous move
    private Deque<Move> stack = new ArrayDeque<Move>();
    //Stores the pieces flipped by the current move, for use in creating moves for the stack
    private ArrayList<Spot> flippedPieces = new ArrayList<Spot>();

    //Determines whether to show available moves for each color after three failed attempts to play
    private boolean showMovesBlack = true;
    private boolean showMovesWhite = true;

    //All the images for the game
    private BufferedImage boardImage;
    private BufferedImage gameOverBoardImage;
    private BufferedImage blackPiece;
    private BufferedImage whitePiece;
    private BufferedImage mostRecentBlackPiece;
    private BufferedImage mostRecentWhitePiece;
    private BufferedImage possPiece;
    private BufferedImage settings;
    private BufferedImage undo;
    private BufferedImage restart;

    //Info about the mode of the game and the computer, if applicable (if not, will always be null)
    private Mode mode;
    private Computer c;

    //Color constants to make code more readable
    private final int BLACK = -1;
    private final int WHITE = 1;
    private int player = WHITE; //Current player
    //Stores number of failed attempts by current player to place a piece
    private int currentPlayerTries = 0;

    private boolean gameOver = false;//Used to determine when the game should end and tally pieces
    private int[] totalPieces = new int[]{0,0}; //Keeps a running total of black (index 0) and white(index 1)

    //Holds the possible squares that the black/white player respectively could play their next move.
    private ArrayList<Spot> possibleSquaresWhite = new ArrayList<>();
    private ArrayList<Spot> possibleSquaresBlack = new ArrayList<>();
    //Used to scan all surrounding locations around a particular square
    private int[] dr = { 0, 0, -1, 1, -1, 1, -1,  1};
    private int[] dc = {-1, 1,  0, 0, -1, 1,  1, -1};

    //Reads mouse clicks
    private MouseListener listener;

    public Board(Runner runner, Mode m, Difficulty d, int computerColor){
        instance = this;
        mode = m;
        //If PVC, we create a computer. If PVP, computer is always null and
        //not referenced elsewhere (for fear of NullPointerException)
        if(m == Mode.PVC)
            c = new Computer(this, computerColor, d);
        //Sets up board panel, behind where board image will be drawn
        setBackground(Color.WHITE);
        setLayout(null);
        setFocusable(true);
        //Tries to load all images needed for the game
        try {
            boardImage = ImageIO.read(new File("assets/Board.png"));
            gameOverBoardImage = ImageIO.read(new File("assets/gameOverBoard.png"));
            blackPiece = ImageIO.read(new File("assets/BlackPiece.png"));
            whitePiece = ImageIO.read(new File("assets/WhitePiece.png"));
            mostRecentBlackPiece = ImageIO.read(new File("assets/mostRecentBlackPiece.png"));
            mostRecentWhitePiece = ImageIO.read(new File("assets/mostRecentWhitePiece.png"));
            possPiece = ImageIO.read(new File("assets/possPiece.png"));
            settings = ImageIO.read(new File("assets/settings.png"));
            undo = ImageIO.read(new File("assets/undo.png"));
            restart = ImageIO.read(new File("assets/restart.png"));
        } catch (IOException e){
            System.out.println("The necessary images for this game could not be loaded.");
        }

        //Sets up initial status of the board
        board[3][3] = -1;
        board[4][4] = -1;
        board[3][4] = 1;
        board[4][3] = 1;
        //Sets initial available moves for the first player to move, be it a computer or a player
        updateAvailable();
        //Tracks clicks on the board and applies game changes based on where the click is
        listener = new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1){ //Left click
                    //If the settings panel is showing, ignore this click
                    if(!inFocus)
                        return;
                    //Currently waiting for computer move, don't do anything
                    if(mode == Mode.PVC && player == c.getColor())
                        return;
                    //Locates the click
                    int x = e.getX();
                    int y = e.getY();
                    //If the click is on the settings icon, goes to the settings panel
                    if(x >= 790 && x <= 870 && y >= 0 && y <= 80) {
                        runner.goToSettings();
                    } else if(x >= 10 && x <= 80 && y >= 797 && y <= 867){ //Clicked the undo button
                        undo();
                    } else if(x >= 97 && x <= 167 && y >= 797 && y <= 867){ //Clicked the reset button
                        restart();
                    } else { //If the click is anywhere else,
                        //Adds to attempts for current player and tries to play where the click was
                        currentPlayerTries++;
                        boolean result = tryMove(x, y);
                        //If the move was a success
                        if(result) {
                            //Log the successful move
                            stack.push(new Move(player, mostRecentPlay, flippedPieces, twoMovesAgo));
                            //Sets up for next player
                            currentPlayerTries = 0;
                            swapPlayers();
                            //If the next player is a computer
                            if(mode == Mode.PVC){
                                //Computer finds move based on its current difficulty
                                final Spot computerMove = c.findMove();
                                //If there is no available move for the computer
                                if(computerMove == null){
                                    //Goes back to player's turn
                                    swapPlayers();
                                    updateAvailable();
                                    //If the player can't move either, game is over
                                    if((player == BLACK && possibleSquaresBlack.isEmpty()) || (player == WHITE && possibleSquaresWhite.isEmpty())){
                                        endGame();
                                    } else {
                                        //Tells player what happened and that it's their turn again.
                                        //Here and a few other times I call repaint earlier than the auto call at the end, this is because when we bring up a dialog,
                                        //it makes more sense to show the person the new board after the move so they can see that indeed, there are no available moves
                                        //If we call it at the end, the dialog shows, but the board hasn't updated and that's confusing
                                        repaint();
                                        JOptionPane.showMessageDialog(instance, "The computer doesn't have an available move! Your turn again.", "No Moves Available!", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    playComputerMove(computerMove);
                                }
                            } else { //Means that mode is PVP
                                //Updates available locations for next player
                                updateAvailable();
                                int openSquares = countEmpty();
                                //If there is nowhere for the next player to go, end the game
                                if(openSquares == 0)
                                    endGame();
                                //If next player can't play, turn goes back to previous player.
                                //If they too can't play, end the game
                                if (player == WHITE && possibleSquaresWhite.isEmpty()) {
                                    player = BLACK;
                                    updateAvailable();
                                    if(possibleSquaresBlack.isEmpty()){
                                        endGame();
                                    } else {
                                        repaint();
                                        JOptionPane.showMessageDialog(instance, "The white player doesn't have an available move! Black's turn again.", "No Moves Available!", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else if (player == BLACK && possibleSquaresBlack.isEmpty()) {
                                    player = WHITE;
                                    updateAvailable();
                                    if(possibleSquaresWhite.isEmpty()){
                                        endGame();
                                    } else {
                                        repaint();
                                        JOptionPane.showMessageDialog(instance, "The black player doesn't have an available move! White's turn again.", "No Moves Available!", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                            //Apply changes to the board
                            repaint();
                        }
                    }
                }
            }
            //We don't care about any of these events, but they require an "implementation"
            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        };
        addMouseListener(listener); //Activates listener for the board
        //If the computer has the first turn, have them play once before handing off to the player
        if(mode == Mode.PVC && player == c.getColor()){
            //Set their move for one second later after construction of things
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Spot computerMove = c.findMove();
                            doTurn(computerMove.r, computerMove.c);
                            swapPlayers();
                            updateAvailable();
                            repaint(); //Here we have to repaint since it doesn't update the board otherwise
                            cancel(); //Ends the thread
                        }
                    },
                    1000
            );
        }
    }

    //Does the act of playing the move found and, updating the game state for after the move is made
    private void playComputerMove(Spot computerMove){
        //Give 1 second delay for computer so you can see it play
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //Plays the move after a 1 second delay, then swaps to the player and updates their available moves
                        doTurn(computerMove.r, computerMove.c);
                        //Log the computer's move
                        stack.push(new Move(player, mostRecentPlay, flippedPieces, twoMovesAgo));
                        swapPlayers();
                        updateAvailable();
                        //If the computer played on the last open spot on the board, end the game
                        if(countEmpty() == 0){
                            endGame();
                        //If the player has nowhere to go,
                        } else if((player == BLACK && possibleSquaresBlack.isEmpty()) || (player == WHITE && possibleSquaresWhite.isEmpty())){
                            //Player can't move, send back to computer
                            swapPlayers();
                            Spot nextComputerMove = c.findMove();
                            //If the computer also can't move, end the game
                            if(nextComputerMove == null){
                                endGame();
                            //Otherwise, update the board before telling the user what happened, then let the computer go again
                            } else {
                                repaint();
                                JOptionPane.showMessageDialog(instance, "You don't have an available move! The computer gets another turn.", "No Moves Available!", JOptionPane.ERROR_MESSAGE);
                                playComputerMove(nextComputerMove);
                            }
                        } //Else its the player's turn, so we'll wait for their move
                        repaint(); //Have to repaint since auto-call below triggers before this
                        cancel(); //Ends the thread
                    }
                },
                1000
        );
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        requestFocusInWindow();
        g.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        //If the game is over, draw a yellow board to signify a obvious change in game state to players
        //Otherwise, draw normal green board
        g.drawImage(gameOver ? gameOverBoardImage : boardImage, 0, 0, null);

        //Draws all pieces on the board currently
        for(int r = 0; r < 8; r++){
            for(int c = 0; c < 8; c++){
                int dx = 80 + 80 * c + 10 * c;
                int dy = 80 + 80 * r + 10 * r;
                //If this piece is the most recently played piece, draw special piece with border to show that
                //Otherwise, draw whatever colored piece it is
                if(mostRecentPlay[0] == r && mostRecentPlay[1] == c)
                    g.drawImage(board[r][c] == -1 ? mostRecentBlackPiece : mostRecentWhitePiece, dx, dy, null);
                else if(board[r][c] == -1)
                    g.drawImage(blackPiece, dx, dy, null);
                else if(board[r][c] == 1)
                    g.drawImage(whitePiece, dx, dy, null);
            }
        }
        //If the game is over, draw some additional stuff
        if(gameOver){
            g.setColor(Color.BLACK);
            //Draws text to say why the game ended, how many pieces each side has, and who won
            g.drawString((countEmpty() == 0 ? "No more open spaces!" : "No player can move!") + " Game Over!", 10, 45);
            g.drawString("Black: " + totalPieces[0] + " pieces", 10, 840);
            g.drawString("White: " + totalPieces[1] + " pieces", 280, 840);
            if(totalPieces[0] > totalPieces[1]) //Black wins
                g.drawString("Black Wins!", 660, 840);
            else if(totalPieces[0] < totalPieces[1]) //White wins
                g.drawString("White Wins!", 660, 840);
            else //Tie
                g.drawString("Tie Game!", 660, 840);
            removeMouseListener(listener); //Remove the mouse listener so that game stops
            return; //Ends the drawing now
        }
        //If the current player has already tried moving three times unsuccessfully, draw available locations to play in yellow
        if(currentPlayerTries >= 3 && ((player == BLACK && showMovesBlack) || (player == WHITE && showMovesWhite))){
            ArrayList<Spot> currentPoss = player == BLACK ? possibleSquaresBlack : possibleSquaresWhite;
            for(Spot s : currentPoss){
                int r = s.r;
                int c = s.c;
                int dx = 80 + 80 * c + 10 * c;
                int dy = 80 + 80 * r + 10 * r;
                g.drawImage(possPiece, dx, dy, null);
            }
        }
        totalPieces = countPieces(); //Update piece counts before drawing how many each side has
        //Draws whose turn it is
        g.drawString((mode == Mode.PVC && player == c.getColor() ? "Computer's Turn" : (player == BLACK ? "Black" : "White") + " Player's Turn"), 15, 45);
        //Draws running total of each color
        g.drawString("B: " + totalPieces[0] + "   W: " + totalPieces[1], 650, 840);
        //Draws icons
        g.drawImage(settings, 790, 0, null);
        g.drawImage(settings, 790, 0, null);
        g.drawImage(undo, 10, 797, null);
        g.drawImage(restart, 97, 797, null);
    }

    //Gives a COPY of the board to the caller (the computer class), not actual board reference
    public int[][] getBoardCopy(){
        int[][] copy = new int[8][8];

        for(int r = 0; r < 8; r++){
            for(int c = 0; c < 8; c++)
                copy[r][c] = board[r][c];
        }

        return copy;
    }

    //Ends the game, counts up pieces, and redraws board with end of game stuff on it
    private void endGame(){
        gameOver = true;
        totalPieces = countPieces();
        repaint();
    }

    //Tries to move where the player clicked
    //Returns false if the method failed to place the piece (for whatever reason)
    //Returns true if the space selected was valid and the method successfully placed the piece
    private boolean tryMove(int x, int y){
        if(x < 80 || x > 790 || y < 80 || y > 790) { //The choice is outside of the grid
            JOptionPane.showMessageDialog(instance, "That's not a square! Pick again.", "Invalid Choice!", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int col = (x-75) / 90; //Each square is considered 90x90 here to take into account half of the lines between squares
        int row = (y-75) / 90; //The -75 is to ignore the empty space in front of the grid
        boolean goodMove = doTurn(row, col); //Tries to play the piece
        //If the move wasn't valid,
        if(!goodMove) {
            //If they are at three tries and their setting to show moves is on, add some extra text to next dialog
            String extraDialog = (currentPlayerTries >= 3 && (player == BLACK ? showMovesBlack : showMovesWhite) ? "\n\nIf you need help, we've highlighted\nall available squares you can play in." : "");
            //If same condition met, redraws board with those yellow pieces
            if((player == BLACK ? showMovesBlack : showMovesWhite) && currentPlayerTries >= 3)
                    repaint();
            //Shows dialog with or without extra text
            JOptionPane.showMessageDialog(instance, "That's not a valid spot to play! Pick again." + extraDialog, "Invalid Choice!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        //We did the move! Tell the listener that it worked
        return true;
    }

    //Updates all available locations for the player to play
    private void updateAvailable(){
        ArrayList<Spot> possibleSquares = player == BLACK ? possibleSquaresBlack : possibleSquaresWhite;
        possibleSquares.clear();

        for (int r = 0; r < 8; r ++) {
            for (int c = 0; c < 8; c ++) {
                if (board[r][c] == 0) {
                    for (int d = 0; d < 8; d ++) {
                        boolean p = checkSpot(r, c, d, false);
                        //If the spot works, add this spot to list
                        if (p) {
                            Spot s = new Spot(r, c);
                            possibleSquares.add(s);
                        }
                    }
                }
            }
        }
    }

    //Counts pieces on both sides and returns them in array of length 2
    private int[] countPieces(){
        int w = 0;
        int b = 0;

        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j ++) {
                if (board[i][j] == WHITE) w ++;
                else if(board[i][j] == BLACK) b++;
            }
        }

        return new int[]{b, w};
    }

    //Checks if the spot in question is valid
    //d indicates the direction we are moving to check if that spot works
    //Padded is false for first call, which looks at piece right next to placed piece
    //Padded is true for future calls, where there is a padding of at least one opposite-color piece between
    //current piece and the placed piece
    //There must be at least one opposite colored piece of padding for the move to be valid,
    //this padding indicates at least one piece will be flipped in that direction
    public boolean checkSpot(int r, int c, int d, boolean padded) {
        int nr = r + dr[d];
        int nc = c + dc[d];

        //If the space is out of bounds or empty, not valid
        if (!inbounds(nr, nc)) return false;

        //If we run into empty space before running into our color again, not a valid move
        if (board[nr][nc] == 0) return false;

        //If the space is the same color as the player trying to move, but we haven't already gone through 1+ opposite color pieces, return false
        if (board[nr][nc] == player && !padded) return false;

        //If we HAVE already gone through an opposite colored piece (padded = true), return true, good move
        if (board[nr][nc] == player) return true;

        //Keep iterating that same direction
        return checkSpot(nr, nc, d, true);
    }

    //Actively places the piece (returning true), if possible. If not possible, returns false.
    private boolean doTurn(int r, int c) {
        boolean possibleMove = false;
        //If the place is already occupied or out of bounds, not a valid move
        if (!inbounds(r, c)) return false;
        if (board[r][c] != 0) return false;

        ArrayList<Spot> currentFlips = new ArrayList<Spot>();
        for (int i = 0; i < 8; i ++) {
            if (checkSpot(r, c, i, false)) {
                flip(r, c, i, currentFlips); // do the turn because it's a possible turn
                possibleMove = true;
            }
        }
        //Returns false if there this is not a possible move (no pieces flipped)
        if (!possibleMove) return false;
        //Places the piece and updates mostRecentPlay to hold this piece's location
        board[r][c] = player;
        twoMovesAgo[0] = mostRecentPlay[0];
        twoMovesAgo[1] = mostRecentPlay[1];
        mostRecentPlay[0] = r;
        mostRecentPlay[1] = c;
        //Since we made a move, update the flips stored from last turn to those made this turn
        flippedPieces = currentFlips;
        //Plays audio clip when piece is placed successfully
        try { //We have to do the whole process every time, otherwise the clip only plays once. I know, it looks unnecessary, but it is necessary.
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Paths.get("assets/placePiece.wav").toUri().toURL()));
            clip.start();
        } catch (Exception e) {
            System.out.println("ERROR: Audio can't play.");
        }
        //We did it! Tell the world all about it.
        return true;
    }

    //Flips the all pieces in a given direction from given location, until it reaches the opposite color
    private void flip(int r, int c, int d, ArrayList<Spot> flips) {
        int nr = r + dr[d];
        int nc = c + dc[d];

        //If we get to empty space or opposite color, end the flipping
        if (board[nr][nc] == 0) return;
        if (board[nr][nc] == player) return;
        //Continue flipping
        board[nr][nc] = player;
        //Add to list to signify this was flipped on most recent turn
        flips.add(new Spot(nr, nc));
        flip(nr, nc, d, flips);
    }

    //Counts remaining spaces. Used to decide if game is over or not
    private int countEmpty() {
        int c = 0;
        for (int i = 0; i < 8; i ++)
            for (int j = 0; j < 8; j ++)
                if (board[i][j] == 0) c ++;
        return c;
    }

    //Checks whether a piece is in the bounds of the board
    private boolean inbounds(int r, int c) {
        return (r >= 0 && r < 8 && c >= 0 && c < 8);
    }

    //Changes whether black has available moves highlighted after 3 tries
    public void setShowMovesBlack(boolean show){
        showMovesBlack = show;
    }

    //Changes whether white has available moves highlighted after 3 tries
    public void setShowMovesWhite(boolean show){
        showMovesWhite = show;
    }

    //Changes difficulty of computer
    public void setDifficulty(Difficulty diff){
        //Does nothing if PVP, trying to reference the computer would causes NullPointerException
        if(mode == Mode.PVC)
            c.setDifficulty(diff);
    }

    //Sets boolean telling whether board is currently in focus of not
    public void setInFocus(boolean b){
        inFocus = b; //Prevents Board from adding to currentPlayerTries when clicking toggles in settings
    }

    //Swaps current player to the other one
    private void swapPlayers(){
        player = player == BLACK ? WHITE : BLACK;
    }

    //Removes all pieces from the board, resets the middle four pieces to the default, then sets the white player to go next
    //If that white player is the computer, make the first computer move
    private void restart(){
        //Empty board and set initial pieces
        board = new int[8][8];
        board[3][3] = -1;
        board[4][4] = -1;
        board[3][4] = 1;
        board[4][3] = 1;
        //Set whose turn it is
        player = WHITE;
        //Also reset the move history stack
        stack = new ArrayDeque<Move>();
        repaint();
        //Reset two most recent moves to invalid values (back to beginning, nobody has played in this game state
        mostRecentPlay = new int[]{-1, -1};
        twoMovesAgo = new int[]{-1, -1};
        //If the computer has the first turn, make them go
        //playComputerMove(Spot s) does this automagically
        if(mode == Mode.PVC && player == c.getColor()){
            Spot computerMove = c.findMove();
            playComputerMove(computerMove);
        }
    }

    //Reads data about previous move from top of the stack, and then undoes that move
    private void undo(){
        if(stack.isEmpty() || (stack.size() == 1 && mode == Mode.PVC)) {
            //Condition 1: There are no recorded moves to undo
            //Condition 2: The only move was a computer that played first, no player move to undo before that
            //Either case, tell them they can't undo anything and be done with it
            JOptionPane.showMessageDialog(instance, "No moves to undo!", "Cannot perform action!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //Takes a move off the top of the history stack and pulls out all its information
        Move lastMove = stack.pop();
        int moveColor = lastMove.getColor();
        Spot piecePlayed = lastMove.getPlacedPiece();
        Spot prevPiecePlayed = lastMove.getPreviousPiece();
        ArrayList<Spot> flips = lastMove.getFlips();

        if(mode == Mode.PVC){
            //Will always be player's turn, player can't click while computer is moving

            //Undoes two moves, one for the computer and one for the user.
            //After undoing the computer move, change lastMove to the move right before,
            // the player's move, and undo that too
            for(int i = 0; i < 2; i++){
                //Performs the undoing action
                board[piecePlayed.r][piecePlayed.c] = 0;
                for(Spot s : flips)
                    board[s.r][s.c] *= -1;
                mostRecentPlay = new int[]{prevPiecePlayed.r, prevPiecePlayed.c};
                //If first iteration, update last move to the move before the current
                //If second iteration, skip this so we aren't popping extra elements we shouldn't be touching yet
                if(i == 0) {
                    lastMove = stack.pop();
                    //We don't care about move color since the color will return to the player's color every time, no need to reassign
                    piecePlayed = lastMove.getPlacedPiece();
                    prevPiecePlayed = lastMove.getPreviousPiece();
                    flips = lastMove.getFlips();
                }
            }

        } else {
            //Flips back all pieces flipped from previous move and removes the piece played
            board[piecePlayed.r][piecePlayed.c] = 0;
            for(Spot s : flips)
                board[s.r][s.c] *= -1;
            mostRecentPlay = new int[]{prevPiecePlayed.r, prevPiecePlayed.c};
            //Whoever made the move, it's their turn again. This is so that, if a player was skipped between two moves,
            //undoing the move returns to the correct player's turn, not just "the opposite of the current color", which is wrong if there
            //were two black moves in a row for that reason.
            player = moveColor;
            updateAvailable();
            //We don't need to worry about checking if a player can play or not, since they clearly already did.
        }
        //Update board status
        repaint();
    }
}