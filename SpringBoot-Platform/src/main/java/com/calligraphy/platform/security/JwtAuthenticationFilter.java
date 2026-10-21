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

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    /** 授权头名称与令牌前缀来自 jwt.* 配置（JwtProperties），不要在这里硬编码。 */
    private final JwtProperties jwtProperties;
    @Override
    protected void doFilterInternal( HttpServletRequest request,  HttpServletResponse response,  FilterChain filterChain)
            throws ServletException, IOException {

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
        } catch (Exception ex) {
            logger.error("JWT认证失败", ex);
        }

        filterChain.doFilter(request, response);
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


