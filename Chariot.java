import java.util.List;

public class Chariot extends ChessPiece{
    public Chariot(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row, int col, List<ChessPiece> allPieces) {
        int currRow = getRow();
        int currCol = getCol();

        if (currRow != row && currCol != col) {
            return false;
        }

        if (currRow == row) {
            int minCol = Math.min(currCol, col);
            int maxCol = Math.max(currCol, col);
            for (int c = minCol + 1; c < maxCol; c++) {
                for (ChessPiece p : allPieces) {
                    if (p.getRow() == currRow && p.getCol() == c) {
                        return false; 
                    }
                }
            }
        } else {
            int minRow = Math.min(currRow, row);
            int maxRow = Math.max(currRow, row);
            for (int r = minRow + 1; r < maxRow; r++) {
                for (ChessPiece p : allPieces) {
                    if (p.getRow() == r && p.getCol() == currCol) {
                        return false; 
                    }
                }
            }
        }
        return true;
    }
}
