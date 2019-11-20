import java.util.ArrayList;

//Stores all required information about the a move, for use in undoing previously made moves
public class Move {
    private int color;
    private Spot placedPiece;
    private ArrayList<Spot> flips;
    private Spot previousPiece;

    public Move(int color, int[] move, ArrayList<Spot> flips, int[] prevMove){
        this.color = color;
        placedPiece = new Spot(move[0], move[1]);
        this.flips = flips;
        previousPiece = new Spot(prevMove[0], prevMove[1]);
    }

    //Accessors for the move components
    public int getColor(){
        return color;
    }

    public Spot getPlacedPiece(){
        return placedPiece;
    }

    public ArrayList<Spot> getFlips(){
        return flips;
    }

    public Spot getPreviousPiece(){
        return previousPiece;
    }
}
