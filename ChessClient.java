import java.io.*;
import java.net.*;

public class ChessClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final OnMessageReceived listener; // 消息回调接口

    // 回调接口：收到消息时通知UI
    public interface OnMessageReceived {
        void onMessage(String message);
        void onSideAssigned(ChessPiece.Side side);
        void onGameStart();
        void onChatMessage(String sender, String message);
    }

    public ChessClient(OnMessageReceived listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("SIDE:")) {
                        String sideStr = line.split(":")[1];
                        ChessPiece.Side side = ChessPiece.Side.valueOf(sideStr);
                        listener.onSideAssigned(side);
                    } else if (line.equals("START")) {
                        listener.onGameStart();
                    } else {
                        listener.onMessage(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("连接断开：" + e.getMessage());
            }
        }).start();
    }

    public void sendMove(int fromRow, int fromCol, int toRow, int toCol) {
        out.println(fromRow + "," + fromCol + "," + toRow + "," + toCol);
    }

    public void close() throws IOException {
        socket.close();
    }
    
    public void sendWinMessage(ChessPiece.Side winner) {
        out.println("WIN:" + winner); 
    }
    
    public void sendChatMessage(String message) {
        if (out != null && message != null) {
            out.println("CHAT:" + message); // 按约定格式发送聊天消息
        }
    }

    public void setSideReceivedListener(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSideReceivedListener'");
    }
}