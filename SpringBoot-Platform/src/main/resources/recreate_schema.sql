-- 警告：本脚本是历史遗留的「部分重建」脚本，只覆盖 users / works / comments /
-- practice_records 四张表，且 users 与 works 的列已经落后于当前实体定义
-- （例如 users 缺 status / email_verified / two_factor_enabled 等列）。
-- 拿它重建线上库会把数据改成实体映射不认的样子。
-- 日常变更请用 db/ 目录下按编号的幂等迁移脚本。
--
-- 禁用外键检查以避免依赖关系问题
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有相关表（按任意顺序）
DROP TABLE IF EXISTS practice_records;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS works;
DROP TABLE IF EXISTS users;

-- 重新创建users表，使用VARCHAR类型作为ID，并将role字段改为ENUM类型
CREATE TABLE users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('USER', 'ADMIN', 'SUPER_ADMIN') NOT NULL,
    full_name VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login DATETIME,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    login_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 重建works表，确保外键引用users表的VARCHAR类型ID
CREATE TABLE works (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 重建comments表，确保外键引用正确的ID类型
CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    work_id BIGINT NOT NULL,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE
);

-- 重建practice_records表，确保外键引用正确的ID类型
-- 字段集对齐 openapi/contract.yaml 的 PracticeRecord schema（见 db/03-practice-records-contract.sql）
CREATE TABLE practice_records (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    copybook_id VARCHAR(36),
    copybook_title VARCHAR(100),
    title VARCHAR(200),
    content VARCHAR(500),
    category VARCHAR(50),
    style VARCHAR(50),
    practice_type VARCHAR(50),
    reference_work_id VARCHAR(36),
    reference_work_title VARCHAR(200),
    file_urls TEXT,
    duration BIGINT,
    character_count INT,
    difficulty VARCHAR(20),
    notes VARCHAR(1000),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    practice_date DATETIME(6),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;