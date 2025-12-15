package com.calligraphy.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("璁块棶琚嫆缁?- 鐢ㄦ埛: {}, 璺緞: {}, 鍘熷洜: {}", 
                request.getRemoteUser(), request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "璁块棶琚嫆缁濓紝鏉冮檺涓嶈冻");
        errorResponse.put("error", "FORBIDDEN");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", System.currentTimeMillis());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}


