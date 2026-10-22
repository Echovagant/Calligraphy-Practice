package com.calligraphy.platform.security;

import com.calligraphy.platform.config.JwtProperties;
import com.calligraphy.platform.service.JwtService;
import com.calligraphy.platform.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 刻意不加 @Component：作为 Bean 会被 Servlet 容器以普通过滤器注册，排序在安全过滤链之后，
// 导致这里填充的 SecurityContext 对鉴权无意义。该过滤器改由 SecurityConfig 显式挂到
// SecurityFilterChain 上（addFilterBefore UsernamePasswordAuthenticationFilter）。
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** 管理端令牌的用户类型声明。 */
    private static final String ADMIN_USER_TYPE = "ADMIN";

    /** 管理端路径族：这些路径下的令牌必须是 ADMIN 类型。 */
    private static final String[] ADMIN_PATH_PREFIXES = {"/api/backend", "/api/admin"};

    /**
     * 管理端里**不消费令牌**的公开端点。
     *
     * <p>登录/刷新不靠 Authorization 头鉴权，前端可能在还带着旧令牌的情况下
     * 调用它们 —— 那时不该因为「令牌类型不对」把登录请求挡掉。
     */
    private static final java.util.Set<String> PUBLIC_ADMIN_PATHS = java.util.Set.of(
            "/api/backend/auth/login",
            "/api/backend/auth/refresh");

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    /** 授权头名称与令牌前缀来自 jwt.* 配置（JwtProperties），不要在这里硬编码。 */
    private final JwtProperties jwtProperties;
    /** 类型不匹配时直接用它输出 403（过滤链里的 AccessDeniedException 不会被 @RestControllerAdvice 接住）。 */
    private final org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler;

    @Override
    protected void doFilterInternal( HttpServletRequest request,  HttpServletResponse response,  FilterChain filterChain)
            throws ServletException, IOException {

        boolean tokenTypeMismatch = false;

        try {
            String jwt = getJwtFromRequest(request);
            logger.debug("收到JWT令牌: {}", jwt != null ? "***" : null);

            if (StringUtils.hasText(jwt)) {
                logger.debug("验证JWT令牌...");
                boolean isValid = jwtService.validateToken(jwt);
                logger.debug("JWT令牌验证结果: {}", isValid);
                
                if (isValid) {
                    String username = jwtService.extractUsername(jwt);
                    logger.debug("从JWT令牌中获取到用户名: {}", username);

                    // 令牌自己声明的 userType 必须与请求的路径族一致：
                    // 「前端登录」发的令牌（userType=USER）不能拿去调管理端接口。
                    // 以前这个声明完全没被校验过，管理员门禁只靠路径前缀 + 数据库角色。
                    if (requiresAdminToken(request) && !ADMIN_USER_TYPE.equals(extractUserType(jwt))) {
                        tokenTypeMismatch = true;
                        logger.warn("令牌类型不匹配，拒绝访问管理端接口 - 路径: {}", pathOf(request));
                    } else {
                        // 直接使用用户名来加载用户信息
                        logger.debug("使用用户名加载用户信息: username={}", username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        logger.debug("成功加载用户信息: username={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());
                        
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("JWT认证成功 - 用户: {}", username);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("JWT认证失败", ex);
        }

        if (tokenTypeMismatch) {
            // 过滤链里的异常不会被 @RestControllerAdvice 接住，直接用处理器输出 403
            accessDeniedHandler.handle(request, response,
                    new org.springframework.security.access.AccessDeniedException(
                            "该接口需要管理员令牌（userType=ADMIN）"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** 读令牌里的 userType 声明；读不出来（含声明缺失/类型不对）一律当作不匹配。 */
    private String extractUserType(String jwt) {
        try {
            return jwtService.extractUserType(jwt);
        } catch (Exception ex) {
            logger.warn("读取令牌 userType 失败: {}", ex.getMessage());
            return null;
        }
    }

    /** 该请求是否必须携带 ADMIN 类型的令牌。 */
    private boolean requiresAdminToken(HttpServletRequest request) {
        String path = pathOf(request);
        if (PUBLIC_ADMIN_PATHS.contains(path)) {
            return false;
        }
        for (String prefix : ADMIN_PATH_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    /** 请求路径（去掉 context path）。 */
    private String pathOf(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri;
    }

    /**
     * 从请求中获取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String headerName = jwtProperties.getAuthHeaderName();
        String prefix = jwtProperties.getTokenPrefix();
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length());
        }
        return null;
    }
}


