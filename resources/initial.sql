CREATE TABLE Users (
                      userID BIGINT AUTO_INCREMENT PRIMARY KEY,
                      userName VARCHAR(50) NOT NULL UNIQUE,
                      password VARCHAR(100) NOT NULL,
                      phone VARCHAR(20)
);

CREATE TABLE Vehicle (
                         vehicleID BIGINT AUTO_INCREMENT PRIMARY KEY,
                         userID BIGINT NOT NULL,
                         license VARCHAR(20) NOT NULL,
                         status BOOLEAN NOT NULL DEFAULT FALSE,
                         CONSTRAINT fk_vehicle_user FOREIGN KEY (userID) REFERENCES Users(userID)
                             ON DELETE CASCADE
                             ON UPDATE CASCADE
);

-- 创建 WorkOrder 表
CREATE TABLE IF NOT EXISTS WorkOrder (
    -- 工单 ID，主键，自增
    orderID BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- 用户 ID，外键，引用 Users 表的 userID 字段
    userID BIGINT,
    -- 车辆 ID，外键，引用 Vehicle 表的 vehicleID 字段
    vehicleID BIGINT,
    -- 分配状态
    assignment VARCHAR(30),
    -- 总花销，使用 DECIMAL 类型存储精确的数值
    totalCost DECIMAL(10, 2),
    -- 维修种类，使用枚举类型，限制取值范围
    description ENUM('漆工', '焊工', '机修'),
    -- 定义 userID 字段的外键约束，关联 Users 表的 userID 字段
    FOREIGN KEY (userID) REFERENCES Users(userID),
    -- 定义 vehicleID 字段的外键约束，关联 Vehicle 表的 vehicleID 字段
    FOREIGN KEY (vehicleID) REFERENCES Vehicle(vehicleID)
    );

CREATE TABLE Technician (
                            technicianID BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            password VARCHAR(100) NOT NULL,
                            skill ENUM('漆⼯', '焊⼯', '机修') NOT NULL,
                            hourlyRate DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS Record (
    -- 记录 ID，主键，自增
    recordID BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- 工单 ID，外键，引用 WorkOrder 表的 orderID 字段
    orderID BIGINT,
    -- 维修人员 ID，外键，引用 Technician 表的 technicianID 字段
    technicianID BIGINT,
    -- 维修用时，使用 DECIMAL 类型存储精确的数值，长度为 5，小数位为 2
    hours DECIMAL(5, 2),
    -- 维修所用材料以及价格，使用 TEXT 类型存储较长文本
    materialRecord TEXT,
    -- 维修工作状态，使用枚举类型，限制取值范围
    PJstatus VARCHAR(30),
    -- 状态更新时间
    updateTime DATETIME,
    -- 工人的个人劳动费
    laborCost DECIMAL(10,2) DEFAULT 0,
    -- 定义 orderID 字段的外键约束，关联 WorkOrder 表的 orderID 字段
    FOREIGN KEY (orderID) REFERENCES WorkOrder(orderID),
    -- 定义 technicianID 字段的外键约束，关联 Technician 表的 technicianID 字段
    FOREIGN KEY (technicianID) REFERENCES Technician(technicianID)
    );

CREATE TABLE IF NOT EXISTS Material (
    -- 每次提交的材料单单号，主键，自增
    usageID BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- 对应相应的记录的 ID，外键
    recordID BIGINT,
    -- 使用的材料种类，使用枚举类型限制取值
    materialName VARCHAR(20),
    -- 数量
    quantity INT,
    -- 单位价格，使用 DECIMAL 类型存储精确的数值
    unitPrice DECIMAL(10, 2),
    -- 总价格，使用 DECIMAL 类型存储精确的数值
    totalPrice DECIMAL(10, 2),
    -- 定义 recordID 字段的外键约束，关联 Record 表的 recordID 字段
    FOREIGN KEY (recordID) REFERENCES Record(recordID)
    );

CREATE TABLE Feedback (
                          feedbackID BIGINT AUTO_INCREMENT PRIMARY KEY,
                          orderID BIGINT,
                          userID BIGINT,
                          technicianID BIGINT,
                          type VARCHAR(25),
                          content TEXT,
                          time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          feedbackTime DATETIME,
                          FOREIGN KEY (orderID) REFERENCES workorder(orderID),
                          FOREIGN KEY (userID) REFERENCES users(userID),
                          FOREIGN KEY (technicianID) REFERENCES technician(technicianID)
);

