import java.sql.*;
import java.util.Scanner;

public class TechnicianUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "201407";

    public void printUserMenu(){
        System.out.println("============= 维修人员界面 ============");
        System.out.println("1. 功能1");
        System.out.println("2. 功能2");
        System.out.println("3. 功能3");
        System.out.println("0. 退出");
        System.out.println("=======================================");
    }

    public void printUserInfo(int idNUM) {
        String sql = "SELECT name , skill , hourlyRate FROM technician WHERE technicianID = ? ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,idNUM);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String skill = rs.getString("skill");
                    Float rate = rs.getFloat("hourlyRate");
                    System.out.println("欢迎回来，维修人员"+name+"；工种："+skill+"；时薪："+rate);
                } else {
                    System.out.println("查询出错，有bug");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
