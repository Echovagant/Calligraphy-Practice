-- 把 3 行播种字帖的封面图指向**真实存在**的静态资源。
--
-- 背景：原来写的是 /uploads/sample-copybookN.jpg，但
--   ① 那些文件从来不存在；
--   ② 后端只映射了 /api/files/**（WebConfig.addResourceHandlers），没有 /uploads/**；
--   ③ 前端 vite 也只代理 /api。
-- 三条都不通 ⇒ 首页「热门字帖」与字帖列表全是破图（真机点击回归时发现）。
--
-- 现在指向前端 public/sample/ 下的样张：vite 会把 public/ 原样拷进 dist/，
-- dev（vite 直接供）与生产（dist/）都能取到，路径是站内相对路径。
--
-- 幂等：只改 image_url 仍是旧值的那一行，重复执行不会重复改。
--
-- 注意：**只按 image_url 匹配，不要按中文标题匹配。**
-- 在中文 Windows 上把 .sql 文件用 PowerShell 管道喂给 mysql 时，
-- 文件里的中文会被编码搞坏，WHERE title = '楷书基础练习' 匹配不到任何行
-- （实测 title_match=0，而纯 ASCII 的 image_url 比较正常）。
-- 旧路径本身已经唯一，用它匹配最稳。
USE calligraphy_practice;

UPDATE copybooks
SET image_url = '/sample/copybook-1.png'
WHERE image_url = '/uploads/sample-copybook1.jpg';

UPDATE copybooks
SET image_url = '/sample/copybook-2.png'
WHERE image_url = '/uploads/sample-copybook2.jpg';

UPDATE copybooks
SET image_url = '/sample/copybook-3.png'
WHERE image_url = '/uploads/sample-copybook3.jpg';
