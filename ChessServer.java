import java.io.*;
import java.net.*;
import java.util.*;

public class ChessServer {
    private ServerSocket serverSocket;
    private final List<Socket> clients = new ArrayList<>();
    private final ChessPiece.Side[] clientSides = new ChessPiece.Side[2];
    
    public static void main(String[] args) throws IOException {
        new ChessServer().start(12345); // 监听12345端口
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("服务器启动，等待玩家连接...");

        while (clients.size() < 2) {
            Socket clientSocket = serverSocket.accept();
            clients.add(clientSocket);
            
            clientSides[clients.size() - 1] = clients.size() == 1 ? 
                ChessPiece.Side.RED : ChessPiece.Side.BLACK;
            System.out.println("玩家" + clients.size() + "连接（" + clientSides[clients.size()-1] + "）");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("SIDE:" + clientSides[clients.size()-1]);

            new ClientHandler(clientSocket, clients.size() - 1).start();
        }

        for (Socket client : clients) {
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("START");
        }
    }

    // 处理客户端消息的线程
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private int clientIndex;

        public ClientHandler(Socket socket, int index) {
            this.clientSocket = socket;
            this.clientIndex = index;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("收到" + clientSides[clientIndex] + "消息：" + inputLine);
                    
                    Socket otherClient = clients.get(1 - clientIndex);
                    PrintWriter out = new PrintWriter(otherClient.getOutputStream(), true);
                    
                    if (inputLine.startsWith("CHAT:")) {
                        out.println("CHAT:" + clientSides[clientIndex] + ":" + inputLine.substring(5));
                    } else {
                        out.println(inputLine);
                    }
                }
            } catch (IOException e) {
                System.err.println("客户端断开连接：" + e.getMessage());
            }
        }
    }
}