import java.sql.*;
import java.util.Scanner;
import java.util.*;

public class TechnicianUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "201407";

    public void printUserMenu(){
        System.out.println("============= 维修人员界面 ============");
        System.out.println("1. 查看账户信息");
        System.out.println("2. 记录材料消耗");
        System.out.println("3. 查看并处理待确认工单");
        System.out.println("4. 录入反馈，更新进展");
        System.out.println("5. 查询历史维修记录与工时费收入");
        System.out.println("6. 结束维修");
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
                    System.out.println("查询出错");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printInfo(int idNUM) {
        String sql = "SELECT name , skill , hourlyRate FROM technician WHERE technicianID = ? ";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,idNUM);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String skill = rs.getString("skill");
                    Float rate = rs.getFloat("hourlyRate");
                    System.out.println("您是维修人员:"+name+"；工种："+skill+"；时薪："+rate);
                    System.out.println(" ");
                } else {
                    System.out.println("查询出错，有bug");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * 查询技师所有待确认的工单
     */
    public List<WorkOrder> getPendingConfirmWorkOrders(int technicianID) {
        List<WorkOrder> pendingOrders = new ArrayList<>();
        String sql = "SELECT w.orderID, w.description FROM workorder w " +
                "JOIN record r ON w.orderID = r.orderID " +
                "WHERE r.technicianID = ? AND w.assignment = '待确认' AND r.PJstatus = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, technicianID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WorkOrder order = new WorkOrder();
                    order.setOrderID(rs.getInt("orderID"));
                    order.setDescription(rs.getString("description"));
                    pendingOrders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingOrders;
    }

    /**
     * 技师接受工单
     */
    public boolean acceptWorkOrder(int orderID, int technicianID) {
        String updateOrderSql = "UPDATE workorder SET assignment = '已分配' WHERE orderID = ? AND assignment = '待确认'";
        String updateRecordSql = "UPDATE record SET PJstatus = '已同意', updateTime = NOW() WHERE orderID = ? AND technicianID = ? AND PJstatus = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql);
                 PreparedStatement psRecord = conn.prepareStatement(updateRecordSql)) {
                psOrder.setInt(1, orderID);
                psRecord.setInt(1, orderID);
                psRecord.setInt(2, technicianID);

                int rowsOrder = psOrder.executeUpdate();
                int rowsRecord = psRecord.executeUpdate();

                if (rowsOrder == 1 && rowsRecord == 1) {
                    conn.commit();
                    System.out.println("工单 " + orderID + " 已被接受");
                    return true;
                } else {
                    conn.rollback();
                    System.out.println("接受工单失败：未找到对应记录或状态不正确");
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 技师拒绝工单
     */
    public boolean rejectWorkOrder(int orderID, int technicianID) {
        String updateOrderSql = "UPDATE workorder SET assignment = '已拒绝' WHERE orderID = ? AND assignment = '待确认'";
        String updateRecordSql = "UPDATE record SET PJstatus = '已拒绝', updateTime = NOW() WHERE orderID = ? AND technicianID = ? AND PJstatus = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql);
                 PreparedStatement psRecord = conn.prepareStatement(updateRecordSql)) {
                psOrder.setInt(1, orderID);
                psRecord.setInt(1, orderID);
                psRecord.setInt(2, technicianID);

                int rowsOrder = psOrder.executeUpdate();
                int rowsRecord = psRecord.executeUpdate();

                if (rowsOrder == 1 && rowsRecord == 1) {
                    conn.commit();
                    System.out.println("工单 " + orderID + " 已被拒绝，系统将重新分配");

                    // 调用已有的重新分配逻辑
                    WorkOrderUtils workOrderUtils = new WorkOrderUtils();
                    boolean reassignSuccess = workOrderUtils.assignWorkOrderRandomly(orderID);
                    if (!reassignSuccess) {
                        System.out.println("重新分配失败，请稍后手动处理");
                    }

                    return true;
                } else {
                    conn.rollback();
                    System.out.println("拒绝工单失败：未找到对应记录或状态不正确");
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //记录材料消耗功能
    public void recordMaterialUsage(Scanner scanner, int technicianID) {
        System.out.println("=== 记录材料消耗 ===");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // 先让用户输入维修记录ID，确认是自己的维修记录
            System.out.print("请输入维修记录ID（recordID）：");
            int recordID = Integer.parseInt(scanner.nextLine());

            // 验证维修记录是否属于该技师
            String verifySql = "SELECT recordID FROM record WHERE recordID = ? AND technicianID = ?";
            try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                ps.setInt(1, recordID);
                ps.setInt(2, technicianID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("该维修记录不存在或不属于您！");
                        return;
                    }
                }
            }

            // 录入材料信息
            System.out.print("请输入材料名称（如M1, M2, M3）：");
            String materialName = scanner.nextLine();
            System.out.print("请输入材料数量：");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("请输入材料单价（元）：");
            double unitPrice = Double.parseDouble(scanner.nextLine());

            // 计算总价，仅用于提示
            double totalPrice = quantity * unitPrice;

            // 插入语句去掉totalPrice字段，让数据库自动计算
            String insertSql = "INSERT INTO Material(recordID, materialName, quantity, unitPrice) VALUES (?, ?, ?, ?)";

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, recordID);
                insertPs.setString(2, materialName);
                insertPs.setInt(3, quantity);
                insertPs.setDouble(4, unitPrice);

                int rows = insertPs.executeUpdate();
                if (rows == 1) {
                    System.out.println("材料消耗记录成功录入，消耗总价：" + totalPrice + " 元");
                } else {
                    System.out.println("材料消耗记录录入失败");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库操作异常");
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入有效数字");
        }
    }


    //录入反馈，维修进度更新和结果更新
    public void insertFeedback(Scanner scanner, int technicianID) {
        System.out.println("=== 录入反馈信息 ===");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.print("请输入工单ID（orderID）：");
            int orderID = Integer.parseInt(scanner.nextLine());

            // 1. 根据 orderID 查询 userID （从 workorder 表）
            String getUserSql = "SELECT userID FROM workorder WHERE orderID = ?";
            Integer userID = null;
            try (PreparedStatement psUser = conn.prepareStatement(getUserSql)) {
                psUser.setInt(1, orderID);
                try (ResultSet rs = psUser.executeQuery()) {
                    if (rs.next()) {
                        userID = rs.getInt("userID");
                    } else {
                        System.out.println("该工单不存在！");
                        return;
                    }
                }
            }

            // 2. 验证该工单是否属于该技师负责（record表中存在且technicianID匹配）
            String verifySql = "SELECT 1 FROM record WHERE orderID = ? AND technicianID = ?";
            try (PreparedStatement psVerify = conn.prepareStatement(verifySql)) {
                psVerify.setInt(1, orderID);
                psVerify.setInt(2, technicianID);
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("该工单不属于您，不能反馈");
                        return;
                    }
                }
            }

            // 3. 输入反馈信息
            System.out.print("请输入反馈类型（例如：进度更新、维修结果）：");
            String type = scanner.nextLine();

            System.out.print("请输入反馈内容：");
            String content = scanner.nextLine();

            // 4. 插入反馈
            String insertSql = "INSERT INTO FeedBack(orderID, userID, technicianID, type, content, feedbackTime) " +
                    "VALUES (?, ?, ?, ?, ?, NOW())";

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, orderID);
                psInsert.setInt(2, userID);
                psInsert.setInt(3, technicianID);
                psInsert.setString(4, type);
                psInsert.setString(5, content);

                int rows = psInsert.executeUpdate();
                if (rows == 1) {
                    System.out.println("反馈信息录入成功");
                } else {
                    System.out.println("反馈录入失败");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库操作异常");
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入有效数字");
        }
    }



    //历史维修记录附加反馈内容完成查询要求
    public void queryHistoricalRecordsAndIncomeWithFeedback(int technicianID) {
        System.out.println("=== 查询历史维修记录、工时费及反馈 ===");

        // 将 r.workHours 改为 r.hours
        String sqlRecord = "SELECT r.recordID, r.orderID, r.hours, t.hourlyRate " +
                "FROM record r JOIN technician t ON r.technicianID = t.technicianID " +
                "WHERE r.technicianID = ?";

        String sqlFeedback = "SELECT type, content, feedbackTime FROM FeedBack WHERE orderID = ? AND technicianID = ? ORDER BY feedbackTime";

        double totalIncome = 0.0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psRecord = conn.prepareStatement(sqlRecord);
             PreparedStatement psFeedback = conn.prepareStatement(sqlFeedback)) {

            psRecord.setInt(1, technicianID);
            try (ResultSet rsRecord = psRecord.executeQuery()) {
                while (rsRecord.next()) {
                    int recordID = rsRecord.getInt("recordID");
                    int orderID = rsRecord.getInt("orderID");
                    double hours = rsRecord.getDouble("hours"); // 修改这里字段名
                    double hourlyRate = rsRecord.getDouble("hourlyRate");
                    double income = hours * hourlyRate;
                    totalIncome += income;

                    System.out.println("---------------------------------------");
                    System.out.printf("维修记录ID: %d, 工单ID: %d, 工时: %.2f, 时薪: %.2f, 工时费收入: %.2f\n",
                            recordID, orderID, hours, hourlyRate, income);

                    // 查询该工单反馈
                    psFeedback.setInt(1, orderID);
                    psFeedback.setInt(2, technicianID);
                    try (ResultSet rsFeedback = psFeedback.executeQuery()) {
                        while (rsFeedback.next()) {
                            String type = rsFeedback.getString("type");
                            String content = rsFeedback.getString("content");
                            Timestamp time = rsFeedback.getTimestamp("feedbackTime");
                            System.out.printf("  反馈类型: %s, 时间: %s\n    内容: %s\n",
                                    type, time.toString(), content);
                        }
                    }
                    System.out.println("---------------------------------------");
                }
                System.out.printf("总工时费收入：%.2f 元\n", totalIncome);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库操作异常");
        }
    }


    //结束订单
    public void finishRepair(Scanner scanner, int technicianID) {
        System.out.println("=== 结束维修 ===");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);  // 开启事务
            try {
                System.out.print("请输入要结束的工单ID（orderID）：");
                int orderID = Integer.parseInt(scanner.nextLine());

                // 1. 验证该工单是否属于该技师且PJstatus='已同意'
                String verifyRecordSql = "SELECT recordID, updateTime FROM record WHERE orderID = ? AND technicianID = ? AND PJstatus = '已同意'";
                int recordID = -1;
                Timestamp updateTime = null;

                try (PreparedStatement psVerify = conn.prepareStatement(verifyRecordSql)) {
                    psVerify.setInt(1, orderID);
                    psVerify.setInt(2, technicianID);
                    try (ResultSet rs = psVerify.executeQuery()) {
                        if (rs.next()) {
                            recordID = rs.getInt("recordID");
                            updateTime = rs.getTimestamp("updateTime");
                        } else {
                            System.out.println("未找到属于您的维修记录，或维修记录状态不符（需为已同意）");
                            conn.rollback();
                            return;
                        }
                    }
                }

                // 2. 更新 workorder.assignment = '修理完毕'
                String updateAssignmentSql = "UPDATE workorder SET assignment = '修理完毕' WHERE orderID = ?";
                try (PreparedStatement psUpdateAssignment = conn.prepareStatement(updateAssignmentSql)) {
                    psUpdateAssignment.setInt(1, orderID);
                    int rows = psUpdateAssignment.executeUpdate();
                    if (rows == 0) {
                        System.out.println("工单不存在，无法更新状态");
                        conn.rollback();
                        return;
                    }
                }

                // 3：更新vehicle表中对应车辆的status为1
                int vehicleID = -1;
                String getVehicleIdSql = "SELECT vehicleID FROM workorder WHERE orderID = ?";
                try (PreparedStatement psVehicleID = conn.prepareStatement(getVehicleIdSql)) {
                    psVehicleID.setInt(1, orderID);
                    try (ResultSet rs = psVehicleID.executeQuery()) {
                        if (rs.next()) {
                            vehicleID = rs.getInt("vehicleID");
                        } else {
                            System.out.println("未找到对应工单，无法获取车辆信息");
                            conn.rollback();
                            return;
                        }
                    }
                }
                String updateVehicleStatusSql = "UPDATE vehicle SET status = 1 WHERE vehicleID = ?";
                try (PreparedStatement psUpdateVehicle = conn.prepareStatement(updateVehicleStatusSql)) {
                    psUpdateVehicle.setInt(1, vehicleID);
                    int rows = psUpdateVehicle.executeUpdate();
                    if (rows == 0) {
                        System.out.println("车辆信息不存在，无法更新状态");
                        conn.rollback();
                        return;
                    }
                }

                // 4. 计算维修用时（小时，按规则）
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long diffMillis = now.getTime() - updateTime.getTime();
                double diffHoursRaw = diffMillis / (1000.0 * 60 * 60);
                int workHours = (int) Math.ceil(diffHoursRaw < 0.5 ? 1 : diffHoursRaw); // 不足30分钟算1小时，否则向上取整

                // 5. 查询技师时薪（字段hourlyRate）
                String getWageSql = "SELECT hourlyRate FROM technician WHERE technicianID = ?";
                double hourlyRate = 0.0;
                try (PreparedStatement psWage = conn.prepareStatement(getWageSql)) {
                    psWage.setInt(1, technicianID);
                    try (ResultSet rs = psWage.executeQuery()) {
                        if (rs.next()) {
                            hourlyRate = rs.getDouble("hourlyRate");
                        } else {
                            System.out.println("未找到对应技师信息");
                            conn.rollback();
                            return;
                        }
                    }
                }

                // 6. 计算人工费用
                double laborCost = hourlyRate * workHours;

                // 7. 查询材料费用总和
                String getMaterialCostSql = "SELECT IFNULL(SUM(totalPrice), 0) AS materialCost FROM material WHERE recordID = ?";
                double materialCost = 0.0;
                try (PreparedStatement psMaterial = conn.prepareStatement(getMaterialCostSql)) {
                    psMaterial.setInt(1, recordID);
                    try (ResultSet rs = psMaterial.executeQuery()) {
                        if (rs.next()) {
                            materialCost = rs.getDouble("materialCost");
                        }
                    }
                }

                // 8. 更新 record 表中的 laborCost 和 hours
                String updateRecordSql = "UPDATE record SET laborCost = ?, hours = ? WHERE recordID = ?";
                try (PreparedStatement psUpdateRecord = conn.prepareStatement(updateRecordSql)) {
                    psUpdateRecord.setDouble(1, laborCost);
                    psUpdateRecord.setInt(2, workHours);
                    psUpdateRecord.setInt(3, recordID);
                    psUpdateRecord.executeUpdate();
                }

                // 9. 更新 workorder 表中的 totalCost = laborCost + materialCost
                String updateCostSql = "UPDATE workorder SET totalCost = ? WHERE orderID = ?";
                try (PreparedStatement psUpdateCost = conn.prepareStatement(updateCostSql)) {
                    psUpdateCost.setDouble(1, laborCost + materialCost);
                    psUpdateCost.setInt(2, orderID);
                    psUpdateCost.executeUpdate();
                }



                conn.commit();

                // 10. 打印人工费和材料费
                System.out.printf("人工费用：%.2f 元，维修用时：%d 小时%n", laborCost, workHours);
                System.out.printf("材料费用：%.2f 元%n", materialCost);
                System.out.println("维修工单已成功标记为修理完毕");

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                System.out.println("操作失败，已回滚");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库操作异常");
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入有效数字");
        }
    }







}
