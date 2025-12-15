package com.calligraphy.platform.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings( CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 浣跨敤allowedOriginPatterns鏇夸唬allowedOrigins浠ユ敮鎸乤llowCredentials=true
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}


