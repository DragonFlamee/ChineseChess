import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/chess_db?serverTimezone=GMT%2B8&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static int createNewGame(String red, String black) {
        String sql = "INSERT INTO games (player_red, player_black) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, red);
            pstmt.setString(2, black);
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        System.out.println("成功创建新对局，ID 为: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("创建新对局失败！");
            e.printStackTrace();
        }
        return -1;
    }

    public static void saveMove(int game_id,String side, String pieceName, int fRow, int fCol, int tRow, int tCol) {
        new Thread(() -> {
            String sql = "INSERT INTO moves (game_id,side, piece_name, from_row, from_col, to_row, to_col) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1,game_id);
                pstmt.setString(2, side);
                pstmt.setString(3, pieceName); // 存入棋子名称
                pstmt.setInt(4, fRow);
                pstmt.setInt(5, fCol);
                pstmt.setInt(6, tRow);
                pstmt.setInt(7, tCol);
                
                pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}