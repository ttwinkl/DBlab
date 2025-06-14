import java.util.Scanner;
import java.sql.*;

public class UserMain {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "568923";


    public static void main(String[] args) {
        WorkOrderUtils workOrderUtils = new WorkOrderUtils();
        UserUtils user = new UserUtils();
        Scanner scanner = new Scanner(System.in);
        boolean MenuFlag = true;
        while (MenuFlag) {
            printMenu();
            System.out.print("请输入您的选择（数字）：");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    registerUser(scanner);
                    break;
                case "2":
                    int useridNUM = login(scanner);
                    user.printUserInfo(useridNUM);
                    while (MenuFlag) {
                        if (useridNUM != -1) {
                            user.printUserMenu();
                            System.out.print("请输入您的选择（数字）：");
                            String userInput = scanner.nextLine().trim();
                            switch (userInput) {
                                case "1":
                                    user.printUVehicleInfo(useridNUM);
                                    break;
                                case "2":
                                    System.out.println("请输入您要提交进行修理的车辆ID（数字）");
                                    String userInputVID = scanner.nextLine().trim();
                                    int Vid = Integer.parseInt(userInputVID);
                                    System.out.println("请输入您车辆的维修类型（漆工、焊工、机修）");
                                    String inputDescription = scanner.nextLine().trim();
                                    user.submitOrder(useridNUM,Vid,inputDescription);
                                    user.assignToRecord(useridNUM,Vid);
                                    break;
                                case "3":
                                    user.printAllOrdersWithFeedback(useridNUM);
                                    break;
                                case "4":
                                    user.insertUserFeedbackByOrderID(scanner, useridNUM);
                                    break;
                                case "4": // 新增处理逻辑
                                    System.out.println("请输入车辆牌号：");
                                    String license = scanner.nextLine().trim();
                                    System.out.println("请输入车辆状态（true表示不需要修理，false表示需要修理）：");
                                    boolean status = Boolean.parseBoolean(scanner.nextLine().trim());
                                    user.registerVehicle(useridNUM, license, status);
                                    break;
                                case "0":
                                    System.out.println("退出程序，感谢使用！");
                                    MenuFlag = false;
                            }
                        } else {
                            System.out.println("登陆失败，请检查用户名或密码");
                            MenuFlag = false;
                        }
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
        System.out.println("1. 用户注册");
        System.out.println("2. 用户登录");
        System.out.println("0. 退出");
        System.out.println("=======================================");
    }

    public static boolean registerUser(Scanner scanner) {
        // 1. 引导用户输入用户名
        System.out.println("============= 用户注册 =============");
        System.out.print("请输入用户名: ");
        String username = scanner.nextLine();

        // 2. 引导用户输入密码
        System.out.print("请输入密码: ");
        String password = scanner.nextLine();

        // 3. 引导用户输入手机号
        System.out.print("请输入手机号: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO users (username, password, phone) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // 生产环境密码应加密
            stmt.setString(3, phone);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("恭喜你注册成功！");
                return true;
            } else {
                return false;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("用户名已存在！");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int login(Scanner scanner) {
        System.out.println("=== 用户登录 ===");
        System.out.print("请输入用户名: ");
        String username = scanner.nextLine();

        System.out.print("请输入密码: ");
        String password = scanner.nextLine();

        String sql = "SELECT userID FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("恭喜！登陆成功");
                    return rs.getInt("userID");
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
