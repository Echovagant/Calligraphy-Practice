-- discussion_likes：谁赞了哪个讨论 —— 点赞去重的最小依据。
--
-- 背景：契约的 discussionLike 端点是「点赞 / 取消点赞」，响应里有 liked 布尔值。
-- 没有这张表就没法知道「当前用户是不是已经赞过」，也就没法既支持取消点赞、
-- 又保证同一个用户的重复点赞不会把 likeCount 一次次往上加。
--
-- 刻意不建外键：只存两个普通列，删用户时不会因为外键约束卡住
-- （代价是删用户后会留下孤儿点赞记录，计数会略微偏高）。
--
-- 幂等：表已存在时不做任何事；可以重复执行。
USE calligraphy_practice;

CREATE TABLE IF NOT EXISTS discussion_likes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    discussion_id BIGINT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at DATETIME NOT NULL,
    -- 同一个用户对同一个讨论最多只有一条记录：
    -- 并发双击时第二次插入会撞这个约束并回滚该事务
    CONSTRAINT uk_discussion_likes_discussion_user UNIQUE (discussion_id, user_id),
    KEY idx_discussion_likes_user_id (user_id)
);
