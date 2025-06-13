import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkOrderUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "201407";

    public boolean assignWorkOrderRandomly(int orderID) {
        String checkOrderSql = "SELECT assignment, description FROM workOrder WHERE orderID = ?";
        String findTechsSql = "SELECT technicianID FROM technician WHERE skill = ?";
        String updateOrderSql = "UPDATE workOrder SET assignment = ? WHERE orderID = ?";
        String insertRecordSql = "INSERT INTO record (orderID, technicianID, PJstatus) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String assignmentStatus = null;
            String orderDescription = null;
            try (PreparedStatement ps = conn.prepareStatement(checkOrderSql)) {
                ps.setInt(1, orderID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignmentStatus = rs.getString("assignment");
                        orderDescription = rs.getString("description");
                    } else {
                        System.out.println("工单ID不存在");
                        return false;
                    }
                }
            }

            if (!"待分配".equals(assignmentStatus) && !"已拒绝".equals(assignmentStatus)) {
                System.out.println("工单状态不是待分配或已拒绝，不能进行分配");
                return false;
            }

            if (orderDescription == null || orderDescription.trim().isEmpty()) {
                System.out.println("工单描述为空，无法匹配维修人员技能");
                return false;
            }

            List<Integer> candidateTechnicians = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(findTechsSql)) {
                ps.setString(1, orderDescription);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        candidateTechnicians.add(rs.getInt("technicianID"));
                    }
                }
            }
            if (candidateTechnicians.isEmpty()) {
                System.out.println("没有技能匹配的维修人员，无法分配");
                return false;
            }

            Random rand = new Random();
            int selectedTechID = candidateTechnicians.get(rand.nextInt(candidateTechnicians.size()));

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psUpdate = conn.prepareStatement(updateOrderSql)) {
                    psUpdate.setString(1, "待确认");
                    psUpdate.setInt(2, orderID);
                    int updatedRows = psUpdate.executeUpdate();
                    if (updatedRows == 0) {
                        System.out.println("更新工单状态失败");
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement psInsert = conn.prepareStatement(insertRecordSql)) {
                    psInsert.setInt(1, orderID);
                    psInsert.setInt(2, selectedTechID);
                    psInsert.setString(3, "待确认");
                    int insertedRows = psInsert.executeUpdate();
                    if (insertedRows == 0) {
                        System.out.println("插入维修记录失败");
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                System.out.println("工单 " + orderID + " 已成功分配给维修人员ID：" + selectedTechID);
                return true;

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

    public List<WorkOrder> getPendingConfirmWorkOrders(int technicianID) {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT w.orderID, w.description FROM workOrder w " +
                "JOIN record r ON w.orderID = r.orderID " +
                "WHERE r.technicianID = ? AND r.PJstatus = '待确认' AND w.assignment = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, technicianID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderID = rs.getInt("orderID");
                    String description = rs.getString("description");
                    orders.add(new WorkOrder(orderID, description));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean acceptWorkOrder(int orderID, int technicianID) {
        String checkSql = "SELECT assignment FROM workOrder WHERE orderID = ?";
        String updateOrderSql = "UPDATE workOrder SET assignment = '已分配' WHERE orderID = ?";
        String updateRecordSql = "UPDATE record SET PJstatus = '已接受' WHERE orderID = ? AND technicianID = ? AND PJstatus = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, orderID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"待确认".equals(rs.getString("assignment"))) {
                            System.out.println("工单状态不是待确认，无法接受");
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                    ps.setInt(1, orderID);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateRecordSql)) {
                    ps.setInt(1, orderID);
                    ps.setInt(2, technicianID);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        System.out.println("未找到对应的待确认分配记录");
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

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

    public boolean rejectWorkOrder(int orderID, int technicianID) {
        String checkSql = "SELECT assignment FROM workOrder WHERE orderID = ?";
        String updateOrderSql = "UPDATE workOrder SET assignment = '待分配' WHERE orderID = ?";
        String updateRecordSql = "UPDATE record SET PJstatus = '已拒绝' WHERE orderID = ? AND technicianID = ? AND PJstatus = '待确认'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, orderID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"待确认".equals(rs.getString("assignment"))) {
                            System.out.println("工单状态不是待确认，无法拒绝");
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                    ps.setInt(1, orderID);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateRecordSql)) {
                    ps.setInt(1, orderID);
                    ps.setInt(2, technicianID);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        System.out.println("未找到对应的待确认分配记录");
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
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

        return assignWorkOrderRandomly(orderID);
    }
}
