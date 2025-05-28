import java.util.Scanner;
import java.sql.*;

public class TechnicianMain {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "201407";


    public static void main(String[] args) {
        TechnicianUtils tech = new TechnicianUtils();
        Scanner scanner = new Scanner(System.in);
        boolean MenuFlag = true;
        while (MenuFlag) {
            printMenu();
            System.out.print("请输入您的选择（数字）：");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    int idNUM = login(scanner);
                    if(idNUM != -1){
                        tech.printUserInfo(idNUM);
                        tech.printUserMenu();
                        System.out.print("请输入您的选择（数字）：");
                        String userInput = scanner.nextLine().trim();
                        switch (userInput){
                            case "1":
                                break;
                            case "2":
                                break;
                            case "0":
                                break;
                        }
                    }else {
                        System.out.println("登陆失败，请检查用户名或密码");
                    }
                    break;
                case "0":
                    System.out.println("退出程序，感谢使用！");
                    scanner.close();
                    return; // 退出main方法，程序结束
                default:
                    System.out.println("无效输入，请重新输入！");
            }
            System.out.println(); // 换行，提示下次输入
        }
    }

    public static void printMenu() {
        System.out.println("============= 车辆维修系统 =============");
        System.out.println("1. 维修人员登录");
        System.out.println("0. 退出");
        System.out.println("=======================================");
    }

    public static int login(Scanner scanner) {
        System.out.println("=== 维修人员登录 ===");
        System.out.print("请输入维修人员用户名: ");
        String username = scanner.nextLine();

        System.out.print("请输入维修人员密码: ");
        String password = scanner.nextLine();

        String sql = "SELECT technicianID FROM technician WHERE name = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("恭喜！登陆成功");
                    return rs.getInt("technicianID");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
