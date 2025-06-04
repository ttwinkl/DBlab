import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkOrderUtils {
    private static final String URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "201407";

    /**
     * 随机分配一个技能匹配工单description的维修人员给指定orderID的工单
     * @param orderID 待分配的工单ID
     * @return 是否分配成功
     */
    public boolean assignWorkOrderRandomly(int orderID) {
        String checkOrderSql = "SELECT assignment, description FROM workOrder WHERE orderID = ?";
        String findTechsSql = "SELECT technicianID FROM technician WHERE skill = ?";
        String updateOrderSql = "UPDATE workOrder SET assignment = ? WHERE orderID = ?";
        String insertRecordSql = "INSERT INTO record (orderID, technicianID) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // 1. 查询工单状态与维修种类描述
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

            if (!"待分配".equals(assignmentStatus)) {
                System.out.println("工单状态不是待分配，不能进行分配");
                return false;
            }
            if (orderDescription == null || orderDescription.trim().isEmpty()) {
                System.out.println("工单描述为空，无法匹配维修人员技能");
                return false;
            }

            // 2. 查询所有技能匹配的维修人员ID列表
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

            // 3. 随机选择一个维修人员
            Random rand = new Random();
            int selectedTechID = candidateTechnicians.get(rand.nextInt(candidateTechnicians.size()));

            // 4. 事务操作，更新工单状态并插入分配记录
            conn.setAutoCommit(false);
            try {
                // 更新工单状态为“已分配”
                try (PreparedStatement psUpdate = conn.prepareStatement(updateOrderSql)) {
                    psUpdate.setString(1, "已分配");
                    psUpdate.setInt(2, orderID);
                    int updatedRows = psUpdate.executeUpdate();
                    if (updatedRows == 0) {
                        System.out.println("更新工单状态失败");
                        conn.rollback();
                        return false;
                    }
                }

                // 插入维修记录表
                try (PreparedStatement psInsert = conn.prepareStatement(insertRecordSql)) {
                    psInsert.setInt(1, orderID);
                    psInsert.setInt(2, selectedTechID);
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
}
