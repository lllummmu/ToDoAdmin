-- TodoApp 数据库初始化脚本
-- 字符集 utf8mb4 / InnoDB
CREATE DATABASE IF NOT EXISTS todo_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE todo_app;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务表（remark/attachment 为工程化版本扩展字段，课程要求字段全集保持不变）
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    assignee_id INT DEFAULT NULL,
    text VARCHAR(255) NOT NULL,
    remark TEXT DEFAULT NULL,
    attachment VARCHAR(255) DEFAULT NULL,
    completed BOOLEAN DEFAULT FALSE,
    priority ENUM('high','medium','low') DEFAULT 'medium',
    due_date DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 旧表兼容：若 tasks 表已存在且缺扩展字段，取消下面两行的注释执行
-- ALTER TABLE tasks ADD COLUMN remark TEXT DEFAULT NULL AFTER text;
-- ALTER TABLE tasks ADD COLUMN attachment VARCHAR(255) DEFAULT NULL AFTER remark;

-- 测试账号（密码均为 123456 的 MD5）
INSERT INTO users(username, password)
SELECT * FROM (SELECT 'admin' AS username, MD5('123456') AS password) t
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin');
INSERT INTO users(username, password)
SELECT * FROM (SELECT 'alice' AS username, MD5('123456') AS password) t
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='alice');
INSERT INTO users(username, password)
SELECT * FROM (SELECT 'bob' AS username, MD5('123456') AS password) t
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='bob');

-- 测试任务
INSERT INTO tasks(user_id, assignee_id, text, remark, priority, due_date)
SELECT 1, 2, '完成 Web 开发期末大作业', '# 作业清单\n- 前端组件化\n- 后端接口\n- **数据库脚本**', 'high', DATE_ADD(NOW(), INTERVAL 2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE text='完成 Web 开发期末大作业');
INSERT INTO tasks(user_id, assignee_id, text, remark, priority, due_date)
SELECT 2, 1, '准备前端实习面试', '复习 `Vue3` 响应式原理与 `Pinia` 用法', 'medium', DATE_ADD(NOW(), INTERVAL 5 DAY)
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE text='准备前端实习面试');
INSERT INTO tasks(user_id, assignee_id, text, priority, due_date, completed)
SELECT 1, NULL, '整理 Git 仓库并完善 README', 'low', DATE_ADD(NOW(), INTERVAL 7 DAY), FALSE
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE text='整理 Git 仓库并完善 README');
INSERT INTO tasks(user_id, assignee_id, text, priority, due_date, completed)
SELECT 3, 1, '已过期的历史任务（演示高亮）', 'high', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE text='已过期的历史任务（演示高亮）');
INSERT INTO tasks(user_id, assignee_id, text, priority, completed)
SELECT 1, 3, '已完成的示例任务', 'medium', TRUE
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE text='已完成的示例任务');
