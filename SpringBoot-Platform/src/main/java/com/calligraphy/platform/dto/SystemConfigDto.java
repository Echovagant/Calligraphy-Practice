package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {
    
    private Long id;
    
    private String configKey;
    
    private String configValue;
    
    private String configType;
    
    private String configGroup;
    
    private String description;
    
    private String defaultValue;
    
    private Boolean isEditable;
    
    private String createdBy;
    
    private String updatedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}


