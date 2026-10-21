package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class SystemConfigUpdateRequestDto {
    
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    private String description;

    private String configType;
    
    // 无参构造器
    public SystemConfigUpdateRequestDto() {
    }
    
    // 全参构造器
    public SystemConfigUpdateRequestDto(String configKey, String configValue, String description, String configType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.configType = configType;
    }
    
    // Getter 和 Setter 方法
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getConfigType() {
        return configType;
    }
    
    public void setConfigType(String configType) {
        this.configType = configType;
    }
    
    // Builder 类
    public static class Builder {
        private String configKey;
        private String configValue;
        private String description;
        private String configType;
        
        public Builder configKey(String configKey) {
            this.configKey = configKey;
            return this;
        }
        
        public Builder configValue(String configValue) {
            this.configValue = configValue;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder configType(String configType) {
            this.configType = configType;
            return this;
        }
        
        public SystemConfigUpdateRequestDto build() {
            return new SystemConfigUpdateRequestDto(configKey, configValue, description, configType);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


