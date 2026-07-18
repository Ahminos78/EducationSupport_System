-- Test data for edu_platform.
-- All seeded users use password: test123456
-- Import:
--   docker exec -i edu-mysql mysql -uroot -p123456 edu_platform < scripts/seed-test-data.sql

USE edu_platform;

SET NAMES utf8mb4;

INSERT INTO tb_user (id, username, password_hash, nickname, role, deleted)
VALUES
    (9001, 'admin_seed', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '测试管理员', 3, 0),
    (9002, 'teacher_li', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '李老师', 2, 0),
    (9003, 'teacher_wang', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '王老师', 2, 0),
    (9011, 'student_zhang', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '张同学', 1, 0),
    (9012, 'student_chen', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '陈同学', 1, 0),
    (9013, 'student_liu', '$2b$12$W/PoJoiFSkzIY9YuYMzXWOoE9O.uNkUaJ3JTuZ7iTgkBJ3rE2EAiy', '刘同学', 1, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    role = VALUES(role),
    deleted = VALUES(deleted);

INSERT INTO tb_course (id, teacher_id, name, description, cover_url, max_students, enrolled_count, status, deleted)
VALUES
    (9101, 9002, 'Java Web 开发实战', '围绕 Spring Boot、网关、接口联调完成在线教育系统后端开发。', 'https://example.com/covers/java-web.png', 60, 2, 1, 0),
    (9102, 9002, '数据库设计与 MyBatis Plus', '学习核心业务表设计、实体映射、逻辑删除和常用 CRUD。', 'https://example.com/covers/database.png', 45, 1, 1, 0),
    (9103, 9003, '前端工程化与 Vue3', '使用 Vue3、Vite、Pinia、Element Plus 构建管理端页面。', 'https://example.com/covers/vue3.png', 50, 1, 1, 0),
    (9104, 9003, '旧版下架课程样例', '用于验证课程下架状态过滤。', 'https://example.com/covers/offline.png', 30, 0, 0, 0)
ON DUPLICATE KEY UPDATE
    teacher_id = VALUES(teacher_id),
    name = VALUES(name),
    description = VALUES(description),
    cover_url = VALUES(cover_url),
    max_students = VALUES(max_students),
    enrolled_count = VALUES(enrolled_count),
    status = VALUES(status),
    deleted = VALUES(deleted);

INSERT INTO tb_enrollment (id, course_id, student_id, status, apply_reason, review_comment, applied_at, reviewed_at)
VALUES
    (9201, 9101, 9011, 1, '希望系统学习后端接口开发。', '基础匹配，通过选课。', '2026-07-01 09:00:00', '2026-07-01 10:00:00'),
    (9202, 9101, 9012, 1, '想完成毕业设计中的后端模块。', '通过。', '2026-07-01 09:30:00', '2026-07-01 10:10:00'),
    (9203, 9102, 9011, 0, '想熟悉 MyBatis Plus 的实际项目用法。', NULL, '2026-07-02 11:00:00', NULL),
    (9204, 9103, 9013, 4, '希望补充前端知识。', '本期名额暂不开放。', '2026-07-03 14:00:00', '2026-07-03 16:00:00'),
    (9205, 9103, 9012, 2, '先试听前端工程化课程。', '学生主动退课。', '2026-07-04 10:00:00', '2026-07-05 10:00:00')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    apply_reason = VALUES(apply_reason),
    review_comment = VALUES(review_comment),
    applied_at = VALUES(applied_at),
    reviewed_at = VALUES(reviewed_at);

INSERT INTO tb_assignment (id, course_id, teacher_id, title, description, full_score, deadline, status, deleted)
VALUES
    (9301, 9101, 9002, '第一次作业：登录接口联调', '完成登录接口调用，记录请求、响应和异常处理。', 100, '2026-07-20 23:59:59', 1, 0),
    (9302, 9101, 9002, '第二次作业：课程模块 CRUD', '完成课程列表、新增、编辑和状态修改接口测试。', 100, '2026-07-27 23:59:59', 0, 0),
    (9303, 9102, 9002, 'MyBatis Plus 实体映射练习', '整理实体类注解、Mapper 继承和逻辑删除说明。', 100, '2026-07-25 23:59:59', 1, 0),
    (9304, 9103, 9003, 'Vue3 页面组件拆分', '完成登录页和基础布局组件拆分。', 100, '2026-07-18 23:59:59', 2, 0)
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    teacher_id = VALUES(teacher_id),
    title = VALUES(title),
    description = VALUES(description),
    full_score = VALUES(full_score),
    deadline = VALUES(deadline),
    status = VALUES(status),
    deleted = VALUES(deleted);

INSERT INTO tb_submission (id, assignment_id, student_id, content, attachment_url, score, teacher_comment, ai_comment, submitted_at, graded_at)
VALUES
    (9401, 9301, 9011, '已完成登录接口联调，附 curl 命令和截图说明。', 'https://example.com/submissions/login-zhang.zip', 92, '接口调用完整，异常场景可以再补充。', 'AI 建议：增加 Token 过期场景测试。', '2026-07-10 20:30:00', '2026-07-11 09:00:00'),
    (9402, 9301, 9012, '完成了登录、获取当前用户和权限错误测试。', 'https://example.com/submissions/login-chen.zip', 88, '整体完成度较好，文档说明略少。', NULL, '2026-07-10 21:10:00', '2026-07-11 09:20:00'),
    (9403, 9303, 9011, '梳理了 TableName、TableId、TableLogic 的使用方式。', NULL, NULL, NULL, NULL, '2026-07-12 18:00:00', NULL)
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    attachment_url = VALUES(attachment_url),
    score = VALUES(score),
    teacher_comment = VALUES(teacher_comment),
    ai_comment = VALUES(ai_comment),
    submitted_at = VALUES(submitted_at),
    graded_at = VALUES(graded_at);

INSERT INTO tb_exam (id, course_id, teacher_id, title, description, start_time, end_time, full_score, status, deleted)
VALUES
    (9501, 9101, 9002, 'Java Web 阶段测验', '覆盖登录、网关、课程和选课接口基础知识。', '2026-07-30 09:00:00', '2026-07-30 11:00:00', 100, 1, 0),
    (9502, 9102, 9002, '数据库设计小测', '考察核心业务表、索引和逻辑删除设计。', '2026-08-02 14:00:00', '2026-08-02 15:30:00', 100, 0, 0),
    (9503, 9103, 9003, 'Vue3 组件化测验', '考察路由、状态管理和组件拆分。', '2026-07-19 19:00:00', '2026-07-19 20:00:00', 100, 2, 0)
ON DUPLICATE KEY UPDATE
    course_id = VALUES(course_id),
    teacher_id = VALUES(teacher_id),
    title = VALUES(title),
    description = VALUES(description),
    start_time = VALUES(start_time),
    end_time = VALUES(end_time),
    full_score = VALUES(full_score),
    status = VALUES(status),
    deleted = VALUES(deleted);

