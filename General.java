import java.util.List;

public class General extends ChessPiece{
    public General(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row, int col, List<ChessPiece> allPieces) {
        int currRow = getRow();
        int currCol = getCol();
        int dr = Math.abs(row - currRow);
        int dc = Math.abs(col - currCol);

        if (!((dr == 1 && dc == 0) || (dr == 0 && dc == 1))) {
            return false;
        }

        // 仅限九宫内移动
        if (getSide() == Side.RED) {
            if (!(row >= 7 && row <= 9 && col >= 3 && col <= 5)) {
                return false;
            }
        } else {
            if (!(row >= 0 && row <= 2 && col >= 3 && col <= 5)) {
                return false;
            }
        }

        ChessPiece enemyGeneral = null;
        for (ChessPiece p : allPieces) {
            if (p.getType() == PieceType.GENERAL && p.getSide() != getSide()) {
                enemyGeneral = p;
                break;
            }
        }
        if (enemyGeneral != null && enemyGeneral.getCol() == col) {
            
            int minRow = Math.min(row, enemyGeneral.getRow());
            int maxRow = Math.max(row, enemyGeneral.getRow());
            for (int r = minRow + 1; r < maxRow; r++) {
                for (ChessPiece p : allPieces) {
                    if (p.getRow() == r && p.getCol() == col) {
                        return true; 
                    }
                }
            }
            return false;
        }

        return true;
    }
}
