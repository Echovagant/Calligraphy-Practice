package com.calligraphy.platform.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Function;

/**
 * 用户身份（{@link UserDetails}）的**短 TTL 本地缓存**。
 *
 * <p>{@code JwtAuthenticationFilter} 每收到一个带令牌的请求就要把用户从库里捞出来
 * （一次 users 查询），并且要过一次 BCrypt 密码比对 —— BCrypt 是故意设计得慢的，
 * 每次请求都做一遍纯属浪费。缓存「用户名 → UserDetails」后，
 * 连续请求不必每次都查库 + 比对密码。
 *
 * <p><b>只缓存「身份与权限」，不影响任何鉴权判定</b>：
 * 令牌里的 userType 声明校验（管理端路径族必须 ADMIN 令牌）走的是令牌本身，
 * 与缓存无关；角色（authorities）随 UserDetails 一起缓存，
 * 角色变更最多延迟一个 TTL。
 *
 * <p><b>失效窗口</b>：TTL 到期前，用户改密码 / 被禁用 / 被锁定后，
 * 已缓存的身份仍然有效，最长 {@code security.user-details-cache.ttl-seconds} 秒
 * （默认 60 秒）。改密码与改状态这两条路径会主动调用 {@link #invalidate(String)}
 * 把缓存打掉，所以正常使用下窗口远小于 TTL。
 *
 * <p><b>失败不缓存</b>：加载器抛异常（用户不存在、账户被锁定）时
 * Caffeine 不会写入缓存，异常照常向上抛。
 */
@Component
public class UserDetailsCache {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsCache.class);

    /** 缓存条数上限 —— 用户量级的安全阀，防止内存无限增长。 */
    private static final long MAX_SIZE = 10_000L;

    private final Cache<String, UserDetails> cache;

    public UserDetailsCache(
            @Value("${security.user-details-cache.ttl-seconds:60}") long ttlSeconds) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(Math.max(1L, ttlSeconds)))
                .maximumSize(MAX_SIZE)
                .build();
        log.info("用户身份缓存已启用，TTL={} 秒", ttlSeconds);
    }

    /**
     * 取用户身份；缓存里没有就用 {@code loader} 加载并写入缓存。
     *
     * <p>加载器抛出的异常（用户不存在 / 被锁定）不会被缓存，异常原样抛出。
     */
    public UserDetails get(String username, Function<String, UserDetails> loader) {
        return cache.get(username, loader);
    }

    /** 让某个用户的缓存身份立刻失效（改密码 / 改账号状态后调用）。 */
    public void invalidate(String username) {
        if (username != null) {
            cache.invalidate(username);
        }
    }

    /** 清空全部缓存。 */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /** 当前缓存里的条目数（测试与排查用）。 */
    public long size() {
        return cache.estimatedSize();
    }
}
