package com.calligraphy.platform.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@EntityListeners(AuditingEntityListener.class)
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "config_type", length = 50)
    private String configType;
    
    @Column(name = "config_group", length = 50)
    private String configGroup;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(name = "is_editable")
    private Boolean isEditable;
    
    @CreatedBy
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public SystemConfig() {
    }
    
    public SystemConfig(Long id, String configKey, String configValue, String configType, String configGroup, String description, String defaultValue, Boolean isEditable, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.configGroup = configGroup;
        this.description = description;
        this.defaultValue = defaultValue;
        this.isEditable = isEditable;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getConfigType() {
        return configType;
    }
    
    public void setConfigType(String configType) {
        this.configType = configType;
    }
    
    public String getConfigGroup() {
        return configGroup;
    }
    
    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Boolean getIsEditable() {
        return isEditable;
    }
    
    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


