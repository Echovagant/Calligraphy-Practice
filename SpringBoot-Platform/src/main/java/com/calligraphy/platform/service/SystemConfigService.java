package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.SystemConfigDto;
import com.calligraphy.platform.dto.SystemConfigUpdateRequestDto;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {
    
    /**
     * 鍒濆鍖栭粯璁ょ郴缁熼厤缃?     */
    void initializeDefaultConfigs();
    
    /**
     * 鑾峰彇閰嶇疆鍊?     * @param key 閰嶇疆閿?     * @return 閰嶇疆鍊?     */
    String getConfigValue(String key);
    
    /**
     * 鑾峰彇閰嶇疆鍊硷紝濡傛灉涓嶅瓨鍦ㄥ垯杩斿洖榛樿鍊?     * @param key 閰嶇疆閿?     * @param defaultValue 榛樿鍊?     * @return 閰嶇疆鍊兼垨榛樿鍊?     */
    String getConfigValue(String key, String defaultValue);
    
    /**
     * 鑾峰彇甯冨皵绫诲瀷鐨勯厤缃€?     * @param key 閰嶇疆閿?     * @return 甯冨皵鍊?     */
    boolean getBooleanConfigValue(String key);
    
    /**
     * 鑾峰彇甯冨皵绫诲瀷鐨勯厤缃€硷紝濡傛灉涓嶅瓨鍦ㄥ垯杩斿洖榛樿鍊?     * @param key 閰嶇疆閿?     * @param defaultValue 榛樿鍊?     * @return 甯冨皵鍊兼垨榛樿鍊?     */
    boolean getBooleanConfigValue(String key, boolean defaultValue);
    
    /**
     * 鑾峰彇鏁存暟绫诲瀷鐨勯厤缃€?     * @param key 閰嶇疆閿?     * @return 鏁存暟鍊?     */
    int getIntConfigValue(String key);
    
    /**
     * 鑾峰彇鏁存暟绫诲瀷鐨勯厤缃€硷紝濡傛灉涓嶅瓨鍦ㄥ垯杩斿洖榛樿鍊?     * @param key 閰嶇疆閿?     * @param defaultValue 榛樿鍊?     * @return 鏁存暟鍊兼垨榛樿鍊?     */
    int getIntConfigValue(String key, int defaultValue);
    
    /**
     * 鏇存柊閰嶇疆鍊?     * @param key 閰嶇疆閿?     * @param value 閰嶇疆鍊?     */
    void updateConfigValue(String key, String value);
    
    /**
     * 鎵归噺鏇存柊閰嶇疆鍊?     * @param configs 配置映射
     */
    void updateConfigValues(Map<String, String> configs);
    
    /**
     * 鑾峰彇鎵€鏈夐厤缃?     * @return 鎵€鏈夐厤缃殑鏄犲皠
     */
    Map<String, String> getAllConfigs();
    
    /**
     * 鑾峰彇鎸囧畾鍒嗙粍鐨勯厤缃?     * @param group 配置分组
     * @return 鍒嗙粍閰嶇疆鐨勬槧灏?     */
    Map<String, String> getConfigsByGroup(String group);
    
    /**
     * 閲嶇疆閰嶇疆涓洪粯璁ゅ€?     * @param key 閰嶇疆閿?     */
    void resetConfigToDefault(String key);
    
    /**
     * 閲嶇疆鎵€鏈夐厤缃负榛樿鍊?     */
    void resetAllConfigsToDefault();
    
    /**
     * 妫€鏌ラ厤缃槸鍚﹀瓨鍦?     * @param key 閰嶇疆閿?     * @return 鏄惁瀛樺湪
     */
    boolean configExists(String key);
    
    /**
     * 删除配置
     * @param key 閰嶇疆閿?     */
    void deleteConfig(String key);
    
    /**
     * 鑾峰彇绯荤粺淇℃伅
     * @return 绯荤粺淇℃伅鏄犲皠
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
     * 鑾峰彇閭欢閰嶇疆
     * @return 閭欢閰嶇疆鏄犲皠
     */
    Map<String, Object> getEmailConfig();
    
    /**
     * 刷新配置缓存
     */
    void refreshConfigCache();
    
    /**
     * 更新配置
     * @param updateRequest 更新请求
     * @param updatedBy 鏇存柊鑰?     * @return 更新后的配置
     */
    SystemConfigDto updateConfig(SystemConfigUpdateRequestDto updateRequest, String updatedBy);
    
    /**
     * 获取配置
     * @param configKey 閰嶇疆閿?     * @return 配置
     */
    SystemConfigDto getConfig(String configKey);
    
    /**
     * 鑾峰彇鎵€鏈夐厤缃瓺TO
     * @return 鎵€鏈夐厤缃瓺TO列表
     */
    List<SystemConfigDto> getAllConfigsDto();
    
    /**
     * 鑾峰彇閰嶇疆鍊间綔涓烘暣鏁?     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆鍊间綔涓烘暣鏁?     */
    Integer getConfigValueAsInt(String configKey);
    
    /**
     * 鑾峰彇閰嶇疆鍊间綔涓哄竷灏斿€?     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆鍊间綔涓哄竷灏斿€?     */
    Boolean getConfigValueAsBoolean(String configKey);
    
    /**
     * 获取配置值作为长整数
     * @param configKey 閰嶇疆閿?     * @return 配置值作为长整数
     */
    Long getConfigValueAsLong(String configKey);
    
    /**
     * 批量更新配置
     * @param updateRequests 更新请求列表
     * @param updatedBy 鏇存柊鑰?     */
    void batchUpdateConfigs(List<SystemConfigUpdateRequestDto> updateRequests, String updatedBy);
    
    /**
     * 鎸夌被鍨嬭幏鍙栭厤缃?     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfigDto> getConfigsByType(String configType);
    
    /**
     * 鑾峰彇鍙紪杈戠殑閰嶇疆
     * @return 鍙紪杈戠殑閰嶇疆鍒楄〃
     */
    List<SystemConfigDto> getEditableConfigs();
    
    /**
     * 閲嶇疆閰嶇疆鍒伴粯璁ゅ€?     * @param configKey 閰嶇疆閿?     * @param defaultValue 榛樿鍊?     * @param updatedBy 鏇存柊鑰?     */
    void resetConfigToDefault(String configKey, String defaultValue, String updatedBy);
}


