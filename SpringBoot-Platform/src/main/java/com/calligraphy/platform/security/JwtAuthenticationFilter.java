package com.calligraphy.platform.security;

import com.calligraphy.platform.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
    
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
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
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


