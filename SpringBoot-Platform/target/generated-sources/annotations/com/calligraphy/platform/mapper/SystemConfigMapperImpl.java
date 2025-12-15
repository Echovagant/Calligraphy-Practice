package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.SystemConfigDto;
import com.calligraphy.platform.entity.SystemConfig;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T11:14:24+0800",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class SystemConfigMapperImpl implements SystemConfigMapper {

    @Override
    public SystemConfigDto toDto(SystemConfig systemConfig) {
        if ( systemConfig == null ) {
            return null;
        }

        SystemConfigDto.SystemConfigDtoBuilder systemConfigDto = SystemConfigDto.builder();

        systemConfigDto.configGroup( systemConfig.getConfigGroup() );
        systemConfigDto.configKey( systemConfig.getConfigKey() );
        systemConfigDto.configType( systemConfig.getConfigType() );
        systemConfigDto.configValue( systemConfig.getConfigValue() );
        systemConfigDto.createdAt( systemConfig.getCreatedAt() );
        systemConfigDto.createdBy( systemConfig.getCreatedBy() );
        systemConfigDto.defaultValue( systemConfig.getDefaultValue() );
        systemConfigDto.description( systemConfig.getDescription() );
        systemConfigDto.id( systemConfig.getId() );
        systemConfigDto.isEditable( systemConfig.getIsEditable() );
        systemConfigDto.updatedAt( systemConfig.getUpdatedAt() );
        systemConfigDto.updatedBy( systemConfig.getUpdatedBy() );

        return systemConfigDto.build();
    }

    @Override
    public SystemConfig toEntity(SystemConfigDto systemConfigDto) {
        if ( systemConfigDto == null ) {
            return null;
        }

        SystemConfig systemConfig = new SystemConfig();

        systemConfig.setId( systemConfigDto.getId() );
        systemConfig.setConfigKey( systemConfigDto.getConfigKey() );
        systemConfig.setConfigValue( systemConfigDto.getConfigValue() );
        systemConfig.setConfigType( systemConfigDto.getConfigType() );
        systemConfig.setConfigGroup( systemConfigDto.getConfigGroup() );
        systemConfig.setDescription( systemConfigDto.getDescription() );
        systemConfig.setDefaultValue( systemConfigDto.getDefaultValue() );
        systemConfig.setIsEditable( systemConfigDto.getIsEditable() );
        systemConfig.setCreatedBy( systemConfigDto.getCreatedBy() );
        systemConfig.setCreatedAt( systemConfigDto.getCreatedAt() );
        systemConfig.setUpdatedBy( systemConfigDto.getUpdatedBy() );
        systemConfig.setUpdatedAt( systemConfigDto.getUpdatedAt() );

        return systemConfig;
    }
}
