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
     * 鎵归噺鏇存柊閰嶇疆鍊?     * @param configs 閰嶇疆鏄犲皠
     */
    void updateConfigValues(Map<String, String> configs);
    
    /**
     * 鑾峰彇鎵€鏈夐厤缃?     * @return 鎵€鏈夐厤缃殑鏄犲皠
     */
    Map<String, String> getAllConfigs();
    
    /**
     * 鑾峰彇鎸囧畾鍒嗙粍鐨勯厤缃?     * @param group 閰嶇疆鍒嗙粍
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
     * 鍒犻櫎閰嶇疆
     * @param key 閰嶇疆閿?     */
    void deleteConfig(String key);
    
    /**
     * 鑾峰彇绯荤粺淇℃伅
     * @return 绯荤粺淇℃伅鏄犲皠
     */
    Map<String, Object> getSystemInfo();
    
    /**
     * 鑾峰彇涓婁紶閰嶇疆
     * @return 涓婁紶閰嶇疆鏄犲皠
     */
    Map<String, Object> getUploadConfig();
    
    /**
     * 鑾峰彇瀹夊叏閰嶇疆
     * @return 瀹夊叏閰嶇疆鏄犲皠
     */
    Map<String, Object> getSecurityConfig();
    
    /**
     * 鑾峰彇閭欢閰嶇疆
     * @return 閭欢閰嶇疆鏄犲皠
     */
    Map<String, Object> getEmailConfig();
    
    /**
     * 鍒锋柊閰嶇疆缂撳瓨
     */
    void refreshConfigCache();
    
    /**
     * 鏇存柊閰嶇疆
     * @param updateRequest 鏇存柊璇锋眰
     * @param updatedBy 鏇存柊鑰?     * @return 鏇存柊鍚庣殑閰嶇疆
     */
    SystemConfigDto updateConfig(SystemConfigUpdateRequestDto updateRequest, String updatedBy);
    
    /**
     * 鑾峰彇閰嶇疆
     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆
     */
    SystemConfigDto getConfig(String configKey);
    
    /**
     * 鑾峰彇鎵€鏈夐厤缃瓺TO
     * @return 鎵€鏈夐厤缃瓺TO鍒楄〃
     */
    List<SystemConfigDto> getAllConfigsDto();
    
    /**
     * 鑾峰彇閰嶇疆鍊间綔涓烘暣鏁?     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆鍊间綔涓烘暣鏁?     */
    Integer getConfigValueAsInt(String configKey);
    
    /**
     * 鑾峰彇閰嶇疆鍊间綔涓哄竷灏斿€?     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆鍊间綔涓哄竷灏斿€?     */
    Boolean getConfigValueAsBoolean(String configKey);
    
    /**
     * 鑾峰彇閰嶇疆鍊间綔涓洪暱鏁存暟
     * @param configKey 閰嶇疆閿?     * @return 閰嶇疆鍊间綔涓洪暱鏁存暟
     */
    Long getConfigValueAsLong(String configKey);
    
    /**
     * 鎵归噺鏇存柊閰嶇疆
     * @param updateRequests 鏇存柊璇锋眰鍒楄〃
     * @param updatedBy 鏇存柊鑰?     */
    void batchUpdateConfigs(List<SystemConfigUpdateRequestDto> updateRequests, String updatedBy);
    
    /**
     * 鎸夌被鍨嬭幏鍙栭厤缃?     * @param configType 閰嶇疆绫诲瀷
     * @return 閰嶇疆鍒楄〃
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


