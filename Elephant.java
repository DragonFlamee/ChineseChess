import java.util.List;

public class Elephant extends ChessPiece{
    public Elephant(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row, int col, List<ChessPiece> allPieces) {
        int currRow = getRow();
        int currCol = getCol();
        int dr = Math.abs(row - currRow);
        int dc = Math.abs(col - currCol);
        if (dr != 2 || dc != 2) {
            return false;
        }

        if (getSide() == Side.RED && row < 5) {
            return false;
        }
        if (getSide() == Side.BLACK && row > 4) {
            return false; 
        }

        int centerRow = currRow + (row - currRow) / 2;
        int centerCol = currCol + (col - currCol) / 2;
        for (ChessPiece p : allPieces) {
            if (p.getRow() == centerRow && p.getCol() == centerCol) {
                return false; 
            }
        }
        return true;
    }
}
