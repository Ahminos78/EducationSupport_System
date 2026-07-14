USE edu_platform;
SET NAMES utf8mb4;

-- Delete garbled test accounts (keep init.sql accounts: teacher01/teacher02 and courses)
DELETE FROM tb_student_profile WHERE user_id >= 201;
DELETE FROM tb_teacher_profile WHERE user_id BETWEEN 101 AND 106;
DELETE FROM tb_user WHERE id IN (1, 101, 102, 103, 104, 105, 106, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215);

-- Fix teacher01/teacher02 garbled names from init.sql
UPDATE tb_user SET nickname = '张教授' WHERE id = 1000;
UPDATE tb_user SET nickname = '李教授' WHERE id = 1001;
UPDATE tb_teacher_profile SET real_name = '张教授' WHERE user_id = 1000;
UPDATE tb_teacher_profile SET real_name = '李教授' WHERE user_id = 1001;

-- Re-insert admin and test accounts
INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted) VALUES
  (1, 'admin', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '系统管理员', 3, 0),
  (101, 't1', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '张老师', 2, 0),
  (102, 't2', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '李老师', 2, 0),
  (103, 't3', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '王老师', 2, 0),
  (104, 't4', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '赵老师', 2, 0),
  (105, 't5', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '刘老师', 2, 0),
  (106, 't6', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '陈老师', 2, 0),
  (201, 's1',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '张同学', 1, 0),
  (202, 's2',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '李同学', 1, 0),
  (203, 's3',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '王同学', 1, 0),
  (204, 's4',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '赵同学', 1, 0),
  (205, 's5',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '刘同学', 1, 0),
  (206, 's6',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '陈同学', 1, 0),
  (207, 's7',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '杨同学', 1, 0),
  (208, 's8',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '黄同学', 1, 0),
  (209, 's9',  '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '周同学', 1, 0),
  (210, 's10', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '吴同学', 1, 0),
  (211, 's11', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '郑同学', 1, 0),
  (212, 's12', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '孙同学', 1, 0),
  (213, 's13', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '马同学', 1, 0),
  (214, 's14', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '胡同学', 1, 0),
  (215, 's15', '$2a$10$E9hyYoDLI.aomyi4RWQgk.kRAEKhW3WnjCY8KLOqUkMTtUDw4G.H.', '林同学', 1, 0)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname), role=VALUES(role), deleted=VALUES(deleted);

-- Re-insert teacher profiles
INSERT INTO tb_teacher_profile (user_id, employee_no, real_name, college) VALUES
  (101, 'T0101', '张老师', '计算机科学与技术学院'),
  (102, 'T0102', '李老师', '软件工程系'),
  (103, 'T0103', '王老师', '人工智能学院'),
  (104, 'T0104', '赵老师', '计算机科学与技术学院'),
  (105, 'T0105', '刘老师', '软件工程系'),
  (106, 'T0106', '陈老师', '外国语学院')
ON DUPLICATE KEY UPDATE real_name=VALUES(real_name), college=VALUES(college);

-- Re-insert student profiles
INSERT INTO tb_student_profile (user_id, student_no, real_name, college, major, class_name, grade, enrollment_year) VALUES
  (201, '2023001', '张同学', '计算机科学与技术学院', '计算机科学与技术', '计科2301班', '2023', 2023),
  (202, '2023002', '李同学', '计算机科学与技术学院', '软件工程', '软工2301班', '2023', 2023),
  (203, '2023003', '王同学', '软件工程系', '软件工程', '软工2302班', '2023', 2023),
  (204, '2023004', '赵同学', '人工智能学院', '人工智能', 'AI2301班', '2023', 2023),
  (205, '2023005', '刘同学', '计算机科学与技术学院', '计算机科学与技术', '计科2302班', '2023', 2023),
  (206, '2023006', '陈同学', '软件工程系', '软件工程', '软工2303班', '2023', 2023),
  (207, '2023007', '杨同学', '人工智能学院', '数据科学', '数科2301班', '2023', 2023),
  (208, '2023008', '黄同学', '计算机科学与技术学院', '计算机科学与技术', '计科2303班', '2023', 2023),
  (209, '2023009', '周同学', '软件工程系', '软件工程', '软工2304班', '2023', 2023),
  (210, '2023010', '吴同学', '计算机科学与技术学院', '网络工程', '网工2301班', '2023', 2023),
  (211, '2023011', '郑同学', '人工智能学院', '人工智能', 'AI2302班', '2023', 2023),
  (212, '2023012', '孙同学', '计算机科学与技术学院', '计算机科学与技术', '计科2304班', '2023', 2023),
  (213, '2023013', '马同学', '软件工程系', '软件工程', '软工2305班', '2023', 2023),
  (214, '2023014', '胡同学', '计算机科学与技术学院', '信息安全', '信安2301班', '2023', 2023),
  (215, '2023015', '林同学', '软件工程系', '软件工程', '软工2306班', '2023', 2023)
ON DUPLICATE KEY UPDATE real_name=VALUES(real_name), college=VALUES(college), major=VALUES(major), class_name=VALUES(class_name), grade=VALUES(grade), enrollment_year=VALUES(enrollment_year);
