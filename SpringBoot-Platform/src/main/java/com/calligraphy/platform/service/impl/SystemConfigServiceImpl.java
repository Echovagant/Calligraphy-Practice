package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.SystemConfigDto;
import com.calligraphy.platform.dto.SystemConfigUpdateRequestDto;
import com.calligraphy.platform.entity.SystemConfig;
import com.calligraphy.platform.exception.ResourceNotFoundException;

import com.calligraphy.platform.repository.SystemConfigRepository;
import com.calligraphy.platform.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    private final SystemConfigRepository systemConfigRepository;

    // 手动实现DTO转换
    private SystemConfigDto toDto(SystemConfig config) {
        SystemConfigDto dto = new SystemConfigDto();
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setDescription(config.getDescription());
        dto.setConfigType(config.getConfigType());
        dto.setIsEditable(config.getIsEditable());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        dto.setUpdatedBy(config.getUpdatedBy());
        dto.setConfigGroup(config.getConfigGroup());
        dto.setDefaultValue(config.getDefaultValue());
        return dto;
    }
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#updateRequest.configKey")
    public SystemConfigDto updateConfig(SystemConfigUpdateRequestDto updateRequest, String updatedBy) {
        log.info("更新系统配置: key={}, 更新者={}", updateRequest.getConfigKey(), updatedBy);

        SystemConfig config = systemConfigRepository.findByConfigKey(updateRequest.getConfigKey())
                .orElseGet(() -> {
                    // 如果配置不存在，创建新的配置
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(updateRequest.getConfigKey());
                    newConfig.setConfigValue(updateRequest.getConfigValue());
                    newConfig.setDescription(updateRequest.getDescription());
                    newConfig.setConfigType(
                            updateRequest.getConfigType() != null ? updateRequest.getConfigType() : "STRING");
                    newConfig.setIsEditable(true);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    newConfig.setUpdatedAt(LocalDateTime.now());
                    newConfig.setUpdatedBy(updatedBy);

                    log.info("创建新的系统配置: key={}", updateRequest.getConfigKey());
                    return newConfig;
                });

        // 更新配置值
        if (updateRequest.getConfigValue() != null) {
            config.setConfigValue(updateRequest.getConfigValue());
        }

        if (updateRequest.getDescription() != null) {
            config.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getConfigType() != null) {
            config.setConfigType(updateRequest.getConfigType());
        }

        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);

        SystemConfig updatedConfig = systemConfigRepository.save(config);
        log.info("系统配置更新成功: key={}", updateRequest.getConfigKey());

        return toDto(updatedConfig);
    }
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", key = "#configKey")
    public void deleteConfig(String configKey) {
        log.info("删除系统配置: key={}", configKey);

        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("系统配置不存在: " + configKey));

        if (!config.getIsEditable()) {
            throw new SecurityException("该配置项不允许删除");
        }

        systemConfigRepository.delete(config);
        log.info("系统配置删除成功: key={}", configKey);
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey")
    public SystemConfigDto getConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("系统配置不存在: " + configKey));

        return toDto(config);
    }
    @Override
    @Cacheable(value = "systemConfig", key = "'all'")
    public List<SystemConfigDto> getAllConfigsDto() {
        List<SystemConfig> configs = systemConfigRepository.findAllByOrderByConfigKeyAsc();
        return configs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey + '_value'")
    public String getConfigValue(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("系统配置不存在: " + configKey));

        return config.getConfigValue();
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey + '_int'")
    public Integer getConfigValueAsInt(String configKey) {
        String value = getConfigValue(configKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("配置值转换整数失败: key={}, value={}", configKey, value);
            throw new IllegalArgumentException("配置值不是有效的整数: " + configKey);
        }
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey + '_boolean'")
    public Boolean getConfigValueAsBoolean(String configKey) {
        String value = getConfigValue(configKey);
        return Boolean.parseBoolean(value);
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey + '_long'")
    public Long getConfigValueAsLong(String configKey) {
        String value = getConfigValue(configKey);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("配置值转换长整数失败: key={}, value={}", configKey, value);
            throw new IllegalArgumentException("配置值不是有效的长整数: " + configKey);
        }
    }
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void batchUpdateConfigs(List<SystemConfigUpdateRequestDto> updateRequests, String updatedBy) {
        log.info("批量更新系统配置: 数量={}, 更新者={}", updateRequests.size(), updatedBy);

        for (SystemConfigUpdateRequestDto updateRequest : updateRequests) {
            try {
                updateConfig(updateRequest, updatedBy);
            } catch (Exception e) {
                log.error("更新配置失败: key={}, 错误={}", updateRequest.getConfigKey(), e.getMessage());
                throw new RuntimeException("更新配置失败: " + updateRequest.getConfigKey());
            }
        }

        log.info("批量更新系统配置完成: 数量={}", updateRequests.size());
    }
    @Override
    public List<SystemConfigDto> getConfigsByType(String configType) {
        List<SystemConfig> configs = systemConfigRepository.findByConfigTypeOrderByConfigKeyAsc(configType);
        return configs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<SystemConfigDto> getEditableConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findByIsEditableTrueOrderByConfigKeyAsc();
        return configs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void resetConfigToDefault(String configKey, String defaultValue, String updatedBy) {
        log.info("重置系统配置到默认值: key={}, 更新者={}", configKey, updatedBy);

        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("系统配置不存在: " + configKey));

        if (!config.getIsEditable()) {
            throw new SecurityException("该配置项不允许重置");
        }

        config.setConfigValue(defaultValue);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);

        systemConfigRepository.save(config);
        log.info("系统配置重置成功: key={}", configKey);
    }

    // ===== SystemConfigService 接口方法实现（Map结构）======
    @Override
    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }
    @Override
    public boolean getBooleanConfigValue(String key) {
        return Boolean.parseBoolean(getConfigValue(key));
    }
    @Override
    public boolean getBooleanConfigValue(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getConfigValue(key, String.valueOf(defaultValue)));
    }
    @Override
    public int getIntConfigValue(String key) {
        try {
            return Integer.parseInt(getConfigValue(key));
        } catch (Exception e) {
            return 0;
        }
    }
    @Override
    public int getIntConfigValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(getConfigValue(key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }
    @Override
    public void updateConfigValue(String key, String value) {
        SystemConfig cfg = systemConfigRepository.findByConfigKey(key)
                .orElseGet(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(key);
                    newConfig.setIsEditable(true);
                    newConfig.setCreatedAt(LocalDateTime.now());
                    newConfig.setUpdatedAt(LocalDateTime.now());
                    newConfig.setUpdatedBy("system");
                    return newConfig;
                });
        cfg.setConfigValue(value);
        cfg.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(cfg);
    }
    @Override
    public void updateConfigValues(java.util.Map<String, String> configs) {
        configs.forEach(this::updateConfigValue);
    }
    @Override
    public java.util.Map<String, String> getAllConfigs() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        systemConfigRepository.findAllByOrderByConfigKeyAsc()
                .forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return map;
    }
    @Override
    public java.util.Map<String, String> getConfigsByGroup(String group) {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        systemConfigRepository.findByConfigGroupOrderByConfigKey(group)
                .forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return map;
    }
    @Override
    public void resetConfigToDefault(String key) {
        systemConfigRepository.findByConfigKey(key).ifPresent(c -> {
            c.setConfigValue(c.getDefaultValue());
            c.setUpdatedAt(LocalDateTime.now());
            systemConfigRepository.save(c);
        });
    }
    @Override
    public void resetAllConfigsToDefault() {
        systemConfigRepository.findAll().forEach(c -> {
            c.setConfigValue(c.getDefaultValue());
            c.setUpdatedAt(LocalDateTime.now());
            systemConfigRepository.save(c);
        });
    }
    @Override
    public boolean configExists(String key) {
        return systemConfigRepository.existsByConfigKey(key);
    }
    @Override
    public java.util.Map<String, Object> getSystemInfo() {
        return java.util.Map.of(
                "appName", "Calligraphy Platform",
                "version", "1.0.0",
                "time", LocalDateTime.now().toString());
    }
    @Override
    public java.util.Map<String, Object> getUploadConfig() {
        return java.util.Map.of(
                "maxFileSize", 10485760,
                "allowedTypes", java.util.List.of("jpg", "jpeg", "png", "gif", "pdf"));
    }
    @Override
    public java.util.Map<String, Object> getSecurityConfig() {
        return java.util.Map.of(
                "jwtExpiration", 3600000,
                "jwtRefreshExpiration", 2592000000L);
    }
    @Override
    public java.util.Map<String, Object> getEmailConfig() {
        return java.util.Map.of(
                "enabled", false,
                "smtpHost", "",
                "smtpPort", 587);
    }
    @Override
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void refreshConfigCache() {
    }
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void initializeDefaultConfigs() {
        log.info("初始化默认系统配置");

        // 网站基本配置
        createDefaultConfigIfNotExists("site.name", "书法练习平台", "网站名称", "STRING", false);
        createDefaultConfigIfNotExists("site.description", "专业的书法练习平台", "网站描述", "STRING", false);
        createDefaultConfigIfNotExists("site.keywords", "书法,练习,毛笔字,硬笔字", "网站关键词", "STRING", true);
        createDefaultConfigIfNotExists("site.logo", "/assets/logo.png", "网站Logo", "STRING", true);
        createDefaultConfigIfNotExists("site.favicon", "/assets/favicon.ico", "网站图标", "STRING", true);

        // 用户相关配置
        createDefaultConfigIfNotExists("user.registration.enabled", "true", "是否允许用户注册", "BOOLEAN", true);
        createDefaultConfigIfNotExists("user.email.verification.required", "false", "是否需要邮箱验证", "BOOLEAN", true);
        createDefaultConfigIfNotExists("user.avatar.default", "/assets/default-avatar.png", "默认头像", "STRING", true);
        createDefaultConfigIfNotExists("user.password.min.length", "6", "密码最小长度", "INTEGER", true);
        createDefaultConfigIfNotExists("user.login.max.attempts", "5", "登录最大尝试次数", "INTEGER", true);

        // 作品相关配置
        createDefaultConfigIfNotExists("work.upload.enabled", "true", "是否允许上传作品", "BOOLEAN", true);
        createDefaultConfigIfNotExists("work.upload.max.size", "10485760", "作品上传最大大小(字节)", "LONG", true); // 10MB
        createDefaultConfigIfNotExists("work.upload.allowed.types", "jpg,jpeg,png,gif,pdf", "允许上传的文件类型", "STRING",
                true);
        createDefaultConfigIfNotExists("work.auto.approve", "true", "作品是否自动审核通过", "BOOLEAN", true);
        createDefaultConfigIfNotExists("work.comment.enabled", "true", "是否允许评论作品", "BOOLEAN", true);

        // 审核相关配置
        createDefaultConfigIfNotExists("audit.work.enabled", "true", "是否启用作品审核", "BOOLEAN", true);
        createDefaultConfigIfNotExists("audit.comment.enabled", "true", "是否启用评论审核", "BOOLEAN", true);
        createDefaultConfigIfNotExists("audit.auto.approve.threshold", "10", "自动审核通过阈值", "INTEGER", true);

        // 存储相关配置
        createDefaultConfigIfNotExists("storage.type", "local", "存储类型(local,aliyun,qiniu)", "STRING", true);
        createDefaultConfigIfNotExists("storage.local.path", "/uploads/", "本地存储路径", "STRING", true);
        createDefaultConfigIfNotExists("storage.cdn.enabled", "false", "是否启用CDN", "BOOLEAN", true);
        createDefaultConfigIfNotExists("storage.cdn.domain", "", "CDN域名", "STRING", true);

        // JWT配置
        createDefaultConfigIfNotExists("jwt.user.expiration", "86400", "用户JWT过期时间(秒)", "LONG", true); // 24小时
        createDefaultConfigIfNotExists("jwt.admin.expiration", "3600", "管理员JWT过期时间(秒)", "LONG", true); // 1小时
        createDefaultConfigIfNotExists("jwt.refresh.expiration", "604800", "刷新令牌过期时间(秒)", "LONG", true); // 7天 
        // 邮件配置
        createDefaultConfigIfNotExists("email.enabled", "false", "是否启用邮件服务", "BOOLEAN", true);
        createDefaultConfigIfNotExists("email.smtp.host", "", "SMTP服务器地址", "STRING", true);
        createDefaultConfigIfNotExists("email.smtp.port", "587", "SMTP服务器端口", "INTEGER", true);
        createDefaultConfigIfNotExists("email.smtp.username", "", "SMTP用户名", "STRING", true);
        createDefaultConfigIfNotExists("email.smtp.password", "", "SMTP密码", "STRING", true);
        createDefaultConfigIfNotExists("email.from.address", "", "发件人邮箱", "STRING", true);

        // 分页配置
        createDefaultConfigIfNotExists("pagination.default.size", "20", "默认分页大小", "INTEGER", true);
        createDefaultConfigIfNotExists("pagination.max.size", "100", "最大分页大小", "INTEGER", true);

        log.info("默认系统配置初始化完成");
    }

    private void createDefaultConfigIfNotExists(String configKey, String defaultValue, String description,
            String configType, boolean isEditable) {
        if (!systemConfigRepository.existsByConfigKey(configKey)) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(defaultValue);
            config.setDescription(description);
            config.setConfigType(configType);
            config.setIsEditable(isEditable);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy("system");

            systemConfigRepository.save(config);
            log.info("创建默认配置: key={}, value={}", configKey, defaultValue);
        }
    }
}


