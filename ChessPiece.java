import java.awt.*;
import java.util.List;

public abstract  class ChessPiece {
    public enum PieceType {
        GENERAL, ADVISOR, ELEPHANT, HORSE, CHARIOT, CANNON, SOLDIER
    }
    
    public enum Side {
        RED, BLACK
    }
    
    private final PieceType type;
    private final Side side;
    private int row;
    private int col;
    private final String name;
    
    private static final int DEFAULT_SIZE = 50;
    private static final int BORDER_WIDTH = 3;
    private static final Color WHITE_BG = new Color(255, 255, 255, 250);
    private static final Color RED_COLOR = new Color(220, 50, 50);
    private static final Color BLACK_COLOR = new Color(30, 30, 30);
    
    public ChessPiece(PieceType type, Side side, int row, int col) {
        this.type = type;
        this.side = side;
        this.row = row;
        this.col = col;
        this.name = getChineseName(type, side);
    }
    
    public void draw(Graphics g, int boardX, int boardY, int cellSize, boolean isSelected) {
        int centerX = boardX + col * cellSize; // 之前已修正为交叉点坐标
        int centerY = boardY + row * cellSize;
        int diameter = (int)(cellSize * 0.75);
        drawAtCenter(g, centerX, centerY, diameter, isSelected); // 传递选中状态
    }
    
    public void drawAtCenter(Graphics g, int centerX, int centerY, int diameter, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int radius = diameter / 2;

        g2d.setColor(WHITE_BG);
        g2d.fillOval(centerX - radius, centerY - radius, diameter, diameter);
        
        Color borderColor = (side == Side.RED) ? RED_COLOR : BLACK_COLOR;
        if (isSelected) {
            borderColor = Color.BLUE;
            g2d.setStroke(new BasicStroke(BORDER_WIDTH + 2));
        } else {
            g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        }
        g2d.setColor(borderColor);
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
        
        drawPieceText(g2d, centerX, centerY, diameter, borderColor);
    }
    
    public void drawAtCenter(Graphics g, int centerX, int centerY, int diameter) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int radius = diameter / 2;
        
        g2d.setColor(WHITE_BG);
        g2d.fillOval(centerX - radius, centerY - radius, diameter, diameter);
        
        Color borderColor = (side == Side.RED) ? RED_COLOR : BLACK_COLOR;
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
        
        drawPieceText(g2d, centerX, centerY, diameter, borderColor);
    }
    
    private void drawPieceText(Graphics2D g2d, int centerX, int centerY, int diameter, Color textColor) {
        g2d.setColor(textColor);
        
        Font font = new Font("隶书", Font.BOLD, (int)(diameter * 0.7));
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(this.name);
        
        int textAscent = fm.getAscent();
        int textDescent = fm.getDescent();

        int textY = centerY + (textAscent - textDescent) / 2;
        
        g2d.drawString(name, centerX - textWidth / 2, textY);
    }
    public void drawAtPosition(Graphics g, int x, int y, int size) {
        drawAtCenter(g, x + size/2, y + size/2, size);
    }
    
    public void drawAtPosition(Graphics g, int x, int y) {
        drawAtPosition(g, x, y, DEFAULT_SIZE);
    }
    
    private String getChineseName(PieceType type, Side side) {
        return switch (type) {
            case GENERAL -> side == Side.RED ? "帥" : "将";
            case ADVISOR -> side == Side.RED ? "仕" : "士";
            case ELEPHANT -> side == Side.RED ? "相" : "象";
            case HORSE -> "馬";
            case CHARIOT -> "車";
            case CANNON -> "炮";
            case SOLDIER -> side == Side.RED ? "兵" : "卒";
            default -> "";
        };
    }
    
    public boolean contains(int x, int y, int boardX, int boardY, int cellSize) {
        int centerX = boardX + col * cellSize;
        int centerY = boardY + row * cellSize;
        int radius = (int)(cellSize * 0.75) / 2;
        
        int dx = x - centerX;
        int dy = y - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    public abstract boolean moveLogic(int row,int cl,List<ChessPiece> allPieces);
    
    public PieceType getType() { return type; }
    public Side getSide() { return side; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getName() { return name; }
    
    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
    public void setPosition(int row, int col) { this.row = row; this.col = col; }
    
    @Override
    public String toString() {
        return String.format("%s%s(%d,%d)", side == Side.RED ? "红" : "黑", name, row, col);
    }
}