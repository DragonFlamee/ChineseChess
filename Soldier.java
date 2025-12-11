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
    public boolean moveLogic(int row,int col){
        if(this.getSide() == ChessPiece.Side.RED){
            if(!isCrossRiver()){
                return row == this.getRow()-1;
            }else{
                return (row == this.getRow()-1) || (Math.abs(this.getCol()-col)==1);
            }
        }else{
            if(!isCrossRiver()){
                return row == this.getRow()+1;
            }else{
                return (row == this.getRow()+1) || (Math.abs(this.getCol()-col)==1);
            }
        }
    }
}

