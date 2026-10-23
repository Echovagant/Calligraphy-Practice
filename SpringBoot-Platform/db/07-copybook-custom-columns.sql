-- copybooks 的「自建字帖」字段与配套列。
--
-- 背景：新增「云端字帖」功能（POST /api/copybooks、GET /api/copybooks/mine）后，
-- copybooks 表多了九列：
--   is_custom / user_id / content / font / layout /
--   characters_per_page / font_size / paper_color / grid_color
--
-- 列本身由 ddl-auto=update 自动补齐 —— 九列全是可空列，没有 NOT NULL 补列失败的问题。
-- 但补出来的 is_custom 在老行上是 NULL：服务端把 NULL 当「不是自建」处理（照常出现在
-- 公开字帖列表里），可 DTO 里的 isCustom 会序列化成 null，前端拿到的类型不稳。
--
-- 所以这里把老行统一补成 0（= 精选字帖，不是用户自建）。
--
-- 幂等：重复执行无害（只补 NULL 的行）。
-- 前置条件：is_custom 列已存在 —— 先启动一次应用，ddl-auto=update 会把列补上。
--
-- 执行方式：
--   mysql -h 127.0.0.1 -P 3306 -u root -p --default-character-set=utf8mb4 \
--     -e "source SpringBoot-Platform/db/07-copybook-custom-columns.sql"

USE calligraphy_practice;

UPDATE copybooks SET is_custom = 0 WHERE is_custom IS NULL;

-- 校验：应当返回 0 行（没有还停在 NULL 的老行）。
-- SELECT COUNT(*) FROM copybooks WHERE is_custom IS NULL;
