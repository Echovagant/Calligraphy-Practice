-- 性能索引：把「按状态过滤 + 按时间排序」「按启用标记 + 使用次数排序」
-- 「按父评论找子评论」「按用户 + 练习时间翻页」这几条热点查询变成走索引。
--
-- 为什么要有这个脚本（重要，别删）：
--   spring.jpa.hibernate.ddl-auto = update **不是**索引的可靠来源。
--   实体上的 @Index 声明只在「Hibernate 认为索引缺失时」才可能补建，
--   而它对「库里的索引与声明是否一致」的判断是按名字 + 列序比对，
--   改名、换列序、或库是历史脚本建出来的，都会让它判成「已存在」而跳过。
--   更关键的是：一个已经跑了一段时间的库，索引只能靠**显式脚本**补齐。
--   所以这里的规矩是 —— 实体上的 @Index 与本脚本保持同步，
--   两者写同一套索引名与列序；本脚本是幂等的，重复执行不报错。
--
-- 幂等：MySQL 8 没有 CREATE INDEX IF NOT EXISTS，
-- 所以每条索引都先查 information_schema.STATISTICS 判断在不在，
-- 不在才 CREATE；已经在就退化成 SELECT 1（空操作）。
--
-- 执行方式：
--   mysql -h 127.0.0.1 -P 3306 -u root -p --default-character-set=utf8mb4 \
--     -e "source SpringBoot-Platform/db/06-performance-indexes.sql"
--
-- 校验：
--   SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
--     FROM information_schema.STATISTICS
--    WHERE TABLE_SCHEMA = 'calligraphy_practice'
--    GROUP BY TABLE_NAME, INDEX_NAME;

USE calligraphy_practice;

-- ---------- works(status, review_status, created_at) ----------
-- 公开画廊的查询条件是 status='PUBLISHED' AND review_status='APPROVED'，
-- 再按 created_at 倒序翻页。已有的 idx_works_user_status(user_id,status,review_status)
-- 前缀是 user_id，公开画廊不带 user_id，用不上，只能单列索引 + filesort。
SELECT '--- works(status, review_status, created_at) ---' AS step;
SET @sql = (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = 'calligraphy_practice'
              AND TABLE_NAME   = 'works'
              AND INDEX_NAME   = 'idx_works_status_review_created'),
    'SELECT 1',
    'CREATE INDEX `idx_works_status_review_created` ON `works` (`status`, `review_status`, `created_at`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- copybooks(is_active, usage_count) ----------
-- 首页概览取「启用中的字帖，按使用次数倒序的前 5 条」。
-- copybooks 表此前除主键外一个索引都没有，这条查询只能全表扫 + 排序。
SELECT '--- copybooks(is_active, usage_count) ---' AS step;
SET @sql = (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = 'calligraphy_practice'
              AND TABLE_NAME   = 'copybooks'
              AND INDEX_NAME   = 'idx_copybooks_active_usage'),
    'SELECT 1',
    'CREATE INDEX `idx_copybooks_active_usage` ON `copybooks` (`is_active`, `usage_count`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- comments(parent_id) ----------
-- 一批根评论的子评论是按 parent_id IN (...) 批量查的（见 CommentRepository
-- 的 findByParentIdInAndStatus*）。comments.parent_id 列存在，但此前没有索引。
SELECT '--- comments(parent_id) ---' AS step;
SET @sql = (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = 'calligraphy_practice'
              AND TABLE_NAME   = 'comments'
              AND INDEX_NAME   = 'idx_comments_parent_id'),
    'SELECT 1',
    'CREATE INDEX `idx_comments_parent_id` ON `comments` (`parent_id`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- practice_records(user_id, practice_date) ----------
-- 练习历史是「某个用户的练习记录，按练习时间倒序翻页」。
-- 已有 idx_user_id 只能定位到用户，排序还得 filesort；
-- 复合索引让过滤与排序走同一条索引。
SELECT '--- practice_records(user_id, practice_date) ---' AS step;
SET @sql = (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = 'calligraphy_practice'
              AND TABLE_NAME   = 'practice_records'
              AND INDEX_NAME   = 'idx_practice_user_date'),
    'SELECT 1',
    'CREATE INDEX `idx_practice_user_date` ON `practice_records` (`user_id`, `practice_date`)'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
