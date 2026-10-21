package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.SystemConfigDto;
import com.calligraphy.platform.dto.SystemConfigUpdateRequestDto;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {
    
    /**
     * 初始化默认系统配置
     */
    void initializeDefaultConfigs();
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    String getConfigValue(String key);
    
    /**
     * 获取配置值，如果不存在则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    String getConfigValue(String key, String defaultValue);
    
    /**
     * 获取布尔类型的配置值
     * @param key 配置键
     * @return 布尔值
     */
    boolean getBooleanConfigValue(String key);
    
    /**
     * 获取布尔类型的配置值，如果不存在则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 布尔值或默认值
     */
    boolean getBooleanConfigValue(String key, boolean defaultValue);
    
    /**
     * 获取整数类型的配置值
     * @param key 配置键
     * @return 整数值
     */
    int getIntConfigValue(String key);
    
    /**
     * 获取整数类型的配置值，如果不存在则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 整数值或默认值
     */
    int getIntConfigValue(String key, int defaultValue);
    
    /**
     * 更新配置值
     * @param key 配置键
     * @param value 配置值
     */
    void updateConfigValue(String key, String value);
    
    /**
     * 批量更新配置值
     * @param configs 配置映射
     */
    void updateConfigValues(Map<String, String> configs);
    
    /**
     * 获取所有配置
     * @return 所有配置的映射
     */
    Map<String, String> getAllConfigs();
    
    /**
     * 获取指定分组的配置
     * @param group 配置分组
     * @return 分组配置的映射
     */
    Map<String, String> getConfigsByGroup(String group);
    
    /**
     * 重置配置为默认值
     * @param key 配置键
     */
    void resetConfigToDefault(String key);
    
    /**
     * 重置所有配置为默认值
     */
    void resetAllConfigsToDefault();
    
    /**
     * 检查配置是否存在
     * @param key 配置键
     * @return 是否存在
     */
    boolean configExists(String key);
    
    /**
     * 删除配置
     * @param key 配置键
     */
    void deleteConfig(String key);
    
    /**
     * 获取系统信息
     * @return 系统信息映射
     */
    Map<String, Object> getSystemInfo();
    
    /**
     * 获取上传配置
     * @return 上传配置映射
     */
    Map<String, Object> getUploadConfig();
    
    /**
     * 获取安全配置
     * @return 安全配置映射
     */
    Map<String, Object> getSecurityConfig();
    
    /**
     * 获取邮件配置
     * @return 邮件配置映射
     */
    Map<String, Object> getEmailConfig();
    
    /**
     * 刷新配置缓存
     */
    void refreshConfigCache();
    
    /**
     * 更新配置
     * @param updateRequest 更新请求
     * @param updatedBy 更新者
     * @return 更新后的配置
     */
    SystemConfigDto updateConfig(SystemConfigUpdateRequestDto updateRequest, String updatedBy);
    
    /**
     * 获取配置
     * @param configKey 配置键
     * @return 配置
     */
    SystemConfigDto getConfig(String configKey);
    
    /**
     * 获取所有配置DTO
     * @return 所有配置DTO列表
     */
    List<SystemConfigDto> getAllConfigsDto();
    
    /**
     * 获取配置值作为整数
     * @param configKey 配置键
     * @return 配置值作为整数
     */
    Integer getConfigValueAsInt(String configKey);
    
    /**
     * 获取配置值作为布尔值
     * @param configKey 配置键
     * @return 配置值作为布尔值
     */
    Boolean getConfigValueAsBoolean(String configKey);
    
    /**
     * 获取配置值作为长整数
     * @param configKey 配置键
     * @return 配置值作为长整数
     */
    Long getConfigValueAsLong(String configKey);
    
    /**
     * 批量更新配置
     * @param updateRequests 更新请求列表
     * @param updatedBy 更新者
     */
    void batchUpdateConfigs(List<SystemConfigUpdateRequestDto> updateRequests, String updatedBy);
    
    /**
     * 按类型获取配置
     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfigDto> getConfigsByType(String configType);
    
    /**
     * 获取可编辑的配置
     * @return 可编辑的配置列表
     */
    List<SystemConfigDto> getEditableConfigs();
    
    /**
     * 重置配置到默认值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @param updatedBy 更新者
     */
    void resetConfigToDefault(String configKey, String defaultValue, String updatedBy);
}


