package com.calligraphy.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 允许携带凭据跨域的前端来源（协议+域名+端口写全，逗号分隔），
     * 由 {@code app.cors.allowed-origins} 配置，默认是本地开发端口。
     * 不使用通配符，避免任意站点携带凭据访问接口。
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
    private List<String> allowedOrigins;

    /**
     * 上传文件的根目录。与 {@code FileUploadServiceImpl} 的 {@code file.upload-dir}
     * 是**同一个**属性 —— 写文件与读文件必须解析到同一个路径。
     */
    @Value("${file.upload-dir:uploads}")
    private String fileUploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射：把 {@code /api/files/**} 指向上传目录。
     *
     * <p>URL 与磁盘布局是同一套：{@code /api/files/{fileType}/{userId}/{yyyy/MM/dd}/{filename}}
     * ↔ {@code {file.upload-dir}/{fileType}/{userId}/{yyyy/MM/dd}/{filename}}。
     * 这个 URL 形式由 {@code FileUploadServiceImpl.buildFileUrl} 生成，不要单方面改。
     *
     * <p>相对路径按**进程工作目录**解析，与写文件时的解析方式一致，
     * 所以两边永远指向同一个目录。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(fileUploadDir).toAbsolutePath().normalize().toString();
        if (!location.endsWith(java.io.File.separator)) {
            location = location + java.io.File.separator;
        }
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + location);
    }

    /**
     * 提供给 Spring Security 过滤链复用的 CORS 配置源。
     * 非 static：要读上面注入的 {@code allowedOrigins}，
     * SecurityConfig 通过注入本 Bean 使用（不再静态直调）。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
