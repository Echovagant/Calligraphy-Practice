-- 精选字帖播种数据（3 行）。
--
-- 这份文件被两处使用：
--   1. 测试库：application-test.properties 里 spring.sql.init.data-locations 指向它，
--      测试库每次启动都重播，保证 copybooks 恰好是这 3 行。
--   2. 开发库：手工执行即可得到同样的 3 个字帖
--      （mysql -h 127.0.0.1 -uroot -p calligraphy_practice < db/../src/main/resources/seed/seed-copybooks.sql）。
--
-- 幂等：先删后插，重复执行不会重复插入。
-- 字段与 dev 库的表结构对应：id / title / description / image_url /
-- difficulty / category / character_count / usage_count / is_active / created_at / updated_at。

DELETE FROM copybooks;

INSERT INTO copybooks
    (id, title, description, image_url, difficulty, category,
     character_count, usage_count, is_active, created_at, updated_at)
VALUES
    ('21b54586-7033-45a5-a91a-92da735c5ec9', '草书鉴赏与练习', '高级草书练习，学习名家草书风格',
     '/sample/copybook-3.png', 'advanced', '草书', 60, 3, 1, NOW(), NOW()),
    ('d54f8675-1aed-413a-bd54-7754514955c8', '楷书基础练习', '适合初学者的楷书基础练习字帖，包含常用汉字',
     '/sample/copybook-1.png', 'beginner', '楷书', 100, 3, 1, NOW(), NOW()),
    ('d620c321-6717-4b25-906a-1071a8e06959', '行书技法', '行书书写技巧练习，提高书写流畅度',
     '/sample/copybook-2.png', 'intermediate', '行书', 80, 2, 1, NOW(), NOW());
