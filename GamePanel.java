import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel {
    private final List<ChessPiece> pieces = new ArrayList<>();
    private final int boardX = 50;      // 棋盘左上角X坐标
    private final int boardY = 50;      // 棋盘左上角Y坐标
    private final int cellSize = 60;    // 交叉点之间的距离（像素）

    private ChessPiece selectedPiece = null;
    private int selectedX;
    private int selectedY;
    
    public GamePanel() {
        setPreferredSize(new Dimension(650, 750));
        setBackground(new Color(240, 240, 220));
        initializeChessPieces();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    
    private void initializeChessPieces() {
        pieces.clear();
        
        // 第一行：车马相仕帅仕相马车
        pieces.add(new ChessPiece(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 0));
        pieces.add(new ChessPiece(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 1));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 2));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 3));
        pieces.add(new ChessPiece(ChessPiece.PieceType.GENERAL, ChessPiece.Side.RED, 9, 4));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 5));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 6));
        pieces.add(new ChessPiece(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 7));
        pieces.add(new ChessPiece(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 8));
        
        // 炮
        pieces.add(new ChessPiece(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 1));
        pieces.add(new ChessPiece(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 7));
        
        // 兵
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 0));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 2));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 4));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 6));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 8));
        
        // 第一行：车马象士将士象马车
        pieces.add(new ChessPiece(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 0));
        pieces.add(new ChessPiece(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 1));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 2));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 3));
        pieces.add(new ChessPiece(ChessPiece.PieceType.GENERAL, ChessPiece.Side.BLACK, 0, 4));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 5));
        pieces.add(new ChessPiece(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 6));
        pieces.add(new ChessPiece(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 7));
        pieces.add(new ChessPiece(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 8));
        
        // 炮
        pieces.add(new ChessPiece(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 1));
        pieces.add(new ChessPiece(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 7));
        
        // 卒
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 0));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 2));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 4));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 6));
        pieces.add(new ChessPiece(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 8));
    }

    private void handleMouseClick(int x, int y) {
        int col = (x - boardX + cellSize / 2) / cellSize;
        int row = (y - boardY + cellSize / 2) / cellSize;
        
        if (row < 0 || row > 9 || col < 0 || col > 8) {
            selectedPiece = null; 
            repaint();
            return;
        }
        
        ChessPiece clickedPiece = findPieceAt(row, col);
        
        if (selectedPiece == null) {
            if (clickedPiece != null) {
                selectedPiece = clickedPiece;
                selectedX = x;
                selectedY = y;
            }
        } else {
            if (clickedPiece == null) {
                selectedPiece.setPosition(row, col);
                selectedPiece = null; 
            } else if (clickedPiece.getSide() != selectedPiece.getSide()) {
                pieces.remove(clickedPiece); 
                selectedPiece.setPosition(row, col);
                selectedPiece = null; 
            } else {
                selectedPiece = clickedPiece;
            }
        }
        
        repaint();
    }

    private ChessPiece findPieceAt(int row, int col) {
        for (ChessPiece piece : pieces) {
            if (piece.getRow() == row && piece.getCol() == col) {
                return piece;
            }
        }
        return null;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        drawChessBoard(g);
        
        drawAllPieces(g);
    }
    
    private void drawChessBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int boardWidth = 8 * cellSize;  // 8个格子，9个交叉点
        int boardHeight = 9 * cellSize; // 9个格子，10个交叉点
        
        // 绘制棋盘背景
        g2d.setColor(new Color(222, 184, 135));
        g2d.fillRect(boardX, boardY, boardWidth, boardHeight);
        
        // 绘制棋盘边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(boardX, boardY, boardWidth, boardHeight);
        
        // 绘制网格线
        g2d.setStroke(new BasicStroke(1));
        
        // 绘制横线（10条）
        for (int i = 0; i <= 9; i++) {
            int y = boardY + i * cellSize;
            g2d.drawLine(boardX, y, boardX + boardWidth, y);
        }
        
        // 绘制竖线（9条）
        for (int i = 0; i <= 8; i++) {
            int x = boardX + i * cellSize;
            g2d.drawLine(x, boardY, x, boardY + 4 * cellSize);
            g2d.drawLine(x, boardY + 5 * cellSize, x, boardY + boardHeight);
        }
        
        // 绘制楚河汉界
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("隶书", Font.BOLD, 30));
        String river = "楚    河    漢    界";
        FontMetrics fm = g2d.getFontMetrics();
        int riverWidth = fm.stringWidth(river);
        int centerY = boardY + 4 * cellSize + cellSize / 2; 
        int baseline = centerY + fm.getAscent() / 2; 
        g2d.drawString(river, boardX + (boardWidth - riverWidth) / 2, baseline);
        
        // 绘制九宫斜线
        drawNinePalaces(g2d, 0); 
        drawNinePalaces(g2d, 7);  
    }
    
    private void drawNinePalaces(Graphics2D g2d, int startY) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        int x1 = boardX + 3 * cellSize;
        int y1 = boardY + startY * cellSize;
        int x2 = boardX + 5 * cellSize;
        int y2 = boardY + (startY + 2) * cellSize;
        
        g2d.drawLine(x1, y1, x2, y2);
        g2d.drawLine(x2, y1, x1, y2);
    }
    
    private void drawAllPieces(Graphics g) {
        for (ChessPiece piece : pieces) {
            boolean isSelected = (selectedPiece != null && piece == selectedPiece);
            piece.draw(g, boardX, boardY, cellSize, isSelected); 
        }
    }
    
}