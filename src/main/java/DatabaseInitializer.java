import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.String;

public class DatabaseInitializer {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db1?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "568923";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // 打印指令菜单
            printMenu();

            // 获取用户输入
            System.out.print("请输入指令编号：");
            String input = scanner.nextLine();

            // 处理指令
            switch (input) {
                case "1":
                    System.out.println("程序结束。");
                    scanner.close();
                    return;
                case "2":
                    handleCreateTable(scanner);
                    break;
                default:
                    System.out.println("无效指令，请重新输入！");
            }
        }
    }

    /**
     * 打印指令菜单
     */
    private static void printMenu() {
        System.out.println("========== 指令菜单 ==========");
        System.out.println("1. quit");
        System.out.println("2. create table");
        System.out.println("=============================");
    }

    /**
     * 处理创建表指令
     */
    private static void handleCreateTable(Scanner scanner) {
        try {
            // 提示用户输入表名
            System.out.print("请输入表名：");
            String tableName = scanner.nextLine();

            // 提示用户输入 CSV 文件路径
            System.out.print("请输入 CSV 文件路径：");
            String filePath = scanner.nextLine();

            // 读取 CSV 文件
            List<String[]> csvData = readFullCSV(filePath,tableName);
            String[] headers = csvData.get(0);

            // 动态创建表
            createTableFromCSV(tableName, headers);

            // 插入数据行到数据库
            insertData(tableName, csvData.subList(1, csvData.size()), headers);

            // 查询并打印表中的数据
            queryTable(tableName);

            System.out.println("数据库初始化完成！");
        } catch (Exception e) {
            System.err.println("发生错误：" + e.getMessage());
        }
    }

    /**
     * 读取 CSV 文件
     */
    private static List<String[]> readFullCSV(String filePath,String tableName) throws IOException {
        List<String[]> allData = new ArrayList<>();

        // 根据表名的首字母决定编码
        String charset = tableName.toLowerCase().startsWith("s") ? "UTF-8" : "GBK";

        try (InputStream in = new FileInputStream(filePath);
             InputStreamReader reader = new InputStreamReader(in,charset);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            for (CSVRecord record : csvParser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                allData.add(row);
            }
        }

        return allData;
    }



    /**
     * 动态创建表
     */
    private static void createTableFromCSV(String tableName, String[] headers) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            // 生成创建表的 SQL 语句
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                    .append(tableName)
                    .append(" (id INT PRIMARY KEY AUTO_INCREMENT, ");
            for (String header : headers) {
                // 按空格或逗号拆分表头
                String[] splitHeaders = header.split("[\\s,]+");

                for (String singleHeader : splitHeaders) {
                    String columnName = sanitizeColumnName(singleHeader.trim());
                    if (!columnName.isEmpty()) {
                        sql.append("`").append(columnName).append("` VARCHAR(50), ");
                    }
                }
            }

            sql.delete(sql.length() - 2, sql.length()).append(")");

            // 执行 SQL 语句
            stmt.execute(sql.toString());
            System.out.println("表创建成功：" + tableName);
        }
    }

    /**
     * 对列名进行合法性检查
     */
    private static String sanitizeColumnName(String header) {
        // 替换非法字符为下划线
        return header.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /**
     * 插入数据到数据库
     */
    private static void insertData(String tableName, List<String[]> csvData, String[] headers) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 动态生成插入 SQL
            String sql = generateInsertSQL(tableName, headers);

            // 插入数据（包含第一行，因为第一行是属性）
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String[] row : csvData) { // 插入所有行，包括第一行
                    for (int j = 0; j < row.length; j++) {
                        pstmt.setString(j + 1, row[j]);
                    }
                    pstmt.executeUpdate();
                }
                System.out.println("数据插入成功！");
            }
        }
    }


    /**
     * 动态生成插入 SQL
     */
    private static String generateInsertSQL(String tableName, String[] headers) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder("VALUES (");
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append("`").append(headers[i]).append("`");
            placeholders.append("?");
        }
        sql.append(") ").append(placeholders).append(")");
        return sql.toString(); // 返回生成的 SQL 语句
    }


    /**
     * 查询并打印表中的数据（不打印 id 列）
     */
    private static void queryTable(String tableName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            // 查询表中的所有数据
            String sql = "SELECT * FROM " + tableName;
            ResultSet rs = stmt.executeQuery(sql);

            // 获取表的元数据
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 打印表头（跳过 id 列）
            for (int i = 2; i <= columnCount; i++) { // 从第 2 列开始
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            // 打印数据（跳过 id 列）
            while (rs.next()) {
                for (int i = 2; i <= columnCount; i++) { // 从第 2 列开始
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }

            System.out.println("查询完成！");
        }
    }
}
