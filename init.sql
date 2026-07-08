CREATE DATABASE IF NOT EXISTS shop_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE shop_db;

CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE TABLE t_order;
TRUNCATE TABLE t_user;

INSERT INTO t_user (id, username, email) VALUES
(1, '张三', 'zhangsan@example.com'),
(2, '李四', 'lisi@example.com'),
(3, '王五', 'wangwu@example.com');

INSERT INTO t_order (id, user_id, product_name, price) VALUES
(101, 1, 'iPhone 15', 6999.00),
(102, 1, 'AirPods Pro', 1899.00),
(103, 2, 'MacBook Pro', 14999.00);
