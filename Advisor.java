import java.util.List;

public class Advisor extends ChessPiece{
    public Advisor(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row, int col, List<ChessPiece> allPieces) {
        int currRow = getRow();
        int currCol = getCol();
        int dr = Math.abs(row - currRow);
        int dc = Math.abs(col - currCol);

        if (dr != 1 || dc != 1) {
            return false;
        }

        if (getSide() == Side.RED) {
            return row >= 7 && row <= 9 && col >= 3 && col <= 5;
        } else {
            return row >= 0 && row <= 2 && col >= 3 && col <= 5;
        }
}
}
