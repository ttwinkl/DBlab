import java.sql.*;
import java.util.Scanner;

public class AdminMain {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "568923";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean menuFlag = true;
        while (menuFlag) {
            printMenu();
            System.out.print("请输入您的选择（数字）：");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    manageUsers(scanner);
                    break;
                case "2":
                    manageTechnicians(scanner);
                    break;
                case "3":
                    manageVehicles(scanner);
                    break;
                case "4":
                    manageWorkOrders(scanner);
                    break;
                case "5":
                    dataStatistics();
                    break;
                case "0":
                    System.out.println("退出程序，感谢使用！");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效输入，请重新输入！");
            }
            System.out.println();
        }
    }

    public static void printMenu() {
        System.out.println("============= 管理员界面 =============");
        System.out.println("1. 管理用户信息");
        System.out.println("2. 管理维修人员信息");
        System.out.println("3. 管理车辆信息");
        System.out.println("4. 管理维修工单");
        System.out.println("5. 数据统计");
        System.out.println("0. 退出");
        System.out.println("=======================================");
    }

    public static void manageUsers(Scanner scanner) {
        System.out.println("=== 管理用户信息 ===");
        System.out.println("1. 查看用户信息");
        System.out.println("2. 添加用户信息");
        System.out.println("3. 修改用户信息");
        System.out.println("4. 删除用户信息");
        System.out.print("请输入您的选择（数字）：");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewUsers();
                break;
            case "2":
                addUser(scanner);
                break;
            case "3":
                updateUser(scanner);
                break;
            case "4":
                deleteUser(scanner);
                break;
            default:
                System.out.println("无效输入，请重新输入！");
        }
    }

    public static void viewUsers() {
        String sql = "SELECT * FROM users";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int userID = rs.getInt("userID");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String phone = rs.getString("phone");
                System.out.println("用户 ID: " + userID + ", 用户名: " + username + ", 密码: " + password + ", 手机号: " + phone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(Scanner scanner) {
        System.out.print("请输入用户名: ");
        String username = scanner.nextLine();
        System.out.print("请输入密码: ");
        String password = scanner.nextLine();
        System.out.print("请输入手机号: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO users (username, password, phone) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, phone);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("用户添加成功！");
            } else {
                System.out.println("用户添加失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(Scanner scanner) {
        System.out.print("请输入要修改的用户 ID: ");
        int userID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的用户名: ");
        String username = scanner.nextLine();
        System.out.print("请输入新的密码: ");
        String password = scanner.nextLine();
        System.out.print("请输入新的手机号: ");
        String phone = scanner.nextLine();

        String sql = "UPDATE users SET username = ?, password = ?, phone = ? WHERE userID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, phone);
            stmt.setInt(4, userID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("用户信息修改成功！");
            } else {
                System.out.println("用户信息修改失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(Scanner scanner) {
        System.out.print("请输入要删除的用户 ID: ");
        int userID = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM users WHERE userID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("用户删除成功！");
            } else {
                System.out.println("用户删除失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageTechnicians(Scanner scanner) {
        System.out.println("=== 管理维修人员信息 ===");
        System.out.println("1. 查看维修人员信息");
        System.out.println("2. 添加维修人员信息");
        System.out.println("3. 修改维修人员信息");
        System.out.println("4. 删除维修人员信息");
        System.out.print("请输入您的选择（数字）：");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewTechnicians();
                break;
            case "2":
                addTechnician(scanner);
                break;
            case "3":
                updateTechnician(scanner);
                break;
            case "4":
                deleteTechnician(scanner);
                break;
            default:
                System.out.println("无效输入，请重新输入！");
        }
    }

    public static void viewTechnicians() {
        String sql = "SELECT * FROM technician";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int technicianID = rs.getInt("technicianID");
                String name = rs.getString("name");
                String skill = rs.getString("skill");
                float hourlyRate = rs.getFloat("hourlyRate");
                System.out.println("维修人员 ID: " + technicianID + ", 姓名: " + name + ", 技能: " + skill + ", 时薪: " + hourlyRate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addTechnician(Scanner scanner) {
        System.out.print("请输入维修人员姓名: ");
        String name = scanner.nextLine();
        System.out.print("请输入维修人员技能: ");
        String skill = scanner.nextLine();
        System.out.print("请输入维修人员时薪: ");
        float hourlyRate = Float.parseFloat(scanner.nextLine());

        String sql = "INSERT INTO technician (name, skill, hourlyRate) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, skill);
            stmt.setFloat(3, hourlyRate);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("维修人员添加成功！");
            } else {
                System.out.println("维修人员添加失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTechnician(Scanner scanner) {
        System.out.print("请输入要修改的维修人员 ID: ");
        int technicianID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的姓名: ");
        String name = scanner.nextLine();
        System.out.print("请输入新的技能: ");
        String skill = scanner.nextLine();
        System.out.print("请输入新的时薪: ");
        float hourlyRate = Float.parseFloat(scanner.nextLine());

        String sql = "UPDATE technician SET name = ?, skill = ?, hourlyRate = ? WHERE technicianID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, skill);
            stmt.setFloat(3, hourlyRate);
            stmt.setInt(4, technicianID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("维修人员信息修改成功！");
            } else {
                System.out.println("维修人员信息修改失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTechnician(Scanner scanner) {
        System.out.print("请输入要删除的维修人员 ID: ");
        int technicianID = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM technician WHERE technicianID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, technicianID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("维修人员删除成功！");
            } else {
                System.out.println("维修人员删除失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageVehicles(Scanner scanner) {
        System.out.println("=== 管理车辆信息 ===");
        System.out.println("1. 查看车辆信息");
        System.out.println("2. 添加车辆信息");
        System.out.println("3. 修改车辆信息");
        System.out.println("4. 删除车辆信息");
        System.out.print("请输入您的选择（数字）：");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewVehicles();
                break;
            case "2":
                addVehicle(scanner);
                break;
            case "3":
                updateVehicle(scanner);
                break;
            case "4":
                deleteVehicle(scanner);
                break;
            default:
                System.out.println("无效输入，请重新输入！");
        }
    }

    public static void viewVehicles() {
        String sql = "SELECT * FROM vehicle";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int vehicleID = rs.getInt("vehicleID");
                int userID = rs.getInt("userID");
                String license = rs.getString("license");
                boolean status = rs.getBoolean("status");
                System.out.println("车辆 ID: " + vehicleID + ", 用户 ID: " + userID + ", 车牌号: " + license + ", 状态: " + (status ? "不需要修理" : "需要修理"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addVehicle(Scanner scanner) {
        System.out.print("请输入用户 ID: ");
        int userID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入车牌号: ");
        String license = scanner.nextLine();
        System.out.print("请输入车辆状态（0: 需要修理，1: 不需要修理）: ");
        boolean status = scanner.nextLine().equals("1");

        String sql = "INSERT INTO vehicle (userID, license, status) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, license);
            stmt.setBoolean(3, status);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("车辆添加成功！");
            } else {
                System.out.println("车辆添加失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateVehicle(Scanner scanner) {
        System.out.print("请输入要修改的车辆 ID: ");
        int vehicleID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的用户 ID: ");
        int userID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的车牌号: ");
        String license = scanner.nextLine();
        System.out.print("请输入新的车辆状态（0: 需要修理，1: 不需要修理）: ");
        boolean status = scanner.nextLine().equals("1");

        String sql = "UPDATE vehicle SET userID = ?, license = ?, status = ? WHERE vehicleID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, license);
            stmt.setBoolean(3, status);
            stmt.setInt(4, vehicleID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("车辆信息修改成功！");
            } else {
                System.out.println("车辆信息修改失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteVehicle(Scanner scanner) {
        System.out.print("请输入要删除的车辆 ID: ");
        int vehicleID = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM vehicle WHERE vehicleID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicleID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("车辆删除成功！");
            } else {
                System.out.println("车辆删除失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageWorkOrders(Scanner scanner) {
        System.out.println("=== 管理维修工单 ===");
        System.out.println("1. 查看维修工单信息");
        System.out.println("2. 修改维修工单信息");
        System.out.println("3. 分配维修工单");
        System.out.println("4. 删除维修工单");
        System.out.print("请输入您的选择（数字）：");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                viewWorkOrders();
                break;
            case "2":
                updateWorkOrder(scanner);
                break;
            case "3":
                assignWorkOrder(scanner);
                break;
            case "4":
                deleteWorkOrder(scanner);
                break;
            default:
                System.out.println("无效输入，请重新输入！");
        }
    }

    public static void viewWorkOrders() {
        String sql = "SELECT * FROM workorder";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int orderID = rs.getInt("orderID");
                int userID = rs.getInt("userID");
                int vehicleID = rs.getInt("vehicleID");
                String assignment = rs.getString("assignment");
                double totalCost = rs.getDouble("totalCost");
                String description = rs.getString("description");
                System.out.println("工单 ID: " + orderID + ", 用户 ID: " + userID + ", 车辆 ID: " + vehicleID + ", 分配状态: " + assignment + ", 总费用: " + totalCost + ", 描述: " + description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateWorkOrder(Scanner scanner) {
        System.out.print("请输入要修改的工单 ID: ");
        int orderID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的用户 ID: ");
        int userID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的车辆 ID: ");
        int vehicleID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入新的分配状态: ");
        String assignment = scanner.nextLine();
        System.out.print("请输入新的总费用: ");
        double totalCost = Double.parseDouble(scanner.nextLine());
        System.out.print("请输入新的描述: ");
        String description = scanner.nextLine();

        String sql = "UPDATE workorder SET userID = ?, vehicleID = ?, assignment = ?, totalCost = ?, description = ? WHERE orderID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, vehicleID);
            stmt.setString(3, assignment);
            stmt.setDouble(4, totalCost);
            stmt.setString(5, description);
            stmt.setInt(6, orderID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("维修工单信息修改成功！");
            } else {
                System.out.println("维修工单信息修改失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void assignWorkOrder(Scanner scanner) {
        System.out.print("请输入要分配的工单 ID: ");
        int orderID = Integer.parseInt(scanner.nextLine());
        System.out.print("请输入要分配的维修人员 ID: ");
        int technicianID = Integer.parseInt(scanner.nextLine());

        String sql1 = "UPDATE workorder SET assignment = '待确认' WHERE orderID = ?";
        String sql2 = "INSERT INTO record (orderID, technicianID, PJstatus) VALUES (?, ?, '待确认')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt1 = conn.prepareStatement(sql1);
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
            conn.setAutoCommit(false);
            try {
                stmt1.setInt(1, orderID);
                int rows1 = stmt1.executeUpdate();
                stmt2.setInt(1, orderID);
                stmt2.setInt(2, technicianID);
                int rows2 = stmt2.executeUpdate();
                if (rows1 > 0 && rows2 > 0) {
                    conn.commit();
                    System.out.println("维修工单分配成功！");
                } else {
                    conn.rollback();
                    System.out.println("维修工单分配失败！");
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteWorkOrder(Scanner scanner) {
        System.out.print("请输入要删除的工单 ID：");
        int orderID = Integer.parseInt(scanner.nextLine().trim());

        String sql = "DELETE FROM workorder WHERE orderID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderID);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("工单删除成功！");
            } else {
                System.out.println("未找到对应的工单，删除失败！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("删除工单时发生数据库错误！");
        }
    }

    public static void dataStatistics() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // 用户数量统计
            String userCountSql = "SELECT COUNT(*) FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(userCountSql)) {
                if (rs.next()) {
                    int userCount = rs.getInt(1);
                    System.out.println("用户数量: " + userCount);
                }
            }

            // 维修人员数量统计
            String technicianCountSql = "SELECT COUNT(*) FROM technician";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(technicianCountSql)) {
                if (rs.next()) {
                    int technicianCount = rs.getInt(1);
                    System.out.println("维修人员数量: " + technicianCount);
                }
            }

            // 车辆数量统计
            String vehicleCountSql = "SELECT COUNT(*) FROM vehicle";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(vehicleCountSql)) {
                if (rs.next()) {
                    int vehicleCount = rs.getInt(1);
                    System.out.println("车辆数量: " + vehicleCount);
                }
            }

            // 工单数量统计
            String workOrderCountSql = "SELECT COUNT(*) FROM workorder";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(workOrderCountSql)) {
                if (rs.next()) {
                    int workOrderCount = rs.getInt(1);
                    System.out.println("工单数量: " + workOrderCount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
