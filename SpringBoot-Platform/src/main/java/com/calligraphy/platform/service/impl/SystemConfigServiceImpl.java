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

    // 手动实现DTO杞崲
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
        log.info("更新系统配置: key={}, 鏇存柊鑰?{}", updateRequest.getConfigKey(), updatedBy);

        SystemConfig config = systemConfigRepository.findByConfigKey(updateRequest.getConfigKey())
                .orElseGet(() -> {
                    // 濡傛灉閰嶇疆涓嶅瓨鍦紝鍒涘缓鏂扮殑閰嶇疆
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

        // 鏇存柊閰嶇疆鍊
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
                .orElseThrow(() -> new ResourceNotFoundException("绯荤粺閰嶇疆涓嶅瓨鍦? " + configKey));

        if (!config.getIsEditable()) {
            throw new SecurityException("璇ラ厤缃」涓嶅厑璁稿垹闄");
        }

        systemConfigRepository.delete(config);
        log.info("系统配置删除成功: key={}", configKey);
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey")
    public SystemConfigDto getConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("绯荤粺閰嶇疆涓嶅瓨鍦? " + configKey));

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
                .orElseThrow(() -> new ResourceNotFoundException("绯荤粺閰嶇疆涓嶅瓨鍦? " + configKey));

        return config.getConfigValue();
    }
    @Override
    @Cacheable(value = "systemConfig", key = "#configKey + '_int'")
    public Integer getConfigValueAsInt(String configKey) {
        String value = getConfigValue(configKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("閰嶇疆鍊艰浆鎹㈡暣鏁板け璐? key={}, value={}", configKey, value);
            throw new IllegalArgumentException("閰嶇疆鍊间笉鏄湁鏁堢殑鏁存暟: " + configKey);
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
            throw new IllegalArgumentException("閰嶇疆鍊间笉鏄湁鏁堢殑闀挎暣鏁? " + configKey);
        }
    }
    @Override
    @Transactional
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void batchUpdateConfigs(List<SystemConfigUpdateRequestDto> updateRequests, String updatedBy) {
        log.info("批量更新系统配置: 数量={}, 鏇存柊鑰?{}", updateRequests.size(), updatedBy);

        for (SystemConfigUpdateRequestDto updateRequest : updateRequests) {
            try {
                updateConfig(updateRequest, updatedBy);
            } catch (Exception e) {
                log.error("更新配置失败: key={}, 閿欒={}", updateRequest.getConfigKey(), e.getMessage());
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
        log.info("閲嶇疆绯荤粺閰嶇疆鍒伴粯璁ゅ€? key={}, 鏇存柊鑰?{}", configKey, updatedBy);

        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("绯荤粺閰嶇疆涓嶅瓨鍦? " + configKey));

        if (!config.getIsEditable()) {
            throw new SecurityException("璇ラ厤缃」涓嶅厑璁搁噸缃");
        }

        config.setConfigValue(defaultValue);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);

        systemConfigRepository.save(config);
        log.info("系统配置重置成功: key={}", configKey);
    }

    // ===== SystemConfigService 鎺ュ彛鏂规硶瀹炵幇锛圡ap缁撴瀯锛?=====
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
        log.info("鍒濆鍖栭粯璁ょ郴缁熼厤缃");

        // 网站基本配置
        createDefaultConfigIfNotExists("site.name", "涔︽硶缁冧範骞冲彴", "缃戠珯鍚嶇О", "STRING", false);
        createDefaultConfigIfNotExists("site.description", "涓撲笟鐨勪功娉曠粌涔犲钩鍙", "网站描述", "STRING", false);
        createDefaultConfigIfNotExists("site.keywords", "涔︽硶,练习,姣涚瑪瀛?纭瑪瀛?, "缃戠珯鍏抽敭璇?, "STRING", true);
        createDefaultConfigIfNotExists("site.logo", "/assets/logo.png", "网站Logo", "STRING", true);
        createDefaultConfigIfNotExists("site.favicon", "/assets/favicon.ico", "网站图标", "STRING", true);

        // 用户相关配置
        createDefaultConfigIfNotExists("user.registration.enabled", "true", "鏄惁鍏佽用户注册", "BOOLEAN", true);
        createDefaultConfigIfNotExists("user.email.verification.required", "false", "鏄惁闇€瑕侀偖绠遍獙璇", "BOOLEAN", true);
        createDefaultConfigIfNotExists("user.avatar.default", "/assets/default-avatar.png", "榛樿头像", "STRING", true);
        createDefaultConfigIfNotExists("user.password.min.length", "6", "瀵嗙爜鏈€灏忛暱搴", "INTEGER", true);
        createDefaultConfigIfNotExists("user.login.max.attempts", "5", "鐧诲綍鏈€澶у皾璇曟鏁", "INTEGER", true);

        // 作品相关配置
        createDefaultConfigIfNotExists("work.upload.enabled", "true", "鏄惁鍏佽上传作品", "BOOLEAN", true);
        createDefaultConfigIfNotExists("work.upload.max.size", "10485760", "浣滃搧涓婁紶鏈€澶уぇ灏?字节)", "LONG", true); // 10MB
        createDefaultConfigIfNotExists("work.upload.allowed.types", "jpg,jpeg,png,gif,pdf", "鍏佽涓婁紶鐨勬枃浠剁被鍨", "STRING",
                true);
        createDefaultConfigIfNotExists("work.auto.approve", "true", "浣滃搧鏄惁鑷姩瀹℃牳閫氳繃", "BOOLEAN", true);
        createDefaultConfigIfNotExists("work.comment.enabled", "true", "鏄惁鍏佽璇勮作品", "BOOLEAN", true);

        // 瀹℃牳鐩稿叧閰嶇疆
        createDefaultConfigIfNotExists("audit.work.enabled", "true", "鏄惁鍚敤浣滃搧瀹℃牳", "BOOLEAN", true);
        createDefaultConfigIfNotExists("audit.comment.enabled", "true", "鏄惁鍚敤璇勮瀹℃牳", "BOOLEAN", true);
        createDefaultConfigIfNotExists("audit.auto.approve.threshold", "10", "鑷姩瀹℃牳閫氳繃闃堝€", "INTEGER", true);

        // 存储相关配置
        createDefaultConfigIfNotExists("storage.type", "local", "存储类型(local,aliyun,qiniu)", "STRING", true);
        createDefaultConfigIfNotExists("storage.local.path", "/uploads/", "鏈湴瀛樺偍璺緞", "STRING", true);
        createDefaultConfigIfNotExists("storage.cdn.enabled", "false", "鏄惁鍚敤CDN", "BOOLEAN", true);
        createDefaultConfigIfNotExists("storage.cdn.domain", "", "CDN域名", "STRING", true);

        // JWT配置
        createDefaultConfigIfNotExists("jwt.user.expiration", "86400", "用户JWT过期时间(绉?", "LONG", true); // 24小时
        createDefaultConfigIfNotExists("jwt.admin.expiration", "3600", "绠＄悊鍛楯WT过期时间(绉?", "LONG", true); // 1小时
        createDefaultConfigIfNotExists("jwt.refresh.expiration", "604800", "刷新令牌过期时间(绉?", "LONG", true); // 7澶?
        // 閭欢閰嶇疆
        createDefaultConfigIfNotExists("email.enabled", "false", "鏄惁鍚敤閭欢鏈嶅姟", "BOOLEAN", true);
        createDefaultConfigIfNotExists("email.smtp.host", "", "SMTP鏈嶅姟鍣ㄥ湴鍧€", "STRING", true);
        createDefaultConfigIfNotExists("email.smtp.port", "587", "SMTP鏈嶅姟鍣ㄧ鍙", "INTEGER", true);
        createDefaultConfigIfNotExists("email.smtp.username", "", "SMTP鐢ㄦ埛鍚", "STRING", true);
        createDefaultConfigIfNotExists("email.smtp.password", "", "SMTP密码", "STRING", true);
        createDefaultConfigIfNotExists("email.from.address", "", "鍙戜欢浜洪偖绠", "STRING", true);

        // 分页配置
        createDefaultConfigIfNotExists("pagination.default.size", "20", "榛樿鍒嗛〉澶у皬", "INTEGER", true);
        createDefaultConfigIfNotExists("pagination.max.size", "100", "鏈€澶у垎椤靛ぇ灏", "INTEGER", true);

        log.info("榛樿绯荤粺閰嶇疆鍒濆鍖栧畬鎴");
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
            log.info("鍒涘缓榛樿配置: key={}, value={}", configKey, defaultValue);
        }
    }
}


