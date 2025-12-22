import java.util.List;

public class Cannon extends ChessPiece {
    public Cannon(PieceType type, Side side, int row, int col) {
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int targetRow, int targetCol, List<ChessPiece> allPieces) {
        int currRow = this.getRow();
        int currCol = this.getCol();
        if (currRow != targetRow && currCol != targetCol) {
            return false;
        }

        if (currRow == targetRow && currCol == targetCol) {
            return false;
        }

        int obstacleCount = countObstacles(currRow, currCol, targetRow, targetCol, allPieces);

        ChessPiece targetPiece = findPieceAt(targetRow, targetCol, allPieces);

        if (targetPiece == null) {
            return obstacleCount == 0;
        }
        else {
            return !targetPiece.getSide().equals(this.getSide()) && obstacleCount == 1;
        }
    }

    private int countObstacles(int currRow, int currCol, int targetRow, int targetCol, List<ChessPiece> allPieces) {
        int count = 0;
        if (currRow == targetRow) {
            int minCol = Math.min(currCol, targetCol);
            int maxCol = Math.max(currCol, targetCol);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (findPieceAt(currRow, col, allPieces) != null) {
                    count++;
                }
            }
        }
        else if (currCol == targetCol) {
            int minRow = Math.min(currRow, targetRow);
            int maxRow = Math.max(currRow, targetRow);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (findPieceAt(row, currCol, allPieces) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    private ChessPiece findPieceAt(int row, int col, List<ChessPiece> allPieces) {
        for (ChessPiece piece : allPieces) {
            if (piece.getRow() == row && piece.getCol() == col) {
                return piece;
            }
        }
        return null;
    }
}