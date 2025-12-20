import java.util.List;

public class Soldier extends ChessPiece{

    public Soldier(PieceType type,Side side,int row,int col){
        super(type, side, row, col);
    }

    public boolean isCrossRiver(){
        if(this.getSide() == ChessPiece.Side.RED){
            return this.getRow() < 5;
        }else return this.getRow() > 4;
    }

    @Override
    public boolean moveLogic(int row,int col,List<ChessPiece> allPieces){
        if(this.getSide() == ChessPiece.Side.RED){
            if(!isCrossRiver()){
                return (row == this.getRow()-1) && (col == this.getCol());
            }else{
                return ((row == this.getRow()-1) && (col == this.getCol())) || (Math.abs(this.getCol()-col)==1 && row == this.getRow());
            }
        }else{
            if(!isCrossRiver()){
                return (row == this.getRow()+1) && (col == this.getCol());
            }else{
                return ((row == this.getRow()+1) && (col == this.getCol())) || (Math.abs(this.getCol()-col)==1 && row == this.getRow());
            }
        }
    }
}

