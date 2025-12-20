import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel {
    private final List<ChessPiece> pieces = new ArrayList<>();
    private final int boardX = 50;      // 棋盘左上角X坐标
    private final int boardY = 50;      // 棋盘左上角Y坐标
    private final int cellSize = 60;    // 交叉点之间的距离

    private ChessPiece selectedPiece = null;
    private int selectedX;
    private int selectedY;
    private ChessPiece.Side now_side = ChessPiece.Side.RED; 
    private ChessClient client;
    private ChessPiece.Side localSide; 
    private boolean isNetworkGame = true; 
    private boolean isMyTurn = false; 

    public GamePanel() {
        setPreferredSize(new Dimension(650, 750));
        setBackground(new Color(240, 240, 220));
        initializeChessPieces();

        // 初始化网络客户端
        client = new ChessClient(new ChessClient.OnMessageReceived() {
            @Override
            public void onMessage(String message) {
                if (message.startsWith("WIN:")) {
                    ChessPiece.Side winner = ChessPiece.Side.valueOf(message.split(":")[1]);
                    handleRemoteWin(winner);
                    return;
                }
                String[] parts = message.split(",");
                if (parts.length == 4) {
                    try {
                        int fromRow = Integer.parseInt(parts[0]);
                        int fromCol = Integer.parseInt(parts[1]);
                        int toRow = Integer.parseInt(parts[2]);
                        int toCol = Integer.parseInt(parts[3]);
                        handleRemoteMove(fromRow, fromCol, toRow, toCol);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSideAssigned(ChessPiece.Side side) {
                // 收到阵营分配
                localSide = side;
                now_side = ChessPiece.Side.RED; // 红方先行
                isMyTurn = (localSide == ChessPiece.Side.RED); // 红方先开始
                SwingUtilities.invokeLater(() -> repaint());
            }

            @Override
            public void onGameStart() {
                JOptionPane.showMessageDialog(GamePanel.this, 
                    "游戏开始！你是" + (localSide == ChessPiece.Side.RED ? "红方" : "黑方"));
            }
        });

        try {
            client.connect("localhost", 12345); // 本地测试用localhost，实际改为服务器IP
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败：" + e.getMessage());
            isNetworkGame = false; 
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isNetworkGame && !isMyTurn) {
                    JOptionPane.showMessageDialog(GamePanel.this, "等待对方走棋...");
                    return;
                }
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void handleLocalMove(int fromRow, int fromCol, int toRow, int toCol) {
        client.sendMove(fromRow, fromCol, toRow, toCol);
        isMyTurn = false; // 发送后切换为对方回合
    }

    private void handleRemoteMove(int fromRow, int fromCol, int toRow, int toCol) {
        SwingUtilities.invokeLater(() -> {
            // 找到对应棋子并移动
            ChessPiece piece = findPieceAt(fromRow, fromCol);
            if (piece != null && piece.moveLogic(toRow, toCol, pieces)) {
                ChessPiece targetPiece = findPieceAt(toRow, toCol);
                if (targetPiece != null) {
                    pieces.remove(targetPiece); // 吃子
                }
                piece.setPosition(toRow, toCol);
                now_side = (now_side == ChessPiece.Side.RED) ? ChessPiece.Side.BLACK : ChessPiece.Side.RED;
                isMyTurn = true; // 对方走完后切换为本地回合
                repaint();
            }
        });
    }

    private void handleRemoteWin(ChessPiece.Side winner) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                winner == ChessPiece.Side.RED ? "红方胜利！" : "黑方胜利！",
                "游戏结束", 
                JOptionPane.INFORMATION_MESSAGE);
            initializeChessPieces(); // 重置棋盘
            now_side = ChessPiece.Side.RED;
            isMyTurn = (localSide == ChessPiece.Side.RED); // 重置回合
            repaint();
        });
    }
    
    private void initializeChessPieces() {
        pieces.clear();
        
        // 第一行：车马相仕帅仕相马车
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 0));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 1));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 2));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 3));
        pieces.add(new General(ChessPiece.PieceType.GENERAL, ChessPiece.Side.RED, 9, 4));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 5));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 6));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 7));
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 8));
        
        // 炮
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 1));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 7));
        
        // 兵
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 0));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 2));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 4));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 6));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 8));
        
        // 第一行：车马象士将士象马车
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 0));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 1));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 2));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 3));
        pieces.add(new General(ChessPiece.PieceType.GENERAL, ChessPiece.Side.BLACK, 0, 4));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 5));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 6));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 7));
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 8));
        
        // 炮
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 1));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 7));
        
        // 卒
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 0));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 2));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 4));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 6));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.BLACK, 3, 8));
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
        boolean isMoveValid = false; 
        
        if (selectedPiece == null) {
            if (clickedPiece != null) {
                if(clickedPiece.getSide() == now_side){
                    selectedPiece = clickedPiece;
                    selectedX = x;
                    selectedY = y;
                }else{
                    System.out.println("现在是"+now_side+"方");
                    return;
                }
            }
        } else {
            if( selectedPiece.moveLogic(row, col,pieces)){
                int fromRow = selectedPiece.getRow();
                int fromCol = selectedPiece.getCol();
                
                if (clickedPiece == null) {
                    selectedPiece.setPosition(row, col);
                    selectedPiece = null;
                    isMoveValid = true;
                } else if (clickedPiece.getSide() != selectedPiece.getSide()) {
                    pieces.remove(clickedPiece); 
                    selectedPiece.setPosition(row, col);
                    selectedPiece = null; 
                    isMoveValid = true;
                } else {
                    selectedPiece = clickedPiece;
                }
                
                if (isMoveValid) {
                    ChessPiece.Side winner = checkWinner();
                    if (winner != null) {
                        repaint();
                        JOptionPane.showMessageDialog(this, 
                            winner == ChessPiece.Side.RED ? "红方胜利！" : "黑方胜利！",
                            "游戏结束", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // 新增：发送胜利消息给对方
                        if (isNetworkGame) {
                            client.sendMove(-1, -1, -1, -1); 
                            client.sendWinMessage(winner); 
                        }
                        
                        initializeChessPieces();
                        now_side = ChessPiece.Side.RED; 
                        repaint();
                        return;
                    }
                    now_side = (now_side == ChessPiece.Side.RED) ? ChessPiece.Side.BLACK : ChessPiece.Side.RED;
                    
                    if (isNetworkGame) {
                        handleLocalMove(fromRow, fromCol, row, col);
                    }
                }
            } else {
                selectedPiece = null;
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
        
        drawCurrentTurn(g);
    }
    
    private void drawCurrentTurn(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String turnText = now_side == ChessPiece.Side.RED ? "当前回合：红方" : "当前回合：黑方";
        Font font = new Font("隶书", Font.BOLD, 24); 
        g2d.setFont(font);
        
        Color textColor = now_side == ChessPiece.Side.RED ? 
            ChessPiece.RED_COLOR : ChessPiece.BLACK_COLOR;
        g2d.setColor(textColor);

        int textX = getWidth() - g2d.getFontMetrics().stringWidth(turnText) - 20;
        int textY = 40; 
        
        g2d.drawString(turnText, textX, textY);
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

    private ChessPiece.Side checkWinner() {
        boolean redGeneralExists = false;
        boolean blackGeneralExists = false;
        for (ChessPiece piece : pieces) {
            if (piece.getType() == ChessPiece.PieceType.GENERAL) {
                if (piece.getSide() == ChessPiece.Side.RED) {
                    redGeneralExists = true;
                } else {
                    blackGeneralExists = true;
                }
            }
        }
        if (!redGeneralExists) {
            return ChessPiece.Side.BLACK;
        } else if (!blackGeneralExists) {
            return ChessPiece.Side.RED;
        } else {
            return null; 
        }
    }

}