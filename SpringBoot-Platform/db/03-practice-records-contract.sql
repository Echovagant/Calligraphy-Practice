-- 把 practice_records 对齐 openapi/contract.yaml 的 PracticeRecord schema。
--
-- 背景：实体原先只有练习时间（分钟）、笔画数、满意度评分、难度等级、完成度、
-- 图片 URL 这几列，跟契约的 duration（秒）/characterCount/fileUrls 等对不上，
-- P4 实施时把实体改了，数据库这边要跟上。
--
-- 幂等：列已存在时不做任何事；可以重复执行。
USE calligraphy_practice;

SET SESSION group_concat_max_len = 8192;

-- ---------- 1. 新增契约需要的列 ----------

SET @add_cols = (
  SELECT GROUP_CONCAT(CONCAT('ADD COLUMN `', c.col, '` ', c.def) ORDER BY c.ord SEPARATOR ', ')
  FROM (
    SELECT 'title' AS col, 'VARCHAR(200) NULL' AS def, 1 AS ord
    UNION ALL SELECT 'category',     'VARCHAR(50) NULL',  2
    UNION ALL SELECT 'style',        'VARCHAR(50) NULL',  3
    UNION ALL SELECT 'reference_work_id',    'VARCHAR(36) NULL', 4
    UNION ALL SELECT 'reference_work_title', 'VARCHAR(200) NULL', 5
    UNION ALL SELECT 'file_urls',   'TEXT NULL',           6
    UNION ALL SELECT 'duration',    'BIGINT NULL',         7
    UNION ALL SELECT 'character_count', 'INT NULL',         8
    UNION ALL SELECT 'difficulty',  'VARCHAR(20) NULL',    9
    UNION ALL SELECT 'notes',       'VARCHAR(1000) NULL',  10
  ) c
  WHERE NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'calligraphy_practice'
      AND TABLE_NAME = 'practice_records'
      AND COLUMN_NAME = c.col
  )
);

SET @sql = IF(@add_cols IS NULL, 'SELECT 1', CONCAT('ALTER TABLE practice_records ', @add_cols));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 2. 删掉实体已不再映射的列 ----------
--
-- 这些列契约里都没有，实体也不再映射，留着只是噪音。
-- characters_practiced 上还挂着 CHECK 约束 (characters_practiced >= 0)，
-- 删列会连带删掉该约束。
-- 注意：本表在 P4 实施时是空表，删列无数据风险。

SET @drop_cols = (
  SELECT GROUP_CONCAT(CONCAT('DROP COLUMN `', c.col, '`') ORDER BY c.ord SEPARATOR ', ')
  FROM (
    SELECT 'practice_time' AS col, 1 AS ord
    UNION ALL SELECT 'strokes_count',       2
    UNION ALL SELECT 'satisfaction_score',  3
    UNION ALL SELECT 'difficulty_level',    4
    UNION ALL SELECT 'completion_rate',     5
    UNION ALL SELECT 'image_url',           6
    UNION ALL SELECT 'characters_practiced',7
  ) c
  WHERE EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'calligraphy_practice'
      AND TABLE_NAME = 'practice_records'
      AND COLUMN_NAME = c.col
  )
);

SET @sql = IF(@drop_cols IS NULL, 'SELECT 1', CONCAT('ALTER TABLE practice_records ', @drop_cols));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
