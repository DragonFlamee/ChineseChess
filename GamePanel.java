import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class GamePanel extends JPanel implements ChessClient.OnMessageReceived {
    private final List<ChessPiece> pieces = new ArrayList<>();
    private final int boardX = 50;      // 棋盘左上角X坐标
    private final int boardY = 50;      // 棋盘左上角Y坐标
    private final int cellSize = 60;    // 交叉点之间的距离

    private ChessPiece selectedPiece = null;
    private ChessPiece.Side nowSide = ChessPiece.Side.RED; 
    private ChessClient client;
    private ChessPiece.Side localSide; 
    private boolean isNetworkGame = true; 
    private boolean isMyTurn = false; 
    private boolean isGameStarted = false; 

    private JTextField chatInput;
    private JButton sendButton;
    private JPanel chatContentPanel; 
    private JScrollPane chatScrollPanel; 
    private final GridBagConstraints chatGbc = new GridBagConstraints(); // 聊天布局约束

    private int currentGameId = -1;

    public GamePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 220));
        
        JPanel gameBoardPanel = createGameBoardPanel();
        add(gameBoardPanel, BorderLayout.CENTER);
        
        initChatComponents();
        initializeChessPieces();

        client = new ChessClient(this);
        try {
            client.connect("localhost", 12345);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败：" + e.getMessage());
            isNetworkGame = false;
            isMyTurn = true; // 单机模式直接可以走棋
        }
    }

    private JPanel createGameBoardPanel() {
        JPanel gameBoardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChessBoard(g);
                drawAllPieces(g);
                drawCurrentTurn(g);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(650, 750);
            }
        };

        gameBoardPanel.setBackground(new Color(240, 240, 220));
        
        gameBoardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isNetworkGame && (!isGameStarted || !isMyTurn)) {
                    String msg = isGameStarted ? "等待对方走棋..." : "等待另一位玩家加入...";
                    JOptionPane.showMessageDialog(GamePanel.this, msg);
                    return;
                }
                handleMouseClick(e.getX(), e.getY());
            }
        });
        return gameBoardPanel;
    }

    private void initChatComponents() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(280, 0));
        chatPanel.setBackground(Color.WHITE);
        chatPanel.setBorder(BorderFactory.createLineBorder(new Color(0xE5E5E5), 1));

        GridBagConstraints chatGbc = new GridBagConstraints();
        
        chatContentPanel = new JPanel(new GridBagLayout());
        chatGbc.insets = new Insets(4, 0, 4, 0); 
        chatGbc.fill = GridBagConstraints.HORIZONTAL; 
        chatGbc.anchor = GridBagConstraints.NORTHWEST; 
        chatGbc.weightx = 1.0; 
        chatGbc.gridx = 0; 
        chatGbc.gridy = 0; 
        chatContentPanel.setBackground(new Color(0xF0F0F0));
        chatContentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        chatScrollPanel = new JScrollPane(chatContentPanel);
        chatScrollPanel.setBorder(null);
        chatScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPanel.getViewport().setBackground(new Color(0xF0F0F0)); 
        chatPanel.add(chatScrollPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        chatInput = new JTextField();
        chatInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E5E5), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        chatInput.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 发送按钮
        sendButton = new JButton("发送") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            public void setBorder(Border border) {
                super.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            }
        };
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        sendButton.setBackground(new Color(0x07C160));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(0x06B158));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(new Color(0x07C160));
            }
        });

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 将聊天面板添加到主面板
        add(chatPanel, BorderLayout.EAST);

        // 绑定发送事件
        sendButton.addActionListener(e -> sendChatMessage());
        chatInput.addActionListener(e -> sendChatMessage());
    }
    private JLabel createMessageBubble(String text, boolean isSelf) {
        JLabel bubbleLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                Color bgColor = isSelf ? new Color(0xD9FDD3) : new Color(0xECECEC);
                g2.setColor(bgColor);
                // 修正气泡绘制区域
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 15, 15));
                super.paintComponent(g);
                g2.dispose();
            }
        };

        bubbleLabel.setText("<html><body style='width:180px; word-wrap:break-word;'>" + text + "</body></html>");
        bubbleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        bubbleLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubbleLabel.setOpaque(false);
        bubbleLabel.setMaximumSize(new Dimension(180, Integer.MAX_VALUE));
        return bubbleLabel;
    }

    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty() && client != null) {
            client.sendChatMessage(message);
            chatInput.setText("");

            JLabel selfBubble = createMessageBubble(message, true);
            JPanel messageRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            messageRow.setBackground(new Color(0xF0F0F0));
            messageRow.add(selfBubble);
            
            addChatComponent(messageRow);
        }
    }

    // 添加聊天组件并更新布局
    private void addChatComponent(JPanel component) {
        chatContentPanel.add(component, chatGbc);
        chatGbc.gridy++; // 下一行
        chatContentPanel.revalidate();
        chatContentPanel.repaint();
        
        // 自动滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = chatScrollPanel.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private void handleLocalMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (client != null) {
            client.sendMove(fromRow, fromCol, toRow, toCol);
        }
        isMyTurn = false;
        repaint();
    }

    private void handleRemoteMove(int fromRow, int fromCol, int toRow, int toCol) {
        SwingUtilities.invokeLater(() -> {
            ChessPiece piece = findPieceAt(fromRow, fromCol);
            if (piece != null) {
                ChessPiece targetPiece = findPieceAt(toRow, toCol);
                if (targetPiece != null) {
                    pieces.remove(targetPiece);
                }
                
                piece.setPosition(toRow, toCol);
                
                String pName = piece.getType().toString();
                if (this.currentGameId != -1) {
                    DatabaseManager.saveMove(this.currentGameId, piece.getSide().toString(), pName, fromRow, fromCol, toRow, toCol);
                }

                nowSide = (nowSide == ChessPiece.Side.RED) ? ChessPiece.Side.BLACK : ChessPiece.Side.RED;
                isMyTurn = true; 
                repaint();
                
                ChessPiece.Side winner = checkWinner();
                if (winner != null) handleGameOver(winner);
            }
        });
    }
    private void handleRemoteWin(ChessPiece.Side winner) {
        SwingUtilities.invokeLater(() -> {
            handleGameOver(winner);
        });
    }

    private void handleGameOver(ChessPiece.Side winner) {
        JOptionPane.showMessageDialog(this, 
            winner == ChessPiece.Side.RED ? "红方胜利！" : "黑方胜利！",
            "游戏结束", 
            JOptionPane.INFORMATION_MESSAGE);
        initializeChessPieces();
        nowSide = ChessPiece.Side.RED;
        isMyTurn = (localSide == null) ? true : (localSide == ChessPiece.Side.RED);
        repaint();
    }
    
    private void initializeChessPieces() {
        pieces.clear();
        
        // 红方棋子
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 0));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 1));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 2));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 3));
        pieces.add(new General(ChessPiece.PieceType.GENERAL, ChessPiece.Side.RED, 9, 4));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.RED, 9, 5));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.RED, 9, 6));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.RED, 9, 7));
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.RED, 9, 8));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 1));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.RED, 7, 7));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 0));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 2));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 4));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 6));
        pieces.add(new Soldier(ChessPiece.PieceType.SOLDIER, ChessPiece.Side.RED, 6, 8));
        
        // 黑方棋子
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 0));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 1));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 2));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 3));
        pieces.add(new General(ChessPiece.PieceType.GENERAL, ChessPiece.Side.BLACK, 0, 4));
        pieces.add(new Advisor(ChessPiece.PieceType.ADVISOR, ChessPiece.Side.BLACK, 0, 5));
        pieces.add(new Elephant(ChessPiece.PieceType.ELEPHANT, ChessPiece.Side.BLACK, 0, 6));
        pieces.add(new Horse(ChessPiece.PieceType.HORSE, ChessPiece.Side.BLACK, 0, 7));
        pieces.add(new Chariot(ChessPiece.PieceType.CHARIOT, ChessPiece.Side.BLACK, 0, 8));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 1));
        pieces.add(new Cannon(ChessPiece.PieceType.CANNON, ChessPiece.Side.BLACK, 2, 7));
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
        
        if (selectedPiece == null) {
            
            if (clickedPiece != null && clickedPiece.getSide() == nowSide) {
                selectedPiece = clickedPiece;
            }
        } else {
            if (selectedPiece.moveLogic(row, col, pieces)) {
                int fromRow = selectedPiece.getRow();
                int fromCol = selectedPiece.getCol();
                boolean isCapture = clickedPiece != null;
                
                if (isCapture) {
                    if (clickedPiece.getSide() == selectedPiece.getSide()) {
                        selectedPiece = clickedPiece;
                        repaint();
                        return;
                    } else {
                        pieces.remove(clickedPiece); // 移除被吃的棋子
                    }
                }
                
                // 执行移动
                selectedPiece.setPosition(row, col);
                String pName = selectedPiece.getType().toString();

                DatabaseManager.saveMove(this.currentGameId,selectedPiece.getSide().toString(),pName,fromRow, fromCol, row, col);

                selectedPiece = null;
                nowSide = (nowSide == ChessPiece.Side.RED) ? ChessPiece.Side.BLACK : ChessPiece.Side.RED;
                ChessPiece.Side winner = checkWinner();
                if (winner != null) {
                    if (isNetworkGame) {
                        client.sendWinMessage(winner);
                    }
                    handleGameOver(winner);
                    return;
                }
                
                if (isNetworkGame) {
                    isMyTurn = false;
                    handleLocalMove(fromRow, fromCol, row, col);
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
    
    private void drawCurrentTurn(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String turnText = nowSide == ChessPiece.Side.RED ? "当前回合：红方" : "当前回合：黑方";
        Font font = new Font("隶书", Font.BOLD, 24); 
        g2d.setFont(font);
        
        Color textColor = nowSide == ChessPiece.Side.RED ? 
            ChessPiece.RED_COLOR : ChessPiece.BLACK_COLOR;
        g2d.setColor(textColor);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(turnText);
        int boardTotalWidth = 8 * cellSize;
        int textX = boardX + (boardTotalWidth - textWidth) / 2;
        int textY = boardY + (9 * cellSize) + 50; 
        
        g2d.drawString(turnText, textX, textY);
    }
    
    private void drawChessBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int boardWidth = 8 * cellSize;
        int boardHeight = 9 * cellSize;
        
        // 绘制棋盘背景
        g2d.setColor(new Color(222, 184, 135));
        g2d.fillRect(boardX, boardY, boardWidth, boardHeight);
        
        // 绘制棋盘边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(boardX, boardY, boardWidth, boardHeight);
        
        // 绘制横线和竖线
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 9; i++) {
            int y = boardY + i * cellSize;
            g2d.drawLine(boardX, y, boardX + boardWidth, y);
        }
        
        for (int i = 0; i <= 8; i++) {
            int x = boardX + i * cellSize;
            // 楚河汉界分隔
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
        int baseline = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(river, boardX + (boardWidth - riverWidth) / 2, baseline);
        
        // 绘制九宫格
        drawNinePalaces(g2d, 0);  // 黑方九宫
        drawNinePalaces(g2d, 7);  // 红方九宫
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
        
        if (!redGeneralExists) return ChessPiece.Side.BLACK;
        if (!blackGeneralExists) return ChessPiece.Side.RED;
        return null;
    }

    @Override
    public void onMessage(String message) {
        if (message.startsWith("WIN:")) {
            String winnerStr = message.split(":", 2)[1];
            ChessPiece.Side winner = ChessPiece.Side.valueOf(winnerStr);
            handleRemoteWin(winner);
            return;
        }
        
        if (message.startsWith("CHAT:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                onChatMessage(parts[1], parts[2]);
            }
            return;
        }
        
        // 处理移动消息
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
        localSide = side;
        nowSide = ChessPiece.Side.RED;
        isMyTurn = false;
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void onGameStart() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "游戏开始！你是" + (localSide == ChessPiece.Side.RED ? "红方" : "黑方"));
            isGameStarted = true; 
            isMyTurn = (localSide == ChessPiece.Side.RED);

            this.currentGameId = DatabaseManager.createNewGame("Player_Red", "Player_Black");
        });
    }

    @Override
    public void onChatMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            JLabel otherBubble = createMessageBubble(message, false);
            JPanel messageRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            messageRow.setBackground(new Color(0xF0F0F0));
            
            JLabel senderLabel = new JLabel(sender + "：");
            senderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            senderLabel.setForeground(Color.GRAY);
            messageRow.add(senderLabel);
            messageRow.add(otherBubble);
            
            addChatComponent(messageRow);
        });
    }
}