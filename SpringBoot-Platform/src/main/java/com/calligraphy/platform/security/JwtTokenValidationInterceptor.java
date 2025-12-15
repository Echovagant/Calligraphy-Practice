package com.calligraphy.platform.security;

import com.calligraphy.platform.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtTokenValidationInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidationInterceptor.class);
    
    private final com.calligraphy.platform.util.JwtTokenProvider jwtTokenProvider;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
        
        if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
            
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    log.debug("JWT楠岃瘉鎴愬姛锛岀敤鎴? {}", username);
                    return true;
                }
            } catch (Exception e) {
                log.error("JWT楠岃瘉澶辫触: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                return false;
            }
        }
        
        return true;
    }
}


