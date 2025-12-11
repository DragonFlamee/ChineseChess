public class Horse extends ChessPiece{
    public Horse(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row,int col){
        return true;
    }
}
