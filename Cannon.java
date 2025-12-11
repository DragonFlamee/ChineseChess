public class Cannon extends ChessPiece{
    public Cannon(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    @Override
    public boolean moveLogic(int row,int col){
        return true;
    }
}
