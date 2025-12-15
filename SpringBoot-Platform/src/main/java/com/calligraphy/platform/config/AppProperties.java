package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 搴旂敤绋嬪簭鍩烘湰淇℃伅閰嶇疆绫? * 鐢ㄤ簬澶勭悊app.*鍓嶇紑鐨勯厤缃睘鎬э紝閬垮厤鏈煡灞炴€ц鍛? */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    /**
     * 搴旂敤鍚嶇О
     */
    private String name;
    
    /**
     * 搴旂敤鐗堟湰
     */
    private String version;
    
    /**
     * 搴旂敤鎻忚堪
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
     * 鑾峰彇搴旂敤鐗堟湰
     * @return 搴旂敤鐗堟湰
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 璁剧疆搴旂敤鐗堟湰
     * @param version 搴旂敤鐗堟湰
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * 鑾峰彇搴旂敤鎻忚堪
     * @return 搴旂敤鎻忚堪
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 璁剧疆搴旂敤鎻忚堪
     * @param description 搴旂敤鎻忚堪
     */
    public void setDescription(String description) {
        this.description = description;
    }
}


