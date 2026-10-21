-- 用户表归一（P3 地基）：删除六张域表上指向 frontend_users 的外键，保留指向 users 的外键。
--
-- 背景：库里历史上并存两张前端用户表（users / frontend_users），域表的 user_id 列上
-- 同时挂了两个外键，MySQL 会同时校验两个 —— 插入一条作品要求作者在两张表里各有一行
-- 且 id 相同。而 P2 的 auth 域只往 users 写，导致「注册完根本发不了作品」。
-- 归一后 users 是唯一的前端用户表（model/User.Role 含 ADMIN，frontend_users 没有）。
--
-- 幂等：先查 information_schema.TABLE_CONSTRAINTS / KEY_COLUMN_USAGE 判断约束是否还在，
-- 在才生成 ALTER TABLE ... DROP FOREIGN KEY；不在则退化成 SELECT 1（空操作）。
-- 重复执行不报错；只匹配 REFERENCED_TABLE_NAME = 'frontend_users'，不会误删其它外键。
--
-- 执行方式：
--   mysql -h 127.0.0.1 -P 3306 -u root -p --default-character-set=utf8mb4 \
--     -e "source SpringBoot-Platform/db/02-unify-user-fk.sql"

USE calligraphy_practice;

-- ---------- works.user_id ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `works` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'works'
    AND kcu.COLUMN_NAME           = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- comments.user_id ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `comments` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'comments'
    AND kcu.COLUMN_NAME           = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- discussions.user_id ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `discussions` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'discussions'
    AND kcu.COLUMN_NAME           = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- discussion_replies.user_id ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `discussion_replies` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'discussion_replies'
    AND kcu.COLUMN_NAME           = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- file_reviews.uploaded_by ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `file_reviews` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'file_reviews'
    AND kcu.COLUMN_NAME           = 'uploaded_by'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- practice_records.user_id ----------
SET @sql = (
  SELECT IFNULL(
    CONCAT('ALTER TABLE `practice_records` ', GROUP_CONCAT(
             CONCAT('DROP FOREIGN KEY `', tc.CONSTRAINT_NAME, '`')
             ORDER BY tc.CONSTRAINT_NAME SEPARATOR ', ')),
    'SELECT 1')
  FROM information_schema.TABLE_CONSTRAINTS tc
  JOIN information_schema.KEY_COLUMN_USAGE kcu
    ON kcu.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
   AND kcu.CONSTRAINT_NAME  = tc.CONSTRAINT_NAME
   AND kcu.TABLE_NAME       = tc.TABLE_NAME
  WHERE tc.CONSTRAINT_SCHEMA      = 'calligraphy_practice'
    AND tc.CONSTRAINT_TYPE        = 'FOREIGN KEY'
    AND tc.TABLE_NAME             = 'practice_records'
    AND kcu.COLUMN_NAME           = 'user_id'
    AND kcu.REFERENCED_TABLE_NAME = 'frontend_users'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 校验：下面这条应当只返回指向 users 的外键。
-- SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME
--   FROM information_schema.KEY_COLUMN_USAGE
--  WHERE TABLE_SCHEMA = 'calligraphy_practice'
--    AND TABLE_NAME IN ('works','comments','discussions','discussion_replies',
--                       'file_reviews','practice_records')
--    AND COLUMN_NAME IN ('user_id','uploaded_by')
--    AND REFERENCED_TABLE_NAME IS NOT NULL;
