-- 清空所有表数据（如果需要重新初始化）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Feedback;
TRUNCATE TABLE Material;
TRUNCATE TABLE Record;
TRUNCATE TABLE WorkOrder;
TRUNCATE TABLE Vehicle;
TRUNCATE TABLE Technician;
TRUNCATE TABLE Users;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据
INSERT INTO Users (userName, password, phone) VALUES
                        ('Xiaoming', '1234xm', '12345678'),
                        ('Xiaogang', '1234xg', '13456789'),
                        ('Zhangsan', '1234zs', '14567890');

-- 插入维修工人数据
INSERT INTO Technician (name, password, skill, hourlyRate) VALUES
                        ('Jack', '1234jack', '焊⼯', 30.00),
                        ('Nick', '1234nick', '焊⼯', 30.00),
                        ('Dog', '1234dog', '漆⼯', 32.00),
                        ('Steve', '1234steve', '漆⼯', 32.00),
                        ('Mike', '1234mike', '机修', 36.00),
                        ('Tyson', '1234tyson', '机修', 36.00);

-- 插入车辆数据（每位用户至少3辆车）
INSERT INTO Vehicle (userID, license, status) VALUES
                        (1, 'GH1234', 0), -- Xiaoming的车
                        (1, 'XC233', 1),
                        (1, 'YZ7890', 0),
                        (1, 'AB1235', 1),
                        (2, 'CD4567', 0), -- Xiaogang的车
                        (2, 'EF8901', 1),
                        (2, 'GH2345', 0),
                        (3, 'IJ6789', 1), -- Zhangsan的车
                        (3, 'KL0123', 0),
                        (3, 'MN4567', 1),
                        (3, 'OP8901', 0);