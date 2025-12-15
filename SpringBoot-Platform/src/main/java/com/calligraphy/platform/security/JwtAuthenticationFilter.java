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
            logger.debug("鏀跺埌JWT浠ょ墝: {}", jwt != null ? "***" : null);

            if (StringUtils.hasText(jwt)) {
                logger.debug("楠岃瘉JWT浠ょ墝...");
                boolean isValid = jwtService.validateToken(jwt);
                logger.debug("JWT浠ょ墝楠岃瘉缁撴灉: {}", isValid);
                
                if (isValid) {
                    String username = jwtService.extractUsername(jwt);
                    logger.debug("浠嶫WT浠ょ墝涓幏鍙栧埌鐢ㄦ埛鍚? {}", username);
                    
                    // 鐩存帴浣跨敤鐢ㄦ埛鍚嶆潵鍔犺浇鐢ㄦ埛淇℃伅
                    logger.debug("浣跨敤鐢ㄦ埛鍚嶅姞杞界敤鎴蜂俊鎭? username={}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    logger.debug("鎴愬姛鍔犺浇鐢ㄦ埛淇℃伅: username={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("JWT璁よ瘉鎴愬姛 - 鐢ㄦ埛: {}", username);
                }
            }
        } catch (Exception ex) {
            logger.error("JWT璁よ瘉澶辫触", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 浠庤姹備腑鑾峰彇JWT浠ょ墝
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


