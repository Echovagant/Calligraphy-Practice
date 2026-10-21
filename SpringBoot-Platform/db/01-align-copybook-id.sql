-- 把 practice_records.copybook_id 从 bigint 改成 varchar(36)，对齐 copybooks.id 的 char(36)。
-- 历史上这里类型不一致，JPA 无法稳定映射。
-- 幂等：列已经是 varchar(36) 时不做任何事。
USE calligraphy_practice;

SET @col_type = (
  SELECT DATA_TYPE FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'calligraphy_practice'
    AND TABLE_NAME = 'practice_records'
    AND COLUMN_NAME = 'copybook_id'
);

SET @sql = IF(@col_type = 'bigint',
  'ALTER TABLE practice_records MODIFY COLUMN copybook_id VARCHAR(36) NULL',
  'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
