package com.calligraphy.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        log.warn("鏈巿鏉冭闂? {} {} - {}", request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "鏈巿鏉冭闂紝璇峰厛鐧诲綍");
        errorResponse.put("path", request.getRequestURI());

        // 娣诲姞寮€鍙戞ā寮忎笅鐨勮缁嗕俊鎭?        String activeProfile = request.getServletContext().getInitParameter("spring.profiles.active");
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            errorResponse.put("debug", authException.getMessage());
        }

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}


