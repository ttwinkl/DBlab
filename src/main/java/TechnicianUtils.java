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
        System.out.println("2. 功能2");
        System.out.println("3. 查看并处理待确认工单");
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

                    // 调用您已有的重新分配逻辑
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



}
