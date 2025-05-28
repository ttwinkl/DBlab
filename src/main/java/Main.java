import java.util.Scanner;
import java.lang.String;

public class Main {
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
                    System.out.println("欢迎用户！请输入下列相应编号继续操作：");
                    UserMain.main(args);
                    scanner.close();
                    return;
                case "2":
//                    待添加
                    System.out.println("欢迎用户！请输入下列相应编号继续操作：");
                    TechnicianMain.main(args);
                    break;
                default:
                    System.out.println("无效指令，请重新输入！");
            }
        }
    }
    private static void printMenu() {
        System.out.println("========== 指令菜单 ==========");
        System.out.println("1. 您是User");
        System.out.println("2. 您是Technician");
        System.out.println("3. 您是管理员");
        System.out.println("=============================");
    }
}
