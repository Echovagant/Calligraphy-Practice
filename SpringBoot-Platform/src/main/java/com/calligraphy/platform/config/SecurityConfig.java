package com.calligraphy.platform.config;

import com.calligraphy.platform.security.CustomAccessDeniedHandler;
import com.calligraphy.platform.security.JwtAuthenticationEntryPoint;
import com.calligraphy.platform.security.JwtAuthenticationFilter;
import com.calligraphy.platform.security.UserDetailsCache;
import com.calligraphy.platform.service.JwtService;
import com.calligraphy.platform.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 基础配置。
 *
 * <p>提供 {@link PasswordEncoder}、无状态 {@link SecurityFilterChain} 以及
 * {@link AuthenticationManager}。JWT 校验统一走 {@link JwtService}
 * （com.calligraphy.platform.service 包下的唯一实现），不再存在第二套 JWT 逻辑。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtProperties jwtProperties;

    /** 无需认证即可访问的路径。 */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/frontend/auth/register",
            "/api/frontend/auth/login",
            "/api/backend/auth/login",
            "/api/auth/verification-code",
            // 刷新与登出在 access token 失效/过期时也要能调用，这两个端点自行校验 refreshToken
            "/api/frontend/auth/refresh",
            "/api/backend/auth/refresh",
            // 注意：**登出端点不在公开列表里**。契约标注两个 logout「需登录」，
            // 放行匿名会让「谁都能登出」变成 204 —— 现在无令牌访问是 401，
            // 有令牌（哪怕已过期）才是 204。
            // 上传文件由 WebConfig 的静态资源映射提供（/api/files/** -> {file.upload-dir}），
            // 作品画廊是公开的，图片也必须能匿名取回
            "/api/files/**",
            "/uploads/**",
            "/error",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   UserDetailsServiceImpl userDetailsService,
                                                   UserDetailsCache userDetailsCache) throws Exception {
        // JwtAuthenticationFilter 不是 Spring Bean，避免被 Servlet 容器当作普通过滤器重复注册
        // （那样它会以 LOWEST_PRECEDENCE 排在安全过滤链之后，填充的 SecurityContext 来得太晚）。
        // 这里显式挂到安全过滤链上，保证它在鉴权过滤器之前执行。
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService, userDetailsService, jwtProperties, accessDeniedHandler,
                        userDetailsCache);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(WebConfig.corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 公开画廊与首页概览不需要令牌（契约里这两个是 security: []）
                        .requestMatchers(HttpMethod.GET, "/api/works").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/overview").permitAll()
                        // 字帖浏览也是公开的（契约里 security: []）：列表、热门、详情。
                        // 注意 /{copybookId} 只匹配一段，所以 /statistics/user 与 /practice/history
                        // 这两个「当前用户」端点仍然需要令牌。
                        .requestMatchers(HttpMethod.GET, "/api/copybooks",
                                "/api/copybooks/popular", "/api/copybooks/{copybookId}").permitAll()
                        // 讨论浏览同样是公开的（契约里 security: []）：列表、热门、精华、置顶、
                        // 搜索、详情、某个讨论的回复列表。
                        // 注意 /{id} 只匹配一段，所以 /{discussionId}/replies 是另一条；
                        // 发帖 / 编辑 / 删除 / 点赞这些写操作仍然需要令牌。
                        .requestMatchers(HttpMethod.GET, "/api/discussions",
                                "/api/discussions/hot", "/api/discussions/featured",
                                "/api/discussions/pinned", "/api/discussions/search",
                                "/api/discussions/{id}", "/api/discussions/{discussionId}/replies").permitAll()
                        // 作品评论列表也是公开的（契约里 security: []）。
                        // 注意只放开「按作品查」这一条：按用户查评论历史是「当前用户」的数据，仍然需要令牌。
                        .requestMatchers(HttpMethod.GET, "/api/community/comments/work/{workId}").permitAll()
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * AuthServiceImpl / AdminServiceImpl / UserServiceImpl 都注入了 PasswordEncoder，
     * Spring Boot 不会自动提供该 Bean，需要显式声明。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthServiceImpl / AdminServiceImpl 都注入了 AuthenticationManager，
     * Spring Security 同样不会自动提供该 Bean，需要从 AuthenticationConfiguration 显式暴露。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
