import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "568923";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // 加载驱动
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch(Exception ignored){}
        try { if (stmt != null) stmt.close(); } catch(Exception ignored){}
        try { if (conn != null) conn.close(); } catch(Exception ignored){}
    }
}
