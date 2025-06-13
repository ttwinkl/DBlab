public class WorkOrder {
    private int orderID;
    private String description;

    // 带参数的构造方法
    public WorkOrder(int orderID, String description) {
        this.orderID = orderID;
        this.description = description;
    }

    // 无参构造（如果需要）
    public WorkOrder() {
    }

    // getter 和 setter
    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
