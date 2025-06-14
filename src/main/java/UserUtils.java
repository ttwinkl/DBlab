import java.sql.*;
import java.util.Scanner;

public class UserUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "568923";

    WorkOrderUtils workOrderUtils = new WorkOrderUtils();

    public void printUserMenu(){
        System.out.println("============= 用户登录后界面 ============");
        System.out.println("1. 查看您的车辆");
        System.out.println("2. 为您的车辆提交订单");
        System.out.println("3. 查询记录在您账户下的订单");
        System.out.println("4. 登记您的车辆");
        System.out.println("0. 退出");
        System.out.println("=======================================");
    }

    public void printUserInfo(int useridNUM) {
        String sql = "SELECT username FROM users WHERE userID = ? ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,useridNUM);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    System.out.println("欢迎回来，用户名： "+ username);
                } else {
                    System.out.println("查询出错");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printUVehicleInfo(int userInput) {
        String sql = "SELECT vehicleID, license, status FROM vehicle WHERE userID = ? ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,userInput);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasVehicle = false;
                while (rs.next()) {  // 用while循环遍历所有车辆
                    hasVehicle = true;
                    String vehicleID = rs.getString("vehicleID");
                    String license = rs.getString("license");
                    boolean status = rs.getBoolean("status");
                    String statusDetail = status ? "不需要修理" : "需要修理";

                    System.out.println("您本车的ID是: " + vehicleID);
                    System.out.println("牌号: " + license);
                    System.out.println("状态: " + statusDetail);
                    System.out.println("-----------------------");
                }
                if (!hasVehicle) {
                    System.out.println("该用户没有车辆信息。");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canCreateWorkOrder(int userID, int vehicleID) {
        String sqlVehicle = "SELECT status FROM vehicle WHERE vehicleID = ? AND userID = ?";
        // 用 orderID 代替 createTime，降序取最新工单
        String sqlLatestWorkOrder = "SELECT assignment FROM workorder WHERE vehicleID = ? AND userID = ? ORDER BY orderID DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // 1. 检查车辆状态，假设status==0表示正常
            try (PreparedStatement psVehicle = conn.prepareStatement(sqlVehicle)) {
                psVehicle.setInt(1, vehicleID);
                psVehicle.setInt(2, userID);
                try (ResultSet rs = psVehicle.executeQuery()) {
                    if (rs.next()) {
                        int status = rs.getInt("status");
                        if (status != 0) {
                            System.out.println("车辆状态不允许维修");
                            return false;
                        }
                    } else {
                        System.out.println("车辆不存在或不属于该用户");
                        return false;
                    }
                }
            }

            // 2. 查询该车辆最新工单状态
            try (PreparedStatement psWorkOrder = conn.prepareStatement(sqlLatestWorkOrder)) {
                psWorkOrder.setInt(1, vehicleID);
                psWorkOrder.setInt(2, userID);
                try (ResultSet rs = psWorkOrder.executeQuery()) {
                    if (rs.next()) {
                        String assignment = rs.getString("assignment");
                        System.out.println("最新工单状态：" + assignment);

                        // 只有修理完毕才能新建工单
                        if (!"修理完毕".equals(assignment)) {
                            System.out.println("车辆之前的工单尚未完成，不能创建新的工单");
                            return false;
                        }
                    } else {
                        // 之前无工单，允许新建
                        System.out.println("该车辆暂无工单，允许创建新工单");
                        return true;
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public boolean submitOrder(int userID, int vehicleID, String description){
        // 先判断车辆是否允许创建工单
        if (!canCreateWorkOrder(userID, vehicleID)) {
            System.out.println("车辆状态不允许创建维修工单。");
            return false;
        }

        // 新工单初始assignment状态为“待分配”，totalCost初始为0
        String insertSql = "INSERT INTO workOrder (userID, vehicleID, assignment, totalCost, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, vehicleID);
            stmt.setString(3, "待分配");   // 这里用字符串，若是枚举或数字请替换
            stmt.setDouble(4, 0.0);       // 新工单总费用为0
            stmt.setString(5, description);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("维修工单提交成功。");
                return true;
            } else {
                System.out.println("维修工单提交失败。");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void printAllOrder(int userID){
        String sql = "SELECT orderID,vehicleID, assignment, totalCost,description FROM workorder WHERE userID = ? ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,userID);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasVehicle = false;
                while (rs.next()) {  // 用while循环遍历所有车辆
                    hasVehicle = true;
                    int orderID = rs.getInt("orderID");
                    String vehicleID = rs.getString("vehicleID");
                    String assignment = rs.getString("assignment");
                    int totalCost = rs.getInt("totalCost");
                    String description =  rs.getString("description");

                    System.out.println("-----------------------");
                    System.out.println("您的订单号是:" + orderID);
                    System.out.println("您该订单维修车的ID是: " + vehicleID);
                    System.out.println("分配状态: " + assignment);
                    System.out.println("总花销: " + totalCost);
                    System.out.println("维修种类：" + description);
                    System.out.println("-----------------------");
                }
                if (!hasVehicle) {
                    System.out.println("该用户没有车辆信息。");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void assignToRecord(int userID, int vehicleID) {
        String sql = "SELECT orderID FROM workorder WHERE userID = ? AND vehicleID = ? AND assignment != '修理完毕' ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, vehicleID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {   // 一定要先调用next()
                    int orderID = rs.getInt("orderID");
                    if (workOrderUtils.assignWorkOrderRandomly(orderID)) {
                        System.out.println("-----------------------");
                        System.out.println("分配成功");
                        System.out.println("-----------------------");
                    } else {
                        System.out.println("-----------------------");
                        System.out.println("分配失败");
                        System.out.println("-----------------------");
                    }
                } else {
                    System.out.println("没有找到对应的工单");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registerVehicle(int userID, String license, boolean status) {
        String insertSql = "INSERT INTO vehicle (userID, license, status) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, userID);
            stmt.setString(2, license);
            stmt.setBoolean(3, status);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("车辆登记成功。");
                return true;
            } else {
                System.out.println("车辆登记失败。");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
