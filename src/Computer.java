import enums.Difficulty;
import java.util.ArrayList;

public class Computer {

    private ArrayList<Spot> possibleSquares = new ArrayList<>();
    private ArrayList<Spot> corner = new ArrayList<>();
    private ArrayList<Spot> edge = new ArrayList<>();
    private ArrayList<Spot> secondEdge = new ArrayList<>();
    private ArrayList<Spot> secondCorner = new ArrayList<>();
    private ArrayList<Spot> body = new ArrayList<>();

    private int[] dr = { 0, 0, -1, 1, -1, 1, -1,  1};
    private int[] dc = {-1, 1,  0, 0, -1, 1,  1, -1};

    private int color;
    private Difficulty difficulty;
    private Board board;
    private int[][] currentBoardState;

    public Computer(Board board, int color, Difficulty difficulty){
        //Sets defaults based on what the user chose in the home screen
        this.board = board;
        this.color = color;
        this.difficulty = difficulty;
    }
    //Changes the difficulty in the middle of the game, only called if changed in settings
    public void setDifficulty(Difficulty diff){
        difficulty = diff;
    }
    //Returns the computer's color (used for checking for available moves/flipping pieces)
    public int getColor(){
        return color;
    }
    //Called by board, finds the computer's preferred move based on the current difficulty of the computer
    public Spot findMove(){
        //Updates what the board looks like
        //Rather than just make the board public, which is horrifying from a design perspective,
        //just gets a copy of the current state
        currentBoardState = board.getBoardCopy();
        updateAvailable(); //Adds possible locations for the computer to play to respective lists
        //If the computer can't play, then return null, signifying to the board the computer can't play
        if(possibleSquares.size() == 0)
            return null;
        //If the computer is on easy,
        if(difficulty == Difficulty.EASY){
            //From all possible locations to play, it chooses a random spot
            int randSpot = (int)(Math.random() * possibleSquares.size());
            return possibleSquares.get(randSpot);
        //If the computer is on medium,
        } else if(difficulty == Difficulty.MEDIUM){
            //Looks at all available moves, pick one with most flips
            return bestMove(possibleSquares);
        } else { //Difficulty must be hard, computer object doesn't exist if PVP
            //Looks at all available moves and sorts by category (best-worst: corner, edge, body, second corner, second edge)
            //Picks the most flips by categorical preference
            return findBestTurn();
        }
    }
    //Updates every list with possible locations for the computer to play
    private void updateAvailable(){
        //Empties all lists from previous move
        possibleSquares.clear();
        corner.clear();
        edge.clear();
        secondEdge.clear();
        secondCorner.clear();
        body.clear();
        //For each spot board[i][j]
        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j ++) {
                //If the spot is empty, we can consider it
                if (currentBoardState[i][j] == 0) {
                    //Variable that iterates through dr/dc, used in checkSpot
                    for (int d = 0; d < 8; d ++) {
                        //Checks if that place would be a valid spot to play
                        boolean p = board.checkSpot(i, j, d, false);
                        if (p) {
                            //If so, creates a spot object representing that
                            //location and adds it to possible locations
                            Spot s = new Spot(i,j);
                            possibleSquares.add(s);
                            //If that spot qualifies for a special category (not in order of preference here),
                            //adds it to that list. Otherwise, added to body
                            if (corner(i, j)) corner.add(s);
                            else if (edge(i, j)) edge.add(s);
                            else if (secondCorner(i, j)) secondCorner.add(s);
                            else if (secondEdge(i, j)) secondEdge.add(s);
                            else body.add(s);
                        }
                    }
                }
            }
        }
    }
    //Checks if a spot is a corner location
    private boolean corner(int r, int c) {
        return (r == 0 && (c == 0 || c == 7)) || (r == 7 && (c == 0 || c == 7));
    }
    //Checks if a spot is on the outer edge
    private boolean edge (int r, int c) {
        return (r >= 2 && r <= 5 && (c == 0 || c == 7)) || (c >= 2 && c <= 5 && (r == 0 || r == 7));
    }
    //Checks if the spot is on the second to last corner
    private boolean secondCorner (int r, int c) {
        if ((r == 1 && (c == 0 || c == 7 || c == 1 || c == 6)) || (r == 6 && (c == 0 || c == 7 || c == 1 || c == 6))) return true;
        if ((c == 1 && (r == 0 || r == 7)) || (c == 6 && (r == 0 || r == 7))) return true;
        return false;
    }
    //Checks if the spot is on the second to last row/column
    private boolean secondEdge (int r, int c) {
        return (r >= 2 && r <= 5 && (c == 1 || c == 6)) || (c >= 2 && c <= 5 && (r == 1 || r == 6));
    }
    //Finds the best move (most flips) among all spots in a list (Medium level computer's method of choosing)
    //Also used by hard computer for each categorical list
    private Spot bestMove(ArrayList<Spot> arr) {
        //Look at all available moves, pick one with most flips
        Spot bestSpot = null;
        int bestFlips = -1;

        for(int i = 0; i < arr.size(); i++){
            Spot current = arr.get(i);
            int num = 0;

            for (int d = 0; d < 8; d ++)
                num += countFlips(current.r, current.c, d);

            if (num > bestFlips) {
                bestFlips = num;
                bestSpot = current;
            }
        }
        return bestSpot;
    }
    //Finds the absolute best spot to go right now (hard level computer's method of choosing)
    //Corners are the best, otherwise we want an outside edge, then a body spot
    //Second to worst is an edge on the second to last row/col
    //Worse would be second to last row/col
    private Spot findBestTurn() {
        //If there are spots in these categories, returns the best spot for the most preferable category
        if (corner.size() != 0) return bestMove(corner);
        if (edge.size() != 0) return bestMove(edge);
        if (body.size() != 0) return bestMove(body);
        if (secondEdge.size() != 0) return bestMove(secondEdge);
        //If no better category has spots, finds best move among
        return bestMove(secondCorner);
    }
    //Counts how many pieces would be flipped given a starting spot and a direction to look (based on dr[d] and dc[d])
    private int countFlips(int r, int c, int d) {
        int nr = r + dr[d];
        int nc = c + dc[d];
        //If the next spot is out of bounds, stop looking
        if (!inbounds(nr, nc)) return 0;
        //If the next spot is empty, stop looking
        if (currentBoardState[nr][nc] == 0) return 0;
        //If the next spot is the current player's color, stop looking
        if (currentBoardState[nr][nc] == color) return 0;
        //Given spot is a piece that would be fipped, return 1 + flips for next spot (and so on)
        return 1 + countFlips(nr, nc, d);
    }
    //Checks if a spot is in bounds
    private boolean inbounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

}
