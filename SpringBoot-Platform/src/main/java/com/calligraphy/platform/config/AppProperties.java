package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 搴旂敤绋嬪簭鍩烘湰淇℃伅閰嶇疆绫? * 用于处理app.*鍓嶇紑鐨勯厤缃睘鎬э紝閬垮厤鏈煡灞炴€ц鍛? */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    /**
     * 搴旂敤鍚嶇О
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
     * 鑾峰彇搴旂敤鍚嶇О
     * @return 搴旂敤鍚嶇О
     */
    public String getName() {
        return name;
    }
    
    /**
     * 璁剧疆搴旂敤鍚嶇О
     * @param name 搴旂敤鍚嶇О
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


