-- 书法练习平台前后端分离用户系统数据库架构
-- 创建时间: 2024年
-- 描述: 实现前后端用户完全分离的认证与权限管理系统

-- 禁用外键检查以避免依赖关系问题
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有相关表（按依赖顺序）
DROP TABLE IF EXISTS file_reviews;
DROP TABLE IF EXISTS operation_audit_logs;
DROP TABLE IF EXISTS login_audit_logs;
DROP TABLE IF EXISTS practice_records;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS works;
DROP TABLE IF EXISTS backend_users;
DROP TABLE IF EXISTS frontend_users;
DROP TABLE IF EXISTS roles;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 前端用户表
-- =====================================================
CREATE TABLE frontend_users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('USER', 'PREMIUM_USER') NOT NULL DEFAULT 'USER',
    full_name VARCHAR(100),
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    last_login DATETIME,
    login_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 索引优化
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 后端管理员表
-- =====================================================
CREATE TABLE backend_users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'SUPER_ADMIN') NOT NULL,
    full_name VARCHAR(100),
    department VARCHAR(100),
    permissions JSON,
    security_level INT NOT NULL DEFAULT 1,
    last_login DATETIME,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    lock_until DATETIME,
    password_changed_at DATETIME,
    last_password_reset DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 索引优化
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_account_locked (account_locked),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 权限角色表
-- =====================================================
CREATE TABLE roles (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    permissions JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_name (name)
);

-- =====================================================
-- 作品表（关联前端用户）
-- =====================================================
CREATE TABLE works (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN', 'UNDER_REVIEW') NOT NULL DEFAULT 'DRAFT',
    review_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'NEEDS_REVISION') DEFAULT 'PENDING',
    reviewer_id VARCHAR(36),
    review_notes TEXT,
    review_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES frontend_users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES backend_users(id) ON DELETE SET NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_review_status (review_status),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 练习记录表（关联前端用户）
-- =====================================================
CREATE TABLE practice_records (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    practice_time INT,
    strokes_count INT,
    practice_type VARCHAR(50),
    content TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES frontend_users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 评论表（关联前端用户）
-- =====================================================
CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    work_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewer_id VARCHAR(36),
    review_notes TEXT,
    review_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES frontend_users(id) ON DELETE CASCADE,
    FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES backend_users(id) ON DELETE SET NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_work_id (work_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 文件审核表
-- =====================================================
CREATE TABLE file_reviews (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL,
    reviewer_id VARCHAR(36),
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'NEEDS_REVISION') NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES frontend_users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES backend_users(id) ON DELETE SET NULL,
    
    INDEX idx_status (status),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_reviewer (reviewer_id),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 登录审计日志表
-- =====================================================
CREATE TABLE login_audit_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36),
    user_type ENUM('FRONTEND', 'BACKEND') NOT NULL,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    login_result ENUM('SUCCESS', 'FAILED', 'LOCKED') NOT NULL,
    failure_reason VARCHAR(255),
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_time (login_time),
    INDEX idx_login_result (login_result),
    INDEX idx_user_type (user_type)
);

-- =====================================================
-- 操作审计日志表
-- =====================================================
CREATE TABLE operation_audit_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36),
    user_type ENUM('FRONTEND', 'BACKEND') NOT NULL,
    operation_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(36),
    operation_result ENUM('SUCCESS', 'FAILED') NOT NULL,
    error_message TEXT,
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_data JSON,
    response_data JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    INDEX idx_user_id (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_type (operation_type),
    INDEX idx_user_type (user_type),
    INDEX idx_resource_type (resource_type)
);

-- =====================================================
-- 插入默认角色数据
-- =====================================================
INSERT INTO roles (id, name, description, permissions) VALUES
(UUID(), 'USER', '普通前端用户', '["view_works", "upload_works", "practice", "comment"]'),
(UUID(), 'PREMIUM_USER', '高级前端用户', '["view_works", "upload_works", "practice", "comment", "premium_content", "advanced_features"]'),
(UUID(), 'ADMIN', '系统管理员', '["manage_users", "review_content", "view_analytics", "system_config"]'),
(UUID(), 'SUPER_ADMIN', '超级管理员', '["all_permissions"]');

-- =====================================================
-- 插入默认超级管理员账户（密码需要在应用启动后修改）
-- =====================================================
-- 注意：默认密码为 "Admin@123456"，首次登录后必须修改
INSERT INTO backend_users (
    id, username, password, email, role, full_name, 
    department, permissions, security_level, password_changed_at
) VALUES (
    'admin-001',
    'superadmin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', -- Admin@123456
    'admin@calligraphy.com',
    'SUPER_ADMIN',
    '系统超级管理员',
    '技术部',
    '["all_permissions"]',
    3,
    NOW()
);

-- =====================================================
-- 创建数据库视图（可选）
-- =====================================================
-- 用户统计视图
CREATE VIEW user_statistics AS
SELECT 
    'frontend' as user_type,
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) as new_users_today
FROM frontend_users
UNION ALL
SELECT 
    'backend' as user_type,
    COUNT(*) as total_users,
    COUNT(CASE WHEN account_locked = FALSE THEN 1 END) as active_users,
    COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) as new_users_today
FROM backend_users;

-- 待审核内容统计视图
CREATE VIEW pending_reviews_statistics AS
SELECT 
    'works' as content_type,
    COUNT(*) as pending_count
FROM works 
WHERE review_status = 'PENDING'
UNION ALL
SELECT 
    'comments' as content_type,
    COUNT(*) as pending_count
FROM comments 
WHERE status = 'PENDING'
UNION ALL
SELECT 
    'files' as content_type,
    COUNT(*) as pending_count
FROM file_reviews 
WHERE status = 'PENDING';

-- =====================================================
-- 数据库性能优化索引
-- =====================================================
-- 复合索引优化
CREATE INDEX idx_works_user_status ON works(user_id, status, review_status);
CREATE INDEX idx_comments_user_status ON comments(user_id, status);
CREATE INDEX idx_file_reviews_user_status ON file_reviews(uploaded_by, status);
CREATE INDEX idx_login_audit_time_type ON login_audit_logs(login_time, user_type, login_result);
CREATE INDEX idx_operation_audit_time_type ON operation_audit_logs(operation_time, user_type, operation_type);