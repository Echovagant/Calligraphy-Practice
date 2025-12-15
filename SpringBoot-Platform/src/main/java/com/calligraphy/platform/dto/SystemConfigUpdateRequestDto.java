package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class SystemConfigUpdateRequestDto {
    
    @NotBlank(message = "閰嶇疆閿笉鑳戒负绌?)
    private String configKey;
    
    @NotBlank(message = "閰嶇疆鍊间笉鑳戒负绌?)
    private String configValue;
    
    private String description;

    private String configType;
    
    // 鏃犲弬鏋勯€犲櫒
    public SystemConfigUpdateRequestDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
    public SystemConfigUpdateRequestDto(String configKey, String configValue, String description, String configType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.configType = configType;
    }
    
    // Getter 鍜?Setter 鏂规硶
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
    
    // Builder 绫?    public static class Builder {
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


