import java.util.List;

public class Horse extends ChessPiece{
    public Horse(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row, int col, List<ChessPiece> allPieces) {
        int currRow = getRow();
        int currCol = getCol();
        int dr = Math.abs(row - currRow);
        int dc = Math.abs(col - currCol);

        if (!((dr == 1 && dc == 2) || (dr == 2 && dc == 1))) {
            return false;
        }

        int legRow = currRow;
        int legCol = currCol;
        if (dr == 2) {
            legRow = currRow + (row > currRow ? 1 : -1);
        } else {
            legCol = currCol + (col > currCol ? 1 : -1);
        }
        for (ChessPiece p : allPieces) {
            if (p.getRow() == legRow && p.getCol() == legCol) {
                return false;
            }
        }
        return true;
    }
}
