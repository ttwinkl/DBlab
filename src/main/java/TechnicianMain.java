import java.util.Scanner;
import java.sql.*;
import java.util.*;
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
                    tech.printUserInfo(idNUM);
                    while(MenuFlag) {
                        if (idNUM != -1) {
                            tech.printUserMenu();
                            System.out.print("请输入您的选择（数字）：");
                            String userInput = scanner.nextLine().trim();
                            switch (userInput) {
                                case "1":
                                    tech.printInfo(idNUM);
                                    break;
                                case "2":
                                    break;
                                case "3":
                                    handlePendingOrders(scanner, tech, idNUM);  // 这里调用处理方法
                                    break;
                                case "0":
                                    System.out.print("退出程序，感谢使用！");
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
    // 处理待确认工单，允许接受或拒绝
    private static void handlePendingOrders(Scanner scanner, TechnicianUtils tech, int technicianID) {
        List<WorkOrder> pendingOrders = tech.getPendingConfirmWorkOrders(technicianID);
        if (pendingOrders.isEmpty()) {
            System.out.println("无待确认工单");
            return;
        }

        System.out.println("===== 待确认工单列表 =====");
        for (WorkOrder order : pendingOrders) {
            System.out.println("工单ID: " + order.getOrderID() + "，描述: " + order.getDescription());
        }
        System.out.println("=========================");

        while (true) {
            System.out.print("请输入要操作的工单ID，或输入0返回上一级菜单：");
            String input = scanner.nextLine().trim();
            int orderID;
            try {
                orderID = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字！");
                continue;
            }

            if (orderID == 0) {
                break;  // 返回上级菜单
            }

            boolean validOrder = pendingOrders.stream().anyMatch(o -> o.getOrderID() == orderID);
            if (!validOrder) {
                System.out.println("工单ID不存在或不在待确认列表中，请重新输入");
                continue;
            }

            System.out.print("输入1接受工单，2拒绝工单，0取消操作：");
            String action = scanner.nextLine().trim();

            switch (action) {
                case "1":
                    boolean accepted = tech.acceptWorkOrder(orderID, technicianID);
                    if (accepted) {
                        System.out.println("工单已接受");
                    } else {
                        System.out.println("接受工单失败，请稍后重试");
                    }
                    return; // 操作完成返回
                case "2":
                    boolean rejected = tech.rejectWorkOrder(orderID, technicianID);
                    if (rejected) {
                        System.out.println("工单已拒绝，系统将重新分配");
                    } else {
                        System.out.println("拒绝工单失败，请稍后重试");
                    }
                    return; // 操作完成返回
                case "0":
                    System.out.println("取消操作");
                    return;
                default:
                    System.out.println("无效输入，请重新选择");
            }
        }
    }
}
