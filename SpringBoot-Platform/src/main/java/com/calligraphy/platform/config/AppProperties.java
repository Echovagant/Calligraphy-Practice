package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用程序基本信息配置类
 * 用于处理app.*前缀的配置属性，避免未知属性警告
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用版本
     */
    private String version;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 获取应用名称
     * @return 应用名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置应用名称
     * @param name 应用名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取应用版本
     * @return 应用版本
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 设置应用版本
     * @param version 应用版本
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * 获取应用描述
     * @return 应用描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置应用描述
     * @param description 应用描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
}


